package sir_draco.spinwheel.furnaces;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.SpinWheel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings ("deprecation")
public class FurnaceListener implements Listener {
    public static final String SUPER_FURNACE = "superFurnace";

    private final SpinWheel plugin;
    private final ArrayList<FurnaceInventory> openCustomFurnaces = new ArrayList<>();

    public FurnaceListener(SpinWheel plugin) {
        this.plugin = plugin;
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

    @EventHandler (ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent e) {
        if (!e.getBlock().getType().equals(Material.FURNACE)) return;
        if (plugin.isGriefPreventionEnabled() && plugin.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) return;
        CustomFurnace furnace = getCustomFurnace(e.getBlock());

        // If we found a CustomFurnace object, handle it through the normal custom furnace removal
        if (furnace != null) {
            if (!furnace.canBreak()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "Slow down there!");
                return;
            }
            removeCustomFurnace(e, furnace);
            return;
        }

        // If no CustomFurnace object, but it has super furnace metadata, handle it as a metadata-only furnace
        checkForSuperFurnace(e);
    }

    @EventHandler
    public void placeFurnaceEvent(BlockPlaceEvent e) {
        if (plugin.isGriefPreventionEnabled() && plugin.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (!e.getBlockPlaced().getType().equals(Material.FURNACE)) return;
        placeCustomFurnace(e);
    }

    private void placeCustomFurnace(BlockPlaceEvent e) {
        Bukkit.getLogger().log(Level.INFO,"[SpinWheel] Placing furnace");
        if (e.getItemInHand().getItemMeta() == null) return;
        if (!e.getItemInHand().getItemMeta().hasCustomModelData()) return;
        int model = e.getItemInHand().getItemMeta().getCustomModelData();
        if (model < 1 || model > 4) return;

        Block block = e.getBlockPlaced();
        Location location = block.getLocation();

        // Check if a furnace already exists at this location
        if (plugin.hasFurnaceAt(location)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "A custom furnace already exists at this location!");
            return;
        }

        Bukkit.getLogger().log(Level.INFO, "[SpinWheel] Placing custom furnace with model {0}", model);

        CustomFurnace furnace = new CustomFurnace(location, model);
        plugin.getCustomFurnaces().add(furnace);
        plugin.getFurnaceIDs().put(location, plugin.getNextFurnaceID());

        new BukkitRunnable() {
            @Override
            public void run() {
                block.getState().setMetadata(SUPER_FURNACE, new FixedMetadataValue(plugin, model));
                furnace.setCanBreak(true);

                try {
                    plugin.saveFurnace(location, model, plugin.getNextFurnaceID());
                    plugin.incrementNextFurnaceID();
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("[SpinWheel] Failed to save furnace at " + location + ": " + ex.getMessage());
                    e.getPlayer().sendMessage(ChatColor.RED + "Failed to place custom furnace. Please try again.");

                    // Clean up the failed furnace
                    plugin.getCustomFurnaces().remove(furnace);
                    plugin.getFurnaceIDs().remove(location);
                }
            }
        }.runTaskLater(plugin, 5);
    }

    private void removeCustomFurnace(BlockBreakEvent e, CustomFurnace furnace) {
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
            Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Failed to remove furnace with ID: {0}", id);
        }
    }

    private void checkForSuperFurnace(BlockBreakEvent e) {
        if (!e.getBlock().getState().hasMetadata(SUPER_FURNACE)) return;
        List<MetadataValue> metaList = e.getBlock().getState().getMetadata(SUPER_FURNACE);
        if (metaList.isEmpty()) return;
        int type = metaList.getFirst().asInt();
        e.setDropItems(false);
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), plugin.fastFurnace(type));
    }

    public CustomFurnace getCustomFurnace(Block block) {
        Location loc = block.getLocation();
        for (CustomFurnace furnace : plugin.getCustomFurnaces()) {
            if (furnace.getLocation().equals(loc)) return furnace;
        }
        return null;
    }
}
