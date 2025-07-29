package sir_draco.spinwheel;

import com.earth2me.essentials.Essentials;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.commands.*;
import sir_draco.spinwheel.furnaces.CustomFurnace;
import sir_draco.spinwheel.furnaces.CustomFurnaceChecker;
import sir_draco.spinwheel.furnaces.FurnaceListener;
import sir_draco.spinwheel.utils.FileUtils;
import sir_draco.spinwheel.utils.SpinRewards;
import sir_draco.spinwheel.wheel.TimeTracker;
import sir_draco.spinwheel.wheel.Wheel;
import sir_draco.spinwheel.wheel.WheelStats;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class SpinWheel extends JavaPlugin {
    public static final String CONFIG_YML = "config.yml";
    public static final String VERSION = "Version";

    private static SpinWheel instance;

    private final HashMap<UUID, WheelStats> spinsStats = new HashMap<>();
    private final List<EntityType> entityTypes = new ArrayList<>();
    private final ArrayList<Player> opSpins = new ArrayList<>();
    private final ArrayList<CustomFurnace> customFurnaces = new ArrayList<>();
    private final HashMap<Location, Integer> furnaceIDs = new HashMap<>();
    private final HashMap<UUID, Double> timeMultipliers = new HashMap<>();


    private Wheel wheel;
    private boolean endLoot;
    private boolean griefPreventionEnabled = false;
    private final ArrayList<ItemStack> commonItems = new ArrayList<>();
    private final ArrayList<ItemStack> rareItems = new ArrayList<>();
    private final ArrayList<ItemStack> epicItems = new ArrayList<>();
    private final ArrayList<ItemStack> legendaryItems = new ArrayList<>();
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
        FileUtils.loadWheel(getConfig());
        FileUtils.loadOnlinePlayers();
        FileUtils.loadFurnaces(furnaceData, furnaceFile, customFurnaces, furnaceIDs);
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
            FileUtils.saveSpins(spinsStats, playerData, dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Failed to save spins", e);
        }

        try {
            FileUtils.saveFurnaces(customFurnaces, furnaceData, furnaceFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Failed to save furnaces", e);
        }
    }

    public void makeRewardsLists() {
        SpinRewards.makeCommonList(commonItems);
        SpinRewards.makeRareList(rareItems);
        SpinRewards.makeEpicList(epicItems, entityTypes);
        SpinRewards.makeLegendaryList(legendaryItems, entityTypes);
    }

    /**
     * Check if a furnace already exists at the given location
     */
    public boolean hasFurnaceAt(Location location) {
        return customFurnaces.stream().anyMatch(furnace -> furnace.getLocation().equals(location));
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

    public Wheel getWheel() {
        return wheel;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    public Map<UUID, WheelStats> getSpinsStats() {
        return spinsStats;
    }

    public FileConfiguration getPlayerData() {
        return playerData;
    }

    public File getDataFile() {
        return dataFile;
    }

    public FileConfiguration getFurnaceData() {
        return furnaceData;
    }

    public File getFurnaceFile() {
        return furnaceFile;
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

    public static SpinWheel getInstance() {
        return instance;
    }
}
