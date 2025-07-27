package sir_draco.spinwheel.furnaces;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.SpinWheel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomFurnaceChecker extends BukkitRunnable {

    private final SpinWheel plugin;
    private static final HashMap<Material, Integer> burnTimeList = new HashMap<>();
    private static final HashMap<Material, Material> furnaceRecipes = new HashMap<>();

    public CustomFurnaceChecker(SpinWheel plugin) {
        this.plugin = plugin;
        createBurnTimeList();
        createFurnaceRecipes();
    }

    @Override
    public void run() {
        if (plugin.getCustomFurnaces().isEmpty()) return;
        for (CustomFurnace furnace : plugin.getCustomFurnaces()) {
            if (furnace.getSmelting() == null || furnace.getSmelting().getType().isAir()) continue;
            furnace.tickFurnace();
        }
    }

    public void createBurnTimeList() {
        burnTimeList.put(Material.LAVA_BUCKET, 20000);
        burnTimeList.put(Material.COAL_BLOCK, 16000);
        burnTimeList.put(Material.DRIED_KELP_BLOCK, 4000);
        burnTimeList.put(Material.BLAZE_ROD, 2400);
        burnTimeList.put(Material.COAL, 1600);
        burnTimeList.put(Material.CHARCOAL, 1600);
        burnTimeList.put(Material.OAK_LOG, 300);
        burnTimeList.put(Material.BIRCH_LOG, 300);
        burnTimeList.put(Material.SPRUCE_LOG, 300);
        burnTimeList.put(Material.JUNGLE_LOG, 300);
        burnTimeList.put(Material.ACACIA_LOG, 300);
        burnTimeList.put(Material.DARK_OAK_LOG, 300);
        burnTimeList.put(Material.MANGROVE_LOG, 300);
        burnTimeList.put(Material.CHERRY_LOG, 300);
        burnTimeList.put(Material.STRIPPED_OAK_LOG, 300);
        burnTimeList.put(Material.STRIPPED_BIRCH_LOG, 300);
        burnTimeList.put(Material.STRIPPED_SPRUCE_LOG, 300);
        burnTimeList.put(Material.STRIPPED_JUNGLE_LOG, 300);
        burnTimeList.put(Material.STRIPPED_ACACIA_LOG, 300);
        burnTimeList.put(Material.STRIPPED_DARK_OAK_LOG, 300);
        burnTimeList.put(Material.STRIPPED_MANGROVE_LOG, 300);
        burnTimeList.put(Material.STRIPPED_CHERRY_LOG, 300);
        burnTimeList.put(Material.OAK_PLANKS, 300);
        burnTimeList.put(Material.BIRCH_PLANKS, 300);
        burnTimeList.put(Material.SPRUCE_PLANKS, 300);
        burnTimeList.put(Material.JUNGLE_PLANKS, 300);
        burnTimeList.put(Material.ACACIA_PLANKS, 300);
        burnTimeList.put(Material.DARK_OAK_PLANKS, 300);
        burnTimeList.put(Material.MANGROVE_PLANKS, 300);
        burnTimeList.put(Material.CHERRY_PLANKS, 300);
        burnTimeList.put(Material.OAK_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.BIRCH_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.SPRUCE_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.JUNGLE_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.ACACIA_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.DARK_OAK_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.MANGROVE_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.CHERRY_PRESSURE_PLATE, 300);
        burnTimeList.put(Material.OAK_FENCE, 300);
        burnTimeList.put(Material.BIRCH_FENCE, 300);
        burnTimeList.put(Material.SPRUCE_FENCE, 300);
        burnTimeList.put(Material.JUNGLE_FENCE, 300);
        burnTimeList.put(Material.ACACIA_FENCE, 300);
        burnTimeList.put(Material.DARK_OAK_FENCE, 300);
        burnTimeList.put(Material.MANGROVE_FENCE, 300);
        burnTimeList.put(Material.CHERRY_FENCE, 300);
        burnTimeList.put(Material.OAK_FENCE_GATE, 300);
        burnTimeList.put(Material.BIRCH_FENCE_GATE, 300);
        burnTimeList.put(Material.SPRUCE_FENCE_GATE, 300);
        burnTimeList.put(Material.JUNGLE_FENCE_GATE, 300);
        burnTimeList.put(Material.ACACIA_FENCE_GATE, 300);
        burnTimeList.put(Material.DARK_OAK_FENCE_GATE, 300);
        burnTimeList.put(Material.MANGROVE_FENCE_GATE, 300);
        burnTimeList.put(Material.CHERRY_FENCE_GATE, 300);
        burnTimeList.put(Material.OAK_STAIRS, 300);
        burnTimeList.put(Material.BIRCH_STAIRS, 300);
        burnTimeList.put(Material.SPRUCE_STAIRS, 300);
        burnTimeList.put(Material.JUNGLE_STAIRS, 300);
        burnTimeList.put(Material.ACACIA_STAIRS, 300);
        burnTimeList.put(Material.DARK_OAK_STAIRS, 300);
        burnTimeList.put(Material.MANGROVE_STAIRS, 300);
        burnTimeList.put(Material.CHERRY_STAIRS, 300);
        burnTimeList.put(Material.OAK_SLAB, 150);
        burnTimeList.put(Material.BIRCH_SLAB, 150);
        burnTimeList.put(Material.SPRUCE_SLAB, 150);
        burnTimeList.put(Material.JUNGLE_SLAB, 150);
        burnTimeList.put(Material.ACACIA_SLAB, 150);
        burnTimeList.put(Material.DARK_OAK_SLAB, 150);
        burnTimeList.put(Material.MANGROVE_SLAB, 150);
        burnTimeList.put(Material.CHERRY_SLAB, 150);
        burnTimeList.put(Material.OAK_TRAPDOOR, 300);
        burnTimeList.put(Material.BIRCH_TRAPDOOR, 300);
        burnTimeList.put(Material.SPRUCE_TRAPDOOR, 300);
        burnTimeList.put(Material.JUNGLE_TRAPDOOR, 300);
        burnTimeList.put(Material.ACACIA_TRAPDOOR, 300);
        burnTimeList.put(Material.DARK_OAK_TRAPDOOR, 300);
        burnTimeList.put(Material.MANGROVE_TRAPDOOR, 300);
        burnTimeList.put(Material.CHERRY_TRAPDOOR, 300);
        burnTimeList.put(Material.OAK_BUTTON, 100);
        burnTimeList.put(Material.BIRCH_BUTTON, 100);
        burnTimeList.put(Material.SPRUCE_BUTTON, 100);
        burnTimeList.put(Material.JUNGLE_BUTTON, 100);
        burnTimeList.put(Material.ACACIA_BUTTON, 100);
        burnTimeList.put(Material.DARK_OAK_BUTTON, 100);
        burnTimeList.put(Material.MANGROVE_BUTTON, 100);
        burnTimeList.put(Material.CHERRY_BUTTON, 100);
        burnTimeList.put(Material.OAK_DOOR, 200);
        burnTimeList.put(Material.BIRCH_DOOR, 200);
        burnTimeList.put(Material.SPRUCE_DOOR, 200);
        burnTimeList.put(Material.JUNGLE_DOOR, 200);
        burnTimeList.put(Material.ACACIA_DOOR, 200);
        burnTimeList.put(Material.DARK_OAK_DOOR, 200);
        burnTimeList.put(Material.MANGROVE_DOOR, 200);
        burnTimeList.put(Material.CHERRY_DOOR, 200);
        burnTimeList.put(Material.CRAFTING_TABLE, 300);
        burnTimeList.put(Material.CARTOGRAPHY_TABLE, 300);
        burnTimeList.put(Material.FLETCHING_TABLE, 300);
        burnTimeList.put(Material.SMITHING_TABLE, 300);
        burnTimeList.put(Material.LOOM, 300);
        burnTimeList.put(Material.LECTERN, 300);
        burnTimeList.put(Material.COMPOSTER, 300);
        burnTimeList.put(Material.BOOKSHELF, 300);
        burnTimeList.put(Material.JUKEBOX, 300);
        burnTimeList.put(Material.BARREL, 300);
        burnTimeList.put(Material.CHEST, 300);
        burnTimeList.put(Material.TRAPPED_CHEST, 300);
        burnTimeList.put(Material.DAYLIGHT_DETECTOR, 300);
        burnTimeList.put(Material.NOTE_BLOCK, 300);
        burnTimeList.put(Material.BROWN_MUSHROOM_BLOCK, 300);
        burnTimeList.put(Material.RED_MUSHROOM_BLOCK, 300);
        burnTimeList.put(Material.MUSHROOM_STEM, 300);
        burnTimeList.put(Material.LADDER, 300);
        burnTimeList.put(Material.OAK_BOAT, 200);
        burnTimeList.put(Material.BIRCH_BOAT, 200);
        burnTimeList.put(Material.SPRUCE_BOAT, 200);
        burnTimeList.put(Material.JUNGLE_BOAT, 200);
        burnTimeList.put(Material.ACACIA_BOAT, 200);
        burnTimeList.put(Material.DARK_OAK_BOAT, 200);
        burnTimeList.put(Material.MANGROVE_BOAT, 200);
        burnTimeList.put(Material.CHERRY_BOAT, 200);
        burnTimeList.put(Material.OAK_SIGN, 200);
        burnTimeList.put(Material.BIRCH_SIGN, 200);
        burnTimeList.put(Material.SPRUCE_SIGN, 200);
        burnTimeList.put(Material.JUNGLE_SIGN, 200);
        burnTimeList.put(Material.ACACIA_SIGN, 200);
        burnTimeList.put(Material.DARK_OAK_SIGN, 200);
        burnTimeList.put(Material.MANGROVE_SIGN, 200);
        burnTimeList.put(Material.CHERRY_SIGN, 200);
        burnTimeList.put(Material.STICK, 100);
        burnTimeList.put(Material.BOWL, 100);
        burnTimeList.put(Material.WOODEN_SWORD, 200);
        burnTimeList.put(Material.WOODEN_SHOVEL, 200);
        burnTimeList.put(Material.WOODEN_PICKAXE, 200);
        burnTimeList.put(Material.WOODEN_AXE, 200);
        burnTimeList.put(Material.WOODEN_HOE, 200);
        burnTimeList.put(Material.OAK_SAPLING, 100);
        burnTimeList.put(Material.BIRCH_SAPLING, 100);
        burnTimeList.put(Material.SPRUCE_SAPLING, 100);
        burnTimeList.put(Material.JUNGLE_SAPLING, 100);
        burnTimeList.put(Material.ACACIA_SAPLING, 100);
        burnTimeList.put(Material.DARK_OAK_SAPLING, 100);
        burnTimeList.put(Material.MANGROVE_PROPAGULE, 100);
        burnTimeList.put(Material.CHERRY_SAPLING, 100);
        burnTimeList.put(Material.WHITE_WOOL, 100);
        burnTimeList.put(Material.ORANGE_WOOL, 100);
        burnTimeList.put(Material.MAGENTA_WOOL, 100);
        burnTimeList.put(Material.LIGHT_BLUE_WOOL, 100);
        burnTimeList.put(Material.YELLOW_WOOL, 100);
        burnTimeList.put(Material.LIME_WOOL, 100);
        burnTimeList.put(Material.PINK_WOOL, 100);
        burnTimeList.put(Material.GRAY_WOOL, 100);
        burnTimeList.put(Material.LIGHT_GRAY_WOOL, 100);
        burnTimeList.put(Material.CYAN_WOOL, 100);
        burnTimeList.put(Material.PURPLE_WOOL, 100);
        burnTimeList.put(Material.BLUE_WOOL, 100);
        burnTimeList.put(Material.BROWN_WOOL, 100);
        burnTimeList.put(Material.GREEN_WOOL, 100);
        burnTimeList.put(Material.RED_WOOL, 100);
        burnTimeList.put(Material.BLACK_WOOL, 100);
        burnTimeList.put(Material.WHITE_CARPET, 50);
        burnTimeList.put(Material.ORANGE_CARPET, 50);
        burnTimeList.put(Material.MAGENTA_CARPET, 50);
        burnTimeList.put(Material.LIGHT_BLUE_CARPET, 50);
        burnTimeList.put(Material.YELLOW_CARPET, 50);
        burnTimeList.put(Material.LIME_CARPET, 50);
        burnTimeList.put(Material.PINK_CARPET, 50);
        burnTimeList.put(Material.GRAY_CARPET, 50);
        burnTimeList.put(Material.LIGHT_GRAY_CARPET, 50);
        burnTimeList.put(Material.CYAN_CARPET, 50);
        burnTimeList.put(Material.PURPLE_CARPET, 50);
        burnTimeList.put(Material.BLUE_CARPET, 50);
        burnTimeList.put(Material.BROWN_CARPET, 50);
        burnTimeList.put(Material.GREEN_CARPET, 50);
        burnTimeList.put(Material.RED_CARPET, 50);
        burnTimeList.put(Material.BLACK_CARPET, 50);
        burnTimeList.put(Material.SCAFFOLDING, 50);
        burnTimeList.put(Material.BAMBOO, 50);
    }

    public void createFurnaceRecipes() {
        for (Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe recipe = it.next();
            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) continue;
            furnaceRecipes.put(furnaceRecipe.getInput().getType(), furnaceRecipe.getResult().getType());
        }
    }

    public static Map<Material, Integer> getBurnTimeList() {
        return burnTimeList;
    }

    public static Map<Material, Material> getFurnaceRecipes() {
        return furnaceRecipes;
    }
}
