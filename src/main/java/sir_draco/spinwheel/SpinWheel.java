package sir_draco.spinwheel;

import com.earth2me.essentials.Essentials;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.commands.*;
import sir_draco.spinwheel.furnaces.CustomFurnace;
import sir_draco.spinwheel.furnaces.CustomFurnaceChecker;
import sir_draco.spinwheel.furnaces.FurnaceListener;
import sir_draco.spinwheel.wheel.TimeTracker;
import sir_draco.spinwheel.wheel.Wheel;
import sir_draco.spinwheel.wheel.WheelStats;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
public final class SpinWheel extends JavaPlugin {
    public static final String CONFIG_YML = "config.yml";
    public static final String VERSION = "Version";
    public static final String WHEEL_STRING = "Wheel";
    public static final String SPINS = ".Spins";
    public static final String TIME = ".Time";
    public static final String RARE = ".Rare";
    public static final String EPIC = ".Epic";
    public static final String LEGENDARY = ".Legendary";
    public static final Random RANDOM = new Random();
    public static final String LOCATION = ".Location";
    public static final String TYPE = ".Type";

    private static SpinWheel instance;

    private final HashMap<UUID, WheelStats> spinsStats = new HashMap<>();
    private final ArrayList<EntityType> entityTypes = new ArrayList<>();
    private final ArrayList<Player> opSpins = new ArrayList<>();
    private final ArrayList<CustomFurnace> customFurnaces = new ArrayList<>();
    private final HashMap<Location, Integer> furnaceIDs = new HashMap<>();
    private final HashMap<UUID, Double> timeMultipliers = new HashMap<>();


    private Wheel wheel;
    private boolean endLoot;
    private boolean bothPicks = false;
    private boolean griefPreventionEnabled = false;
    private ArrayList<ItemStack> commonItems;
    private ArrayList<ItemStack> rareItems;
    private ArrayList<ItemStack> epicItems;
    private ArrayList<ItemStack> legendaryItems;
    private Essentials essentials;

    private FileConfiguration playerData;
    private File dataFile;
    private FileConfiguration furnaceData;
    private File furnaceFile;
    private int nextFurnaceID = 1;

