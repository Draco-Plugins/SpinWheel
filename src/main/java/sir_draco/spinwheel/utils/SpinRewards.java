package sir_draco.spinwheel.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import sir_draco.spinwheel.SpinWheel;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class SpinRewards {
    private SpinRewards() {
        // Prevent instantiation
    }

    public static void makeCommonList(List<ItemStack> list) {
        list.add(new ItemStack(Material.BREAD, 16));
        list.add(new ItemStack(Material.COOKED_BEEF, 16));
        list.add(new ItemStack(Material.COOKED_PORKCHOP, 16));
        list.add(new ItemStack(Material.COOKED_SALMON, 16));
        list.add(new ItemStack(Material.CARROT, 16));
        list.add(new ItemStack(Material.POTATO, 16));
        list.add(new ItemStack(Material.BEETROOT, 16));
        list.add(new ItemStack(Material.HAY_BLOCK, 16));
        list.add(new ItemStack(Material.BAMBOO, 16));
        list.add(new ItemStack(Material.SUGAR_CANE, 16));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.SPRUCE_LOG, 64));
        list.add(new ItemStack(Material.BIRCH_LOG, 64));
        list.add(new ItemStack(Material.JUNGLE_LOG, 64));
        list.add(new ItemStack(Material.ACACIA_LOG, 64));
        list.add(new ItemStack(Material.DARK_OAK_LOG, 64));
        list.add(new ItemStack(Material.MANGROVE_LOG, 64));
        list.add(new ItemStack(Material.CHERRY_LOG, 64));
        list.add(new ItemStack(Material.CRIMSON_STEM, 64));
        list.add(new ItemStack(Material.WARPED_STEM, 64));
        list.add(new ItemStack(Material.ROTTEN_FLESH, 16));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.GUNPOWDER, 16));
        list.add(new ItemStack(Material.BONE, 16));
        list.add(new ItemStack(Material.SPIDER_EYE, 16));
        list.add(new ItemStack(Material.OAK_SAPLING, 4));
        list.add(new ItemStack(Material.SPRUCE_SAPLING, 4));
        list.add(new ItemStack(Material.BIRCH_SAPLING, 4));
        list.add(new ItemStack(Material.JUNGLE_SAPLING, 4));
        list.add(new ItemStack(Material.ACACIA_SAPLING, 4));
        list.add(new ItemStack(Material.DARK_OAK_SAPLING, 4));
        list.add(new ItemStack(Material.MANGROVE_PROPAGULE, 4));
        list.add(new ItemStack(Material.CHERRY_SAPLING, 4));
        list.add(new ItemStack(Material.CACTUS, 4));
        list.add(new ItemStack(Material.DIRT, 1));
        list.add(new ItemStack(Material.LEATHER_HORSE_ARMOR, 1));
        list.add(new ItemStack(Material.DIAMOND, 5));
        list.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        list.add(new ItemStack(Material.IRON_INGOT, 24));
        list.add(new ItemStack(Material.NAME_TAG, 2));
    }

    public static void makeRareList(List<ItemStack> list) {
        if (SpinWheel.getInstance().isEndLoot()) {
            list.add(new ItemStack(Material.SHULKER_BOX, 1));
            ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
            FireworkMeta metaData = (FireworkMeta) firework.getItemMeta();
            if (metaData == null) return;
            metaData.setPower(3);
            firework.setItemMeta(metaData);
            list.add(firework);
        }
        list.add(new ItemStack(Material.DIAMOND_AXE, 1));
        list.add(new ItemStack(Material.DIAMOND_PICKAXE, 1));
        list.add(new ItemStack(Material.DIAMOND_SWORD, 1));
        list.add(new ItemStack(Material.DIAMOND_SHOVEL, 1));
        list.add(new ItemStack(Material.DIAMOND_HOE, 1));
        list.add(new ItemStack(Material.DIAMOND_HELMET, 1));
        list.add(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
        list.add(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
        list.add(new ItemStack(Material.DIAMOND_BOOTS, 1));
        list.add(new ItemStack(Material.ANCIENT_DEBRIS, 3));
        list.add(new ItemStack(Material.WITHER_SKELETON_SKULL, 1));
        list.add(new ItemStack(Material.IRON_HORSE_ARMOR, 1));
        list.add(new ItemStack(Material.GOLDEN_HORSE_ARMOR, 1));
        list.add(new ItemStack(Material.SADDLE, 1));
        list.add(SpinUtils.enchantedBook(Enchantment.UNBREAKING, 3));
        list.add(new ItemStack(Material.GOLDEN_APPLE, 4));
        list.add(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1));
    }

    public static void makeEpicList(List<ItemStack> list, List<EntityType> entityTypes) {
        if (SpinWheel.getInstance().isEndLoot()) list.add(new ItemStack(Material.ELYTRA, 1));
        list.add(SpinUtils.fastFurnace(1));
        list.add(SpinUtils.fastFurnace(2));
        list.add(SpinUtils.fastFurnace(3));

        list.add(getSpawner(entityTypes));

        list.add(new ItemStack(Material.WITHER_SKELETON_SKULL, 3));
        list.add(new ItemStack(Material.TRIDENT, 1));
        list.add(new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        list.add(new ItemStack(Material.HEART_OF_THE_SEA, 1));
        list.add(SpinUtils.enchantedBook(Enchantment.MENDING, 1));
        list.add(SpinUtils.enchantedBook(Enchantment.SILK_TOUCH, 1));
        list.add(SpinUtils.enchantedBook(Enchantment.FORTUNE, 3));
        list.add(SpinUtils.enchantedBook(Enchantment.EFFICIENCY, 5));
        list.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        list.add(new ItemStack(Material.NETHERITE_INGOT, 3));
        list.add(new ItemStack(Material.MACE, 1));

        ItemStack horseBale = new ItemStack(Material.HAY_BLOCK, 1);
        ItemMeta meta = horseBale.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Spawns a horse with max stats (vanilla)");
        lore.add(ChatColor.RED + "You can't use it in spawn");
        if (meta == null) return;
        meta.setLore(lore);
        meta.setCustomModelData(1);
        horseBale.setItemMeta(meta);
        list.add(horseBale);
    }

    public static void makeLegendaryList(List<ItemStack> list, List<EntityType> entityTypes) {
        if (SpinWheel.getInstance().isEndLoot()) {
            ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
            elytra.addUnsafeEnchantment(Enchantment.UNBREAKING, 7);
            list.add(elytra);
        }
        list.add(SpinUtils.fastFurnace(4));

        list.add(getSuperSpawner(entityTypes, 3));

        list.add(SpinUtils.diamondMax(Material.DIAMOND_PICKAXE, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_AXE, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_SHOVEL, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_SWORD, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_HELMET, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_CHESTPLATE, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_LEGGINGS, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_BOOTS, false));
        list.add(SpinUtils.diamondMax(Material.BOW, false));
        list.add(SpinUtils.diamondMax(Material.DIAMOND_PICKAXE, true));
    }

    public static ItemStack getSpawner(List<EntityType> entityTypes) {
        int rand = SpinUtils.randomSlot(entityTypes.size());
        ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = spawner.getItemMeta();
        if (meta == null) return spawner;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + SpinUtils.makeReadableName(entityTypes.get(rand).toString()) + " Spawner");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "YOU CAN NOT PICK THIS UP ONCE YOU PUT IT DOWN!");
        meta.setCustomModelData(rand);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        spawner.setItemMeta(meta);
        return spawner;
    }

    public static ItemStack getSuperSpawner(List<EntityType> entityTypes, int amount) {
        int rand = SpinUtils.randomSlot(entityTypes.size());
        ItemStack spawner = new ItemStack(Material.SPAWNER, amount);
        ItemMeta meta = spawner.getItemMeta();
        if (meta == null) return spawner;
        meta.setDisplayName(ChatColor.GOLD + SpinUtils.makeReadableName(entityTypes.get(rand).toString()) + " Super Spawner");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "YOU CAN NOT PICK THIS UP ONCE YOU PUT IT DOWN!");
        meta.setCustomModelData(rand + 100);
        meta.setLore(lore);
        spawner.setItemMeta(meta);
        return spawner;
    }
}
