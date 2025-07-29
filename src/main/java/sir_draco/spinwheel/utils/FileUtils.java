package sir_draco.spinwheel.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.metadata.FixedMetadataValue;
import sir_draco.spinwheel.SpinWheel;
import sir_draco.spinwheel.furnaces.CustomFurnace;
import sir_draco.spinwheel.wheel.Wheel;
import sir_draco.spinwheel.wheel.WheelStats;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class FileUtils {
    public static final String WHEEL_STRING = "Wheel";
    public static final String SPINS = ".Spins";
    public static final String TIME = ".Time";
    public static final String RARE = ".Rare";
    public static final String EPIC = ".Epic";
    public static final String LEGENDARY = ".Legendary";
    public static final String LOCATION = ".Location";
    public static final String TYPE = ".Type";

    private FileUtils() {
        // Prevent instantiation
    }

    public static void loadWheel(FileConfiguration config) {
        if (config.get(WHEEL_STRING) == null) {
            Bukkit.getLogger().info("No wheel found");
            return;
        }
        Wheel wheel = new Wheel(config.getLocation(WHEEL_STRING));
        SpinWheel.getInstance().setWheel(wheel);
    }

    public static void loadOnlinePlayers() {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;
        for (Player p : Bukkit.getServer().getOnlinePlayers())
            loadSpins(p, SpinWheel.getInstance().getPlayerData(), SpinWheel.getInstance().getSpinsStats());
    }

    public static void loadSpins(Player p, FileConfiguration playerData, Map<UUID, WheelStats> spinsStats) {
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

    public static void saveWheel() {
        Wheel wheel = SpinWheel.getInstance().getWheel();
        if (wheel == null) return;

        // Load the existing config file
        File configFile = new File(SpinWheel.getInstance().getDataFolder(), SpinWheel.CONFIG_YML);
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

    public static void saveSpins(Player p, Map<UUID, WheelStats> spinsStats, FileConfiguration playerData
                                , File dataFile) throws IOException {
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

    public static void saveSpins(Map<UUID, WheelStats> spinsStats, FileConfiguration playerData, File dataFile) throws IOException {
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

    public static void loadFurnaces(FileConfiguration furnaceData, File furnaceFile, List<CustomFurnace> customFurnaces
                                   , Map<Location, Integer> furnaceIDs) {
        ConfigurationSection section = furnaceData.getConfigurationSection("");
        if (section == null) return;

        // First, clean up duplicates in the YAML file
        cleanupDuplicateFurnacesInFile(furnaceData, furnaceFile);

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

            loadFurnacesHelper(key, loc, furnace, loadedLocations, furnaceData, customFurnaces, furnaceIDs);
        });

        // Clean up any duplicate furnaces that might exist in memory
        cleanupDuplicateFurnaces(customFurnaces, furnaceIDs);
    }

    private static void loadFurnacesHelper(String key, Location loc, Block furnace, Set<Location> loadedLocations
                                          , FileConfiguration furnaceData, List<CustomFurnace> customFurnaces,
                                           Map<Location, Integer> furnaceIDs) {
        CustomFurnace customFurnace = new CustomFurnace(loc, furnaceData.getInt(key + TYPE));
        customFurnace.setCanBreak(true);
        BlockState state = furnace.getState();
        if (state instanceof Furnace furnaceBlock) {
            if (furnaceBlock.getInventory().getFuel() != null) customFurnace.getInventory().setFuel(furnaceBlock.getInventory().getFuel());
            if (furnaceBlock.getInventory().getSmelting() != null) customFurnace.getInventory().setSmelting(furnaceBlock.getInventory().getSmelting());
            if (furnaceBlock.getInventory().getResult() != null) customFurnace.getInventory().setResult(furnaceBlock.getInventory().getResult());
        }
        customFurnaces.add(customFurnace);
        furnaceIDs.put(loc, SpinWheel.getInstance().getNextFurnaceID());
        furnace.setMetadata("superFurnace", new FixedMetadataValue(SpinWheel.getInstance(), furnaceData.getInt(key + TYPE)));
        loadedLocations.add(loc);
        SpinWheel.getInstance().incrementNextFurnaceID();
    }

    /**
     * Clean up duplicate furnaces from the YAML configuration file
     */
    private static void cleanupDuplicateFurnacesInFile(FileConfiguration furnaceData, File furnaceFile) {
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
    private static void cleanupDuplicateFurnaces(List<CustomFurnace> customFurnaces, Map<Location, Integer> furnaceIDs) {
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
    public static void saveFurnace(Location location, int speed, int id, FileConfiguration furnaceData, File furnaceFile) throws IOException {
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
    public static void removeFurnace(int id, FileConfiguration furnaceData, File furnaceFile, Map<Location, Integer> furnaceIDs) throws IOException {
        if (furnaceData == null) return;

        String key = String.valueOf(id);
        furnaceData.set(key, null);

        // Remove from furnaceIDs map
        furnaceIDs.entrySet().removeIf(entry -> entry.getValue().equals(id));

        furnaceData.save(furnaceFile);
        Bukkit.getLogger().log(Level.INFO, "[SpinWheel] Removed furnace with ID {0}", id);
    }

    /**
     * Save all furnaces to the configuration file
     */
    public static void saveFurnaces(List<CustomFurnace> customFurnaces, FileConfiguration furnaceData, File furnaceFile)
            throws IOException {
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
            num = saveFurnaceAndState(furnace, num, furnaceData);
        }

        furnaceData.save(furnaceFile);
    }

    private static int saveFurnaceAndState(CustomFurnace furnace, int num, FileConfiguration furnaceData) {
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
