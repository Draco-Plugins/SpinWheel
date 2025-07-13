package sir_draco.spinwheel;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

    private final SpinWheel plugin;
    private final ArrayList<FurnaceInventory> openCustomFurnaces = new ArrayList<>();

    private ProtectedRegion region;

    public PlayerListener(SpinWheel plugin) {
        this.plugin = plugin;
        this.region = null;
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) return;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        World world = plugin.getServer().getWorld("world");
        if (world == null) {
            Bukkit.getLogger().warning("World - 'world' not found");
            return;
        }
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) {
            Bukkit.getLogger().warning("RegionManager not found");
            return;
        }
        this.region = regions.getRegion("spawn");
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        if (!plugin.getSpins().containsKey(e.getPlayer().getUniqueId())) {
            plugin.loadSpins(e.getPlayer());
            return;
        }
        WheelStats stats = new WheelStats(1, 0, 0, 0, 0);
        plugin.getSpins().put(e.getPlayer().getUniqueId(), stats);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        try {
            plugin.saveSpins(e.getPlayer());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        plugin.getSpins().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void spawnHorse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack hay = p.getInventory().getItemInMainHand();
        if (!hay.getType().equals(Material.HAY_BLOCK)) return;
        ItemMeta meta = hay.getItemMeta();
        if (meta == null) return;
        if (!meta.hasCustomModelData()) return;
        if (meta.getCustomModelData() != 1) return;
        if (region != null
                && region.contains(BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))) {
            p.sendRawMessage(ChatColor.RED + "You are in spawn");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            e.setCancelled(true);
            return;
        }
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) return;

        hay.setAmount(1);
        e.setCancelled(true);
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        Entity ent = p.getWorld().spawnEntity(clickedBlock.getLocation().clone().add(0, 1, 0), EntityType.HORSE);
        Horse horse = (Horse) ent;
        horse.setJumpStrength(1);
        AttributeInstance speed = horse.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(.3375d);
        AttributeInstance health = horse.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) health.setBaseValue(30d);
        horse.setOwner(p);
        horse.setTamed(true);
        p.getInventory().remove(hay);
        p.sendRawMessage(ChatColor.GREEN + "Horse spawned and tamed successfully!");
    }

    @EventHandler
    public void openCustomFurnace(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (!e.getClickedBlock().getType().equals(Material.FURNACE)) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (plugin.isGriefPreventionEnabled() && plugin.checkForClaim(e.getPlayer(), e.getClickedBlock().getLocation())) return;
        CustomFurnace furnace = getCustomFurnace(e.getClickedBlock());
        if (furnace == null) return;
        e.setCancelled(true);
        furnace.openFurnace(e.getPlayer());
        openCustomFurnaces.add(furnace.getInventory());
    }

    @EventHandler
    public void closeCustomFurnace(InventoryCloseEvent e) {
        if (!(e.getInventory() instanceof FurnaceInventory inv)) return;
        if (!openCustomFurnaces.contains(inv)) return;
        openCustomFurnaces.remove(inv);
    }

    @EventHandler
    public void takeOutCustomFurnaceItems(InventoryClickEvent e) {
        if (!(e.getClickedInventory() instanceof FurnaceInventory inv)) return;
        if (!openCustomFurnaces.contains(inv)) return;
        // If the player is trying to take out the result give them xp based on the number of items
        if (e.getSlot() == 2) {
            ItemStack result = inv.getResult();
            if (result == null) return;
            double xp = result.getAmount() * 0.35;
            Player p = (Player) e.getWhoClicked();
            p.giveExp((int) xp);
        }
    }

    @EventHandler
    public void dragOutCustomFurnaceItems(InventoryDragEvent e) {
        if (!(e.getInventory() instanceof FurnaceInventory inv)) return;
        if (!openCustomFurnaces.contains(inv)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void placeCustomBlock(BlockPlaceEvent e) {
        if (plugin.isGriefPreventionEnabled() && plugin.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }
        if (e.getBlockPlaced().getType().equals(Material.SPAWNER)) {
            if (e.getItemInHand().getItemMeta() == null) return;
            if (!e.getItemInHand().getItemMeta().hasCustomModelData()) return;
            if (region != null
                    && region.contains(BlockVector3.at(e.getBlockPlaced().getX(), e.getBlockPlaced().getY(), e.getBlockPlaced().getZ()))) {
                e.setCancelled(true);
                return;
            }
            int model = e.getItemInHand().getItemMeta().getCustomModelData();
            if (model >= 100) {
                CreatureSpawner spawner = (CreatureSpawner) e.getBlockPlaced().getState();
                spawner.setSpawnedType(plugin.getEntityTypes().get(model - 100));
                spawner.setDelay(50);
                spawner.setSpawnCount(4);
                spawner.setSpawnRange(4);
                spawner.setMinSpawnDelay(50);
                spawner.setMaxSpawnDelay(50);
                spawner.setMaxNearbyEntities(6);
                spawner.setRequiredPlayerRange(16);
                spawner.update();
            }
            CreatureSpawner spawner = (CreatureSpawner) e.getBlockPlaced().getState();
            spawner.setSpawnedType(plugin.getEntityTypes().get(model));
            spawner.update();
        }
        else if (e.getBlockPlaced().getType().equals(Material.FURNACE)) {
            if (e.getItemInHand().getItemMeta() == null) return;
            if (!e.getItemInHand().getItemMeta().hasCustomModelData()) return;
            if (region != null && region.contains(BlockVector3.at(e.getBlockPlaced().getX(), e.getBlockPlaced().getY(), e.getBlockPlaced().getZ()))) {
                e.setCancelled(true);
                return;
            }
            int model = e.getItemInHand().getItemMeta().getCustomModelData();
            if (model < 1 || model > 4) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    Block block = e.getBlockPlaced();
                    block.getState().setMetadata("superFurnace", new FixedMetadataValue(plugin, model));
                    plugin.getCustomFurnaces().add(new CustomFurnace(block.getLocation(), model));
                    plugin.getFurnaceIDs().put(block.getLocation(), plugin.getNextFurnaceID());

                    try {
                        plugin.saveFurnace(block.getLocation(), model, plugin.getNextFurnaceID());
                        plugin.incrementNextFurnaceID();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }.runTaskLater(plugin, 5);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.FURNACE)) {
            if (plugin.isGriefPreventionEnabled() && plugin.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) return;
            CustomFurnace furnace;
            furnace = getCustomFurnace(e.getBlock());

            if (furnace == null)  {
                if (!e.getBlock().getState().hasMetadata("superFurnace")) return;
                int type = e.getBlock().getState().getMetadata("superFurnace").getFirst().asInt();
                e.setDropItems(false);
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), plugin.fastFurnace(type));
                return;
            }

            if (!plugin.getFurnaceIDs().containsKey(furnace.getLocation())) return;
            e.setDropItems(false);
            plugin.getCustomFurnaces().remove(furnace);
            ArrayList<ItemStack> newDrops = new ArrayList<>();
            if (furnace.getFuel() != null) newDrops.add(furnace.getFuel());
            if (furnace.getSmelting() != null) newDrops.add(furnace.getSmelting());
            if (furnace.getResult() != null) newDrops.add(furnace.getResult());
            newDrops.add(plugin.fastFurnace(furnace.getSpeed()));
            for (ItemStack drop : newDrops) e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);

            int id = plugin.getFurnaceIDs().get(furnace.getLocation());
            try {
                plugin.removeFurnace(id);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        if (plugin.getWheel() == null) return;
        if (plugin.getWheel().getLocations().contains(e.getBlock().getLocation())) e.setCancelled(true);
    }

    public CustomFurnace getCustomFurnace(Block block) {
        Location loc = block.getLocation();
        for (CustomFurnace furnace : plugin.getCustomFurnaces()) {
            if (furnace.getLocation().equals(loc)) return furnace;
        }
        return null;
    }
}