    @Override
    public void onEnable() {
        instance = this;
        // Load config
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), CONFIG_YML);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (config.get(VERSION) == null || config.getDouble(VERSION) != 1.31) {
            Bukkit.getLogger().info("Replacing Config");
            saveResource(CONFIG_YML, true);
        }
        else Bukkit.getLogger().log(Level.INFO, "[SpinWheel] Version: {0}", getConfig().getDouble(VERSION));

        // Load player and furnace data
        dataFile = new File(getDataFolder(), "players.yml");
        if (!dataFile.exists()) saveResource("players.yml", true);
        playerData = YamlConfiguration.loadConfiguration(dataFile);

        furnaceFile = new File(getDataFolder(), "furnaces.yml");
        if (!furnaceFile.exists()) saveResource("furnaces.yml", true);
        furnaceData = YamlConfiguration.loadConfiguration(furnaceFile);

        // Prepare plugin and take care of players already online
        loadWheel();
        loadOnlinePlayers();
        loadFurnaces();
        entityList();
        makeRewardsLists();
        endLoot = getConfig().getBoolean("EndLoot");
        essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        // Commands
        new SpinCommand(this);
        new SpinsCommand(this);
        new SpinWheelCommand(this);
        new NextSpinCommand(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new FurnaceListener(this), this);
        getServer().getPluginManager().registerEvents(new SpinTabComplete(), this);

        if (getServer().getPluginManager().getPlugin("GriefPrevention") != null) griefPreventionEnabled = true;

        // Run the tracker for player time
        TimeTracker tracker = new TimeTracker(this);
        tracker.runTaskTimer(this, 0, 20);

        // Custom furnace timer
        BukkitRunnable furnaceTimer = new CustomFurnaceChecker(this);
        furnaceTimer.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        try {
            saveSpins();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Failed to save spins", e);
        }

        try {
            saveFurnaces();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Failed to save furnaces", e);
        }
    }

    public void loadWheel() {
        if (getConfig().get(WHEEL_STRING) == null) {
            Bukkit.getLogger().info("No wheel found");
            return;
        }
        wheel = new Wheel(getConfig().getLocation(WHEEL_STRING));
    }

    public void loadOnlinePlayers() {
        if (getServer().getOnlinePlayers().isEmpty()) return;
        for (Player p : getServer().getOnlinePlayers()) loadSpins(p);
    }

    public void loadSpins(Player p) {
        boolean success = true;
        try {
            playerData.getString(p.getUniqueId().toString());
        } catch (Exception e) {
            success = false;
        }

        if (!success) {
            WheelStats stats = new WheelStats(0, 0, 0, 0, 0);
            spinsStats.put(p.getUniqueId(), stats);
            return;
        }

        UUID uuid = p.getUniqueId();
        int spinAmount = playerData.getInt(uuid + SPINS);
        int time = playerData.getInt(uuid + TIME);
        int rare = playerData.getInt(uuid + RARE);
        int epic = playerData.getInt(uuid + EPIC);
        int legendary = playerData.getInt(uuid + LEGENDARY);
        spinsStats.put(uuid, new WheelStats(spinAmount, time, rare, epic, legendary));
    }

    public void saveWheel() {
        if (wheel == null) return;

        // Load the existing config file
        File configFile = new File(getDataFolder(), CONFIG_YML);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Set the Wheel information
        config.set(WHEEL_STRING, wheel.getC1());

        // Save the config file
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save config file");
        }
    }

    public void saveSpins(Player p) throws IOException {
        if (spinsStats.isEmpty()) return;
        if (playerData == null) return;

        if (!spinsStats.containsKey(p.getUniqueId())) return;
        WheelStats stats = spinsStats.get(p.getUniqueId());
        playerData.set(p.getUniqueId() + SPINS, stats.getSpins());
        playerData.set(p.getUniqueId() + TIME, stats.getTime());
        playerData.set(p.getUniqueId() + RARE, stats.getRare());
        playerData.set(p.getUniqueId() + EPIC, stats.getEpic());
        playerData.set(p.getUniqueId() + LEGENDARY, stats.getLegendary());

        playerData.save(dataFile);
    }

    public void saveSpins() throws IOException {
        if (spinsStats.isEmpty()) return;
        if (playerData == null) return;

        for (Map.Entry<UUID, WheelStats> p : spinsStats.entrySet()) {
            playerData.set(p.getKey() + SPINS, p.getValue().getSpins());
            playerData.set(p.getKey() + TIME, p.getValue().getTime());
            playerData.set(p.getKey() + RARE, p.getValue().getRare());
            playerData.set(p.getKey() + EPIC, p.getValue().getEpic());
            playerData.set(p.getKey() + LEGENDARY, p.getValue().getLegendary());
        }

        playerData.save(dataFile);
    }

    public int generateAward(Player p, int type) {
        // Determine rarity
        double rarity;
        switch (type) {
            case 0 -> rarity = 0.1;
            case 1 -> rarity = 0.8;
            case 2 -> rarity = 0.95;
            case 3 -> rarity = 0.999;
            default -> rarity = Math.random();
        }

        // Common
        if (rarity < 0.65) {
            dropItem(p, commonItems.get(randomSlot(commonItems.size())), wheel.getCenter());
            return 0;
        }
        // Rare
        if (rarity < .90) {
            spinsStats.get(p.getUniqueId()).changeRare(1);
            dropItem(p, rareItems.get(randomSlot(rareItems.size())), wheel.getCenter());
            return 1;
        }
        // Epic
        if (rarity < .99) {
            spinsStats.get(p.getUniqueId()).changeEpic(1);
            dropItem(p, epicItems.get(randomSlot(epicItems.size())), wheel.getCenter());
            return 2;
        }
        // Legendary
        spinsStats.get(p.getUniqueId()).changeLegendary(1);
        dropItem(p, legendaryItems.get(randomSlot(legendaryItems.size())), wheel.getCenter());
        return 3;
    }

    public int randomSlot(int max) {
        return RANDOM.nextInt(max);
    }

    public void dropItem(Player p, ItemStack item, Location loc) {
        Item drop = wheel.getWorld().dropItem(loc.add(0.5, 1, 0.5), item);
        drop.setOwner(p.getUniqueId());
        wheel.setSpinning(false);
    }

    public void makeRewardsLists() {
        makeCommonList();
        makeRareList();
        makeEpicList();
        makeLegendaryList();
    }

    public void loadFurnaces() {
        ConfigurationSection section = furnaceData.getConfigurationSection("");
        if (section == null) return;

        // First, clean up duplicates in the YAML file
        cleanupDuplicateFurnacesInFile();

        // Reload the configuration after cleanup
        section = furnaceData.getConfigurationSection("");
        if (section == null) return;

        // Use a set to track already loaded locations to prevent duplicates
        Set<Location> loadedLocations = new HashSet<>();

        section.getKeys(false).forEach(key -> {
            Location loc = furnaceData.getLocation(key + LOCATION);
            if (loc == null) {
                Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Found furnace with null location for key {0}, skipping", key);
                return;
            }

            // Skip if we already loaded a furnace at this location
            if (loadedLocations.contains(loc)) {
                Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Duplicate furnace found at {0}, skipping", loc);
                return;
            }

            Block furnace = loc.getBlock();
            // Verify the block is actually a furnace
            if (!furnace.getType().equals(Material.FURNACE)) {
                Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Expected furnace at {0} but found {1}, skipping", new Object[]{loc, furnace.getType()});
                return;
            }

            loadFurnacesHelper(key, loc, furnace, loadedLocations);
        });

        // Clean up any duplicate furnaces that might exist in memory
        cleanupDuplicateFurnaces();
    }

    private void loadFurnacesHelper(String key, Location loc, Block furnace, Set<Location> loadedLocations) {
        CustomFurnace customFurnace = new CustomFurnace(loc, furnaceData.getInt(key + TYPE));
        customFurnace.setCanBreak(true);
        BlockState state = furnace.getState();
        if (state instanceof Furnace furnaceBlock) {
            if (furnaceBlock.getInventory().getFuel() != null) customFurnace.getInventory().setFuel(furnaceBlock.getInventory().getFuel());
            if (furnaceBlock.getInventory().getSmelting() != null) customFurnace.getInventory().setSmelting(furnaceBlock.getInventory().getSmelting());
            if (furnaceBlock.getInventory().getResult() != null) customFurnace.getInventory().setResult(furnaceBlock.getInventory().getResult());
        }
        customFurnaces.add(customFurnace);
        furnaceIDs.put(loc, nextFurnaceID);
        furnace.setMetadata("superFurnace", new FixedMetadataValue(this, furnaceData.getInt(key + TYPE)));
        loadedLocations.add(loc);
        nextFurnaceID++;
    }

    /**
     * Clean up duplicate furnaces from the YAML configuration file
     */
    private void cleanupDuplicateFurnacesInFile() {
        ConfigurationSection section = furnaceData.getConfigurationSection("");
        if (section == null) return;

        Map<Location, String> locationToKey = new HashMap<>();
        Set<String> keysToRemove = new HashSet<>();
        boolean foundDuplicates = false;

        // First pass: identify duplicates
        for (String key : section.getKeys(false)) {
            Location loc = furnaceData.getLocation(key + LOCATION);
            if (loc == null) {
                // Invalid location, mark for removal
                keysToRemove.add(key);
                foundDuplicates = true;
                Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Found furnace with invalid location, removing key: {0}", key);
                continue;
            }

            if (locationToKey.containsKey(loc)) {
                // Duplicate location found
                String existingKey = locationToKey.get(loc);
                keysToRemove.add(key);
                foundDuplicates = true;
                Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Found duplicate furnace at {0}, removing key {1} (keeping {2})",
                    new Object[]{loc, key, existingKey});
            } else {
                locationToKey.put(loc, key);
            }
        }

        // Second pass: remove duplicates
        if (foundDuplicates) {
            for (String keyToRemove : keysToRemove) {
                furnaceData.set(keyToRemove, null);
            }

            try {
                furnaceData.save(furnaceFile);
                Bukkit.getLogger().log(Level.INFO, "[SpinWheel] Cleaned up {0} duplicate furnace entries from configuration file", keysToRemove.size());
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[SpinWheel] Failed to save cleaned furnace configuration", e);
            }
        }
    }

    /**
     * Remove duplicate furnaces from the same location, keeping only the first one
     */
    private void cleanupDuplicateFurnaces() {
        Map<Location, CustomFurnace> uniqueFurnaces = new LinkedHashMap<>();

        for (CustomFurnace furnace : customFurnaces) {
            Location loc = furnace.getLocation();
            if (!uniqueFurnaces.containsKey(loc)) {
                uniqueFurnaces.put(loc, furnace);
            } else {
                Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Removing duplicate furnace at {0}", loc);
            }
        }

        customFurnaces.clear();
        customFurnaces.addAll(uniqueFurnaces.values());

        // Clean up furnaceIDs map to match
        Map<Location, Integer> cleanedIDs = new HashMap<>();
        for (CustomFurnace furnace : customFurnaces) {
            Location loc = furnace.getLocation();
            if (furnaceIDs.containsKey(loc)) {
                cleanedIDs.put(loc, furnaceIDs.get(loc));
            }
        }
        furnaceIDs.clear();
        furnaceIDs.putAll(cleanedIDs);
    }

    /**
     * Save a single furnace to the configuration
     */
    public void saveFurnace(Location location, int speed, int id) throws IOException {
        if (furnaceData == null) return;

        String key = String.valueOf(id);
        furnaceData.set(key + LOCATION, location);
        furnaceData.set(key + TYPE, speed);

        furnaceData.save(furnaceFile);
        Bukkit.getLogger().log(Level.INFO, "[SpinWheel] Saved furnace with ID {0} at {1}", new Object[]{id, location});
    }

    /**
     * Remove a furnace from the configuration by ID
     */
    public void removeFurnace(int id) throws IOException {
        if (furnaceData == null) return;

        String key = String.valueOf(id);
        furnaceData.set(key, null);

        // Remove from furnaceIDs map
        furnaceIDs.entrySet().removeIf(entry -> entry.getValue().equals(id));

        furnaceData.save(furnaceFile);
        Bukkit.getLogger().log(Level.INFO, "[SpinWheel] Removed furnace with ID {0}", id);
    }

    /**
     * Check if a furnace already exists at the given location
     */
    public boolean hasFurnaceAt(Location location) {
        return customFurnaces.stream().anyMatch(furnace -> furnace.getLocation().equals(location));
    }

    public void fireworks(int type) {
        switch (type) {
            case 1 -> rareFireworks();
            case 2 -> epicFireworks();
            default -> legendaryFireworks();
        }
    }

    private void legendaryFireworks() {
        List<Color> colors = getRainbowColors();
        new BukkitRunnable() {
            int total = 0;
            @Override
            public void run() {
                if (total == 3) {
                    cancel();
                    return;
                }

                for (Location loc : wheel.getCorners()) {
                    spawnFirework(loc.add(0, 1, 0), 1, FireworkEffect.Type.BALL_LARGE, colors, true, true, null);
                }
                for (Location loc : wheel.getCardinalDirections()) {
                    spawnFirework(loc.add(0, 1, 0), 0, FireworkEffect.Type.BALL_LARGE, colors, true, true, null);
                }
                for (Location loc : wheel.getCardinalDirections()) {
                    spawnFirework(loc.add(0, 1, 0), 2, FireworkEffect.Type.BALL_LARGE, colors, true, true, null);
                }
                total++;
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void epicFireworks() {
        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.fromRGB(255, 0, 255));
        for (Location loc : wheel.getCorners()) {
            spawnFirework(loc.add(0, 1, 0), 0, FireworkEffect.Type.BALL, colors, true, true, null);
        }
        for (Location loc : wheel.getCardinalDirections()) {
            spawnFirework(loc.add(0, 1, 0), 1, FireworkEffect.Type.BALL, colors, true, true, null);
        }
    }

    private void rareFireworks() {
        for (Location loc : wheel.getCorners()) {
            ArrayList<Color> colors = new ArrayList<>();
            colors.add(Color.fromRGB(0, 0, 120));
            spawnFirework(loc.add(0, 1, 0), 0, FireworkEffect.Type.BALL, colors, false, true, null);
        }
    }

    public void spawnFirework(Location loc, int power, FireworkEffect.Type type, List<Color> colors, boolean trail, boolean flicker, List<Color> fadeColors) {
        Firework fw = (Firework) wheel.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwm = fw.getFireworkMeta();

        FireworkEffect.Builder build = FireworkEffect.builder();
        build.with(type);
        build.withColor(colors);
        if (trail) build.withTrail();
        if (flicker) build.withFlicker();
        if (fadeColors != null) build.withFade(fadeColors);

        fwm.addEffect(build.build());
        fwm.setPower(power);
        fw.setFireworkMeta(fwm);
    }

    public List<Color> getRainbowColors() {
        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.fromRGB(255, 0, 0));
        colors.add(Color.fromRGB(0, 255, 0));
        colors.add(Color.fromRGB(0, 0, 255));
        colors.add(Color.fromRGB(255, 0, 255));
        colors.add(Color.fromRGB(255, 255, 0));
        colors.add(Color.fromRGB(0, 255, 255));
        colors.add(Color.fromRGB(255, 255, 255));
        return colors;
    }

    public void makeCommonList() {
        commonItems = new ArrayList<>();
        commonItems.add(new ItemStack(Material.BREAD, 16));
        commonItems.add(new ItemStack(Material.COOKED_BEEF, 16));
        commonItems.add(new ItemStack(Material.COOKED_PORKCHOP, 16));
        commonItems.add(new ItemStack(Material.COOKED_SALMON, 16));
        commonItems.add(new ItemStack(Material.CARROT, 16));
        commonItems.add(new ItemStack(Material.POTATO, 16));
        commonItems.add(new ItemStack(Material.BEETROOT, 16));
        commonItems.add(new ItemStack(Material.HAY_BLOCK, 16));
        commonItems.add(new ItemStack(Material.BAMBOO, 16));
        commonItems.add(new ItemStack(Material.SUGAR_CANE, 16));
        commonItems.add(new ItemStack(Material.OAK_LOG, 64));
        commonItems.add(new ItemStack(Material.SPRUCE_LOG, 64));
        commonItems.add(new ItemStack(Material.BIRCH_LOG, 64));
        commonItems.add(new ItemStack(Material.JUNGLE_LOG, 64));
        commonItems.add(new ItemStack(Material.ACACIA_LOG, 64));
        commonItems.add(new ItemStack(Material.DARK_OAK_LOG, 64));
        commonItems.add(new ItemStack(Material.MANGROVE_LOG, 64));
        commonItems.add(new ItemStack(Material.CHERRY_LOG, 64));
        commonItems.add(new ItemStack(Material.CRIMSON_STEM, 64));
        commonItems.add(new ItemStack(Material.WARPED_STEM, 64));
        commonItems.add(new ItemStack(Material.ROTTEN_FLESH, 16));
        commonItems.add(new ItemStack(Material.COBBLESTONE, 64));
        commonItems.add(new ItemStack(Material.STONE, 64));
        commonItems.add(new ItemStack(Material.GUNPOWDER, 16));
        commonItems.add(new ItemStack(Material.BONE, 16));
        commonItems.add(new ItemStack(Material.SPIDER_EYE, 16));
        commonItems.add(new ItemStack(Material.OAK_SAPLING, 4));
        commonItems.add(new ItemStack(Material.SPRUCE_SAPLING, 4));
        commonItems.add(new ItemStack(Material.BIRCH_SAPLING, 4));
        commonItems.add(new ItemStack(Material.JUNGLE_SAPLING, 4));
        commonItems.add(new ItemStack(Material.ACACIA_SAPLING, 4));
        commonItems.add(new ItemStack(Material.DARK_OAK_SAPLING, 4));
        commonItems.add(new ItemStack(Material.MANGROVE_PROPAGULE, 4));
        commonItems.add(new ItemStack(Material.CHERRY_SAPLING, 4));
        commonItems.add(new ItemStack(Material.CACTUS, 4));
        commonItems.add(new ItemStack(Material.DIRT, 1));
        commonItems.add(new ItemStack(Material.LEATHER_HORSE_ARMOR, 1));
        commonItems.add(new ItemStack(Material.DIAMOND, 5));
        commonItems.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        commonItems.add(new ItemStack(Material.IRON_INGOT, 24));
        commonItems.add(new ItemStack(Material.NAME_TAG, 2));
    }

    public void makeRareList() {
        rareItems = new ArrayList<>();
        if (endLoot) {
            rareItems.add(new ItemStack(Material.SHULKER_BOX, 1));
            ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
            FireworkMeta metaData = (FireworkMeta) firework.getItemMeta();
            if (metaData == null) return;
            metaData.setPower(3);
            firework.setItemMeta(metaData);
            rareItems.add(firework);
        }
        rareItems.add(new ItemStack(Material.DIAMOND_AXE, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_PICKAXE, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_SWORD, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_SHOVEL, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_HOE, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_HELMET, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
        rareItems.add(new ItemStack(Material.DIAMOND_BOOTS, 1));
        rareItems.add(new ItemStack(Material.ANCIENT_DEBRIS, 3));
        rareItems.add(new ItemStack(Material.WITHER_SKELETON_SKULL, 1));
        rareItems.add(new ItemStack(Material.IRON_HORSE_ARMOR, 1));
        rareItems.add(new ItemStack(Material.GOLDEN_HORSE_ARMOR, 1));
        rareItems.add(new ItemStack(Material.SADDLE, 1));
        rareItems.add(enchantedBook(Enchantment.UNBREAKING, 3));
        rareItems.add(new ItemStack(Material.GOLDEN_APPLE, 4));
        rareItems.add(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1));
    }

    public void makeEpicList() {
        epicItems = new ArrayList<>();
        if (endLoot) epicItems.add(new ItemStack(Material.ELYTRA, 1));
        epicItems.add(fastFurnace(1));
        epicItems.add(fastFurnace(2));
        epicItems.add(fastFurnace(3));

        int rand = randomSlot(entityTypes.size());
        ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = spawner.getItemMeta();
        if (meta == null) return;
        meta.setLore(null);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + entityTypes.get(rand).toString());
        lore.add(ChatColor.RED + "YOU CAN NOT PICK THIS UP ONCE YOU PUT IT DOWN!");
        meta.setCustomModelData(rand);
        meta.setLore(lore);
        spawner.setItemMeta(meta);
        epicItems.add(spawner);

        epicItems.add(new ItemStack(Material.WITHER_SKELETON_SKULL, 3));
        epicItems.add(new ItemStack(Material.TRIDENT, 1));
        epicItems.add(new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        epicItems.add(new ItemStack(Material.HEART_OF_THE_SEA, 1));
        epicItems.add(enchantedBook(Enchantment.MENDING, 1));
        epicItems.add(enchantedBook(Enchantment.SILK_TOUCH, 1));
        epicItems.add(enchantedBook(Enchantment.FORTUNE, 3));
        epicItems.add(enchantedBook(Enchantment.EFFICIENCY, 5));
        epicItems.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        epicItems.add(new ItemStack(Material.NETHERITE_INGOT, 3));
        epicItems.add(new ItemStack(Material.MACE, 1));

        ItemStack horseBale = new ItemStack(Material.HAY_BLOCK, 1);
        meta = horseBale.getItemMeta();
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Spawns a horse with max stats (vanilla)");
        lore.add(ChatColor.RED + "You can't use it in spawn");
        if (meta == null) return;
        meta.setLore(lore);
        meta.setCustomModelData(1);
        horseBale.setItemMeta(meta);
        epicItems.add(horseBale);
    }

    public void makeLegendaryList() {
        legendaryItems = new ArrayList<>();
        if (endLoot) {
            ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
            elytra.addUnsafeEnchantment(Enchantment.UNBREAKING, 7);
            legendaryItems.add(elytra);
        }
        legendaryItems.add(fastFurnace(4));

        ItemStack spawner = new ItemStack(Material.SPAWNER, 3);
        int rand = randomSlot(entityTypes.size());
        ItemMeta meta = spawner.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Super Spawner");
        lore.add(ChatColor.GRAY + entityTypes.get(rand).toString());
        lore.add(ChatColor.RED + "YOU CAN NOT PICK THIS UP ONCE YOU PUT IT DOWN!");
        if (meta == null) return;
        meta.setCustomModelData(rand + 100);
        meta.setLore(lore);
        spawner.setItemMeta(meta);
        legendaryItems.add(spawner);

        legendaryItems.add(diamondMax(Material.DIAMOND_PICKAXE));
        legendaryItems.add(diamondMax(Material.DIAMOND_AXE));
        legendaryItems.add(diamondMax(Material.DIAMOND_SHOVEL));
        legendaryItems.add(diamondMax(Material.DIAMOND_SWORD));
        legendaryItems.add(diamondMax(Material.DIAMOND_HELMET));
        legendaryItems.add(diamondMax(Material.DIAMOND_CHESTPLATE));
        legendaryItems.add(diamondMax(Material.DIAMOND_LEGGINGS));
        legendaryItems.add(diamondMax(Material.DIAMOND_BOOTS));
        legendaryItems.add(diamondMax(Material.BOW));
        legendaryItems.add(diamondMax(Material.DIAMOND_PICKAXE));
    }

    public ItemStack fastFurnace(int type) {
        if (type == 1) {
            ItemStack furnace = new ItemStack(Material.FURNACE, 1);
            ItemMeta meta = furnace.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Smelts twice as fast (12 Items/Minute)");
            if (meta == null) return furnace;
            meta.setLore(lore);
            meta.setCustomModelData(1);
            furnace.setItemMeta(meta);
            return furnace;
        }
        else if (type == 2) {
            ItemStack furnace = new ItemStack(Material.FURNACE, 1);
            ItemMeta meta = furnace.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Smelts three times as fast (18 Items/Minute)");
            if (meta == null) return furnace;
            meta.setLore(lore);
            meta.setCustomModelData(2);
            furnace.setItemMeta(meta);
            return furnace;
        }
        else if (type == 3) {
            ItemStack furnace = new ItemStack(Material.FURNACE, 1);
            ItemMeta meta = furnace.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Smelts four times as fast (24 Items/Minute)");
            if (meta == null) return furnace;
            meta.setLore(lore);
            meta.setCustomModelData(3);
            furnace.setItemMeta(meta);
            return furnace;
        }
        else if (type == 4) {
            ItemStack furnace = new ItemStack(Material.FURNACE, 1);
            ItemMeta meta = furnace.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Smelts items in only two ticks (100 Items/Minute)");
            if (meta == null) return furnace;
            meta.setLore(lore);
            meta.setCustomModelData(4);
            furnace.setItemMeta(meta);
            return furnace;
        }
        return new ItemStack(Material.FURNACE, 1);
    }

    public ItemStack enchantedBook(Enchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta == null) return book;
        meta.addStoredEnchant(enchant, level, true);
        book.setItemMeta(meta);
        return book;
    }

    public void entityList() {
        entityTypes.add(EntityType.ENDERMAN);
        entityTypes.add(EntityType.SKELETON);
        entityTypes.add(EntityType.ZOMBIE);
        entityTypes.add(EntityType.CREEPER);
        entityTypes.add(EntityType.SPIDER);
        entityTypes.add(EntityType.WITCH);
        entityTypes.add(EntityType.SLIME);
        entityTypes.add(EntityType.GUARDIAN);
    }

    public ItemStack diamondMax(Material mat) {
        ItemStack item;
        if (mat.equals(Material.DIAMOND_PICKAXE)) {
            item = new ItemStack(Material.DIAMOND_PICKAXE);
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 8);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            if (!bothPicks) {
                item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
                bothPicks = true;
            }
            else item.addUnsafeEnchantment(Enchantment.FORTUNE, 6);
        }
        else if (mat.equals(Material.DIAMOND_AXE)) {
            item = new ItemStack(Material.DIAMOND_AXE);
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 8);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
        else if (mat.equals(Material.DIAMOND_SHOVEL)) {
            item = new ItemStack(Material.DIAMOND_SHOVEL);
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 8);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
        else if (mat.equals(Material.DIAMOND_SWORD)) {
            item = new ItemStack(Material.DIAMOND_SWORD);
            item.addUnsafeEnchantment(Enchantment.SHARPNESS, 8);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
            item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 4);
            item.addUnsafeEnchantment(Enchantment.LOOTING, 4);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
        else if (mat.equals(Material.DIAMOND_HELMET)) {
            item = new ItemStack(Material.DIAMOND_HELMET);
            item.addUnsafeEnchantment(Enchantment.PROTECTION, 7);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.THORNS, 6);
            item.addUnsafeEnchantment(Enchantment.RESPIRATION, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            item.addUnsafeEnchantment(Enchantment.AQUA_AFFINITY, 1);
        }
        else if (mat.equals(Material.DIAMOND_CHESTPLATE)) {
            item = new ItemStack(Material.DIAMOND_CHESTPLATE);
            item.addUnsafeEnchantment(Enchantment.PROTECTION, 7);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.THORNS, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
        else if (mat.equals(Material.DIAMOND_LEGGINGS)) {
            item = new ItemStack(Material.DIAMOND_LEGGINGS);
            item.addUnsafeEnchantment(Enchantment.PROTECTION, 7);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.THORNS, 6);
            item.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
        else if (mat.equals(Material.DIAMOND_BOOTS)) {
            item = new ItemStack(Material.DIAMOND_BOOTS);
            item.addUnsafeEnchantment(Enchantment.PROTECTION, 7);
            item.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 7);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.THORNS, 6);
            item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 6);
            item.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
        else {
            item = new ItemStack(Material.BOW);
            item.addUnsafeEnchantment(Enchantment.POWER, 8);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.PUNCH, 4);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            item.addUnsafeEnchantment(Enchantment.INFINITY, 1);
            item.addUnsafeEnchantment(Enchantment.FLAME, 1);
        }
        return item;
    }

    /**
     * Returns true if there is a claim there
     */
    public boolean checkForClaim(Player p, Location loc) {
        String noBuildReason = GriefPrevention.instance.allowBuild(p, loc);
        return (noBuildReason != null);
    }

    public Wheel getWheel() {
        return wheel;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    public Map<UUID, WheelStats> getSpinsStats() {
        return spinsStats;
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public boolean isEndLoot() {
        return endLoot;
    }

    public void setEndLoot(boolean endLoot) {
        this.endLoot = endLoot;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public List<ItemStack> getCommonItems() {
        return commonItems;
    }

    public List<ItemStack> getRareItems() {
        return rareItems;
    }

    public List<ItemStack> getEpicItems() {
        return epicItems;
    }

    public List<ItemStack> getLegendaryItems() {
        return legendaryItems;
    }

    public List<Player> getOpSpins() {
        return opSpins;
    }

    public void addOpSpins(Player p) {
        opSpins.add(p);
    }

    public void removeOpSpins(Player p) {
        opSpins.remove(p);
    }

    public List<CustomFurnace> getCustomFurnaces() {
        return customFurnaces;
    }

    public int getNextFurnaceID() {
        return nextFurnaceID;
    }

    public void incrementNextFurnaceID() {
        nextFurnaceID++;
    }

    public Map<Location, Integer> getFurnaceIDs() {
        return furnaceIDs;
    }

    public boolean isGriefPreventionEnabled() {
        return griefPreventionEnabled;
    }

    public double getTimeMultiplier(UUID uuid) {
        return timeMultipliers.getOrDefault(uuid, 1.0);
    }

    public int getEffectiveWaitTime(UUID uuid) {
        double multiplier = getTimeMultiplier(uuid);
        return (int) (getConfig().getInt("Time") * multiplier);
    }

    public static void decreaseSpinWaitTime(UUID uuid, double decrease) {
        if (instance == null) return;
        decrease = decrease * 100;

        // Clamp decrease to reasonable bounds (0.1 to 99.9 for 90% to 0.1% of original time)
        decrease = Math.clamp(decrease, 0.1, 99.9);

        // Convert percentage to multiplier (50.0 -> 0.5)
        double multiplier = (100.0 - decrease) / 100.0;

        instance.timeMultipliers.put(uuid, multiplier);
    }

    /**
     * Save all furnaces to the configuration file
     */
    public void saveFurnaces() throws IOException {
        if (customFurnaces.isEmpty()) return;
        if (furnaceData == null) return;

        // Clear existing data first
        ConfigurationSection section = furnaceData.getConfigurationSection("");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                furnaceData.set(key + LOCATION, null);
                furnaceData.set(key + TYPE, null);
            }
        }

        // Save all current furnaces
        int num = 1;
        for (CustomFurnace furnace : customFurnaces) {
            num = saveFurnaceAndState(furnace, num);
        }

        furnaceData.save(furnaceFile);
    }

    private int saveFurnaceAndState(CustomFurnace furnace, int num) {
        furnaceData.set(num + LOCATION, furnace.getLocation());
        furnaceData.set(num + TYPE, furnace.getSpeed());
        Block furnaceBlock = furnace.getLocation().getBlock();
        BlockState state = furnaceBlock.getState();
        if (state instanceof Furnace block) {
            FurnaceInventory inv = block.getInventory();
            if (furnace.getFuel() != null) inv.setFuel(furnace.getFuel());
            if (furnace.getSmelting() != null) inv.setSmelting(furnace.getSmelting());
            if (furnace.getResult() != null) inv.setResult(furnace.getResult());
        }
        num++;
        return num;
    }
}
