package sir_draco.spinwheel.utils;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.SpinWheel;
import sir_draco.spinwheel.wheel.Wheel;
import sir_draco.spinwheel.wheel.WheelStats;

import java.util.*;

@SuppressWarnings("deprecation")
public class SpinUtils {
    public static final Random RANDOM = new Random();

    private SpinUtils() {
        // Utility class, no instantiation allowed
    }

    public static int generateAward(Player p, int type) {
        // Determine rarity
        double rarity;
        switch (type) {
            case 0 -> rarity = 0.1;
            case 1 -> rarity = 0.8;
            case 2 -> rarity = 0.95;
            case 3 -> rarity = 0.999;
            default -> rarity = Math.random();
        }

        Wheel wheel = SpinWheel.getInstance().getWheel();
        List<ItemStack> commonItems = SpinWheel.getInstance().getCommonItems();
        List<ItemStack> rareItems = SpinWheel.getInstance().getRareItems();
        List<ItemStack> epicItems = SpinWheel.getInstance().getEpicItems();
        List<ItemStack> legendaryItems = SpinWheel.getInstance().getLegendaryItems();
        Map<UUID, WheelStats> spinsStats = SpinWheel.getInstance().getSpinsStats();

        // Common
        if (rarity < 0.65) {
            dropItem(p, commonItems.get(SpinUtils.randomSlot(commonItems.size())), wheel.getCenter());
            return 0;
        }
        // Rare
        if (rarity < .90) {
            spinsStats.get(p.getUniqueId()).changeRare(1);
            dropItem(p, rareItems.get(SpinUtils.randomSlot(rareItems.size())), wheel.getCenter());
            return 1;
        }
        // Epic
        if (rarity < .99) {
            spinsStats.get(p.getUniqueId()).changeEpic(1);
            dropItem(p, epicItems.get(SpinUtils.randomSlot(epicItems.size())), wheel.getCenter());
            return 2;
        }
        // Legendary
        spinsStats.get(p.getUniqueId()).changeLegendary(1);
        dropItem(p, legendaryItems.get(SpinUtils.randomSlot(legendaryItems.size())), wheel.getCenter());
        return 3;
    }

    public static void dropItem(Player p, ItemStack item, Location loc) {
        Wheel wheel = SpinWheel.getInstance().getWheel();
        Item drop = wheel.getWorld().dropItem(loc.add(0.5, 1, 0.5), item);
        drop.setOwner(p.getUniqueId());
        wheel.setSpinning(false);
    }

    public static void fireworks(int type) {
        switch (type) {
            case 1 -> rareFireworks();
            case 2 -> epicFireworks();
            default -> legendaryFireworks();
        }
    }

    private static void legendaryFireworks() {
        Wheel wheel = SpinWheel.getInstance().getWheel();
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
        }.runTaskTimer(SpinWheel.getInstance(), 0, 20);
    }

    private static void epicFireworks() {
        Wheel wheel = SpinWheel.getInstance().getWheel();
        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.fromRGB(255, 0, 255));
        for (Location loc : wheel.getCorners()) {
            spawnFirework(loc.add(0, 1, 0), 0, FireworkEffect.Type.BALL, colors, true, true, null);
        }
        for (Location loc : wheel.getCardinalDirections()) {
            spawnFirework(loc.add(0, 1, 0), 1, FireworkEffect.Type.BALL, colors, true, true, null);
        }
    }

    private static void rareFireworks() {
        Wheel wheel = SpinWheel.getInstance().getWheel();
        for (Location loc : wheel.getCorners()) {
            ArrayList<Color> colors = new ArrayList<>();
            colors.add(Color.fromRGB(0, 0, 120));
            spawnFirework(loc.add(0, 1, 0), 0, FireworkEffect.Type.BALL, colors, false, true, null);
        }
    }

    public static void spawnFirework(Location loc, int power, FireworkEffect.Type type, List<Color> colors, boolean trail, boolean flicker, List<Color> fadeColors) {
        Firework fw = (Firework) SpinWheel.getInstance().getWheel().getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
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

    public static List<Color> getRainbowColors() {
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

    public static ItemStack fastFurnace(int type) {
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

    public static ItemStack enchantedBook(Enchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta == null) return book;
        meta.addStoredEnchant(enchant, level, true);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Returns true if there is a claim there
     */
    public static boolean checkForClaim(Player p, Location loc) {
        String noBuildReason = GriefPrevention.instance.allowBuild(p, loc);
        return (noBuildReason != null);
    }

    public static ItemStack diamondMax(Material mat, boolean alternative) {
        ItemStack item;
        if (mat.equals(Material.DIAMOND_PICKAXE)) {
            item = new ItemStack(Material.DIAMOND_PICKAXE);
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 8);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);

            if (alternative) item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
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

    public static int randomSlot(int max) {
        return RANDOM.nextInt(max);
    }
}
