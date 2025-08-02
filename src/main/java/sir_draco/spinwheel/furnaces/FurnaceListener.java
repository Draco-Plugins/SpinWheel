package sir_draco.spinwheel.furnaces;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.utils.FileUtils;
import sir_draco.spinwheel.utils.SpinUtils;
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
        // If the player is crouching and is trying to place a block do not open the furnace
        if (e.getPlayer().isSneaking() && e.getItem() != null && e.getItem().getType().isBlock()) return;
        // Make sure the event fires once
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        // Make sure they are allowed to open the furnace
        if (plugin.isGriefPreventionEnabled() && SpinUtils.checkForClaim(e.getPlayer(), e.getClickedBlock().getLocation())) return;
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
        if (plugin.isGriefPreventionEnabled() && SpinUtils.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) return;
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
        if (plugin.isGriefPreventionEnabled() && SpinUtils.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (!e.getBlockPlaced().getType().equals(Material.FURNACE)) return;
        placeCustomFurnace(e);
    }

    @EventHandler
    public void preventCustomFurnaceSmelting(FurnaceStartSmeltEvent e) {
        CustomFurnace furnace = getCustomFurnace(e.getBlock());
        if (furnace != null) {
            // Clear the furnace contents to prevent smelting
            Furnace furnaceBlock = (Furnace) e.getBlock().getState();
            furnaceBlock.getInventory().clear();
            furnaceBlock.update();
        }
    }

    @EventHandler
    public void handleHopperMovement(InventoryMoveItemEvent e) {
        handleHopperInputToFurnace(e);
        handleHopperOutputFromFurnace(e);
    }

    private void placeCustomFurnace(BlockPlaceEvent e) {
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

        CustomFurnace furnace = new CustomFurnace(location, model);
        plugin.getCustomFurnaces().add(furnace);
        plugin.getFurnaceIDs().put(location, plugin.getNextFurnaceID());

        new BukkitRunnable() {
            @Override
            public void run() {
                block.getState().setMetadata(SUPER_FURNACE, new FixedMetadataValue(plugin, model));
                furnace.setCanBreak(true);

                try {
                    FileUtils.saveFurnace(location, model, plugin.getNextFurnaceID(), plugin.getFurnaceData(),
                                          plugin.getFurnaceFile());
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
        newDrops.add(SpinUtils.fastFurnace(furnace.getSpeed()));

        // Get and drop any items left in the base furnace block's inventory
        Block block = e.getBlock();
        if (block.getState() instanceof Furnace furnaceBlock) {
            FurnaceInventory inv = furnaceBlock.getInventory();
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    newDrops.add(item);
                }
            }
            inv.clear(); // Clear the inventory to avoid duplication
            furnaceBlock.update();
        }

        for (ItemStack drop : newDrops) e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);

        int id = plugin.getFurnaceIDs().get(furnace.getLocation());
        try {
            FileUtils.removeFurnace(id, plugin.getFurnaceData(), plugin.getFurnaceFile(), plugin.getFurnaceIDs());
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
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), SpinUtils.fastFurnace(type));
    }

    private void handleHopperInputToFurnace(InventoryMoveItemEvent e) {
        if (!(e.getDestination().getHolder() instanceof Furnace furnaceBlock)) return;

        CustomFurnace customFurnace = getCustomFurnace(furnaceBlock.getBlock());
        if (customFurnace == null) return;

        if (!(e.getSource().getHolder() instanceof Hopper hopper)) return;

        ItemStack itemToMove = new ItemStack(e.getItem().getType(), 1);

        BlockFace hopperDirection = getRelativeDirection(hopper.getBlock().getLocation(),
                                                        furnaceBlock.getBlock().getLocation());

        boolean canMove = false;
        if (hopperDirection == BlockFace.UP) {
            // Check if this item can go into the smelting slot
            canMove = canAddToSmeltingSlot(customFurnace, itemToMove);
        } else if (isHorizontalDirection(hopperDirection)) {
            // Check if this item can go into the fuel slot
            canMove = CustomFurnaceChecker.getBurnTimeList().containsKey(itemToMove.getType()) &&
                     canAddToFuelSlot(customFurnace, itemToMove);
        }

        e.setCancelled(true);
        if (canMove) {
            // Add the item to the appropriate custom furnace slot
            if (hopperDirection == BlockFace.UP) {
                addToSmeltingSlot(customFurnace, itemToMove);
            } else if (isHorizontalDirection(hopperDirection)) {
                addToFuelSlot(customFurnace, itemToMove);
            }

            // Schedule the hopper inventory modification for next tick to avoid Spigot's temporary modification
            // Store a reference to the original item for verification to prevent duplication
            ItemStack originalEventItem = e.getItem().clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    removeOneFromHopperDelayed(hopper, originalEventItem);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    private void handleHopperOutputFromFurnace(InventoryMoveItemEvent e) {
        if (!(e.getSource().getHolder() instanceof Furnace furnaceBlock)) return;

        CustomFurnace customFurnace = getCustomFurnace(furnaceBlock.getBlock());
        if (customFurnace == null) return;

        e.setCancelled(true);

        if (!(e.getDestination().getHolder() instanceof Hopper destinationHopper)) return;

        BlockFace hopperDirection = getRelativeDirection(destinationHopper.getBlock().getLocation(),
                                                        furnaceBlock.getBlock().getLocation());

        if (hopperDirection == BlockFace.DOWN) {
            // Try to transfer result items first
            ItemStack result = customFurnace.getResult();
            if (result != null && result.getAmount() > 0) {
                transferResultToHopper(customFurnace, destinationHopper, result);
                return; // Prioritize result items over buckets
            }

            // If no result items, try to transfer empty buckets from fuel slot
            ItemStack fuel = customFurnace.getFuel();
            if (fuel != null && fuel.getType() == Material.BUCKET) {
                transferBucketToHopper(customFurnace, destinationHopper, fuel);
            }
        }
    }

    private void transferResultToHopper(CustomFurnace furnace, Hopper hopper, ItemStack result) {
        ItemStack outputItem = result.clone();
        outputItem.setAmount(1);

        if (canHopperAcceptItem(hopper, outputItem)) {
            addItemToHopper(hopper, outputItem);
            if (result.getAmount() == 1) {
                furnace.getInventory().setResult(null);
            } else {
                result.setAmount(result.getAmount() - 1);
                furnace.getInventory().setResult(result);
            }
        }
    }

    private void transferBucketToHopper(CustomFurnace furnace, Hopper hopper, ItemStack bucket) {
        ItemStack bucketItem = bucket.clone();
        bucketItem.setAmount(1);

        if (canHopperAcceptItem(hopper, bucketItem)) {
            addItemToHopper(hopper, bucketItem);
            if (bucket.getAmount() == 1) {
                furnace.getInventory().setFuel(null);
            } else {
                bucket.setAmount(bucket.getAmount() - 1);
                furnace.getInventory().setFuel(bucket);
            }
        }
    }

    private boolean isHorizontalDirection(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH ||
               face == BlockFace.EAST || face == BlockFace.WEST;
    }

    private BlockFace getRelativeDirection(Location from, Location to) {
        int dx = from.getBlockX() - to.getBlockX();
        int dy = from.getBlockY() - to.getBlockY();
        int dz = from.getBlockZ() - to.getBlockZ();

        if (dy > 0) return BlockFace.UP;
        if (dy < 0) return BlockFace.DOWN;
        if (dx < 0) return BlockFace.EAST;
        if (dx > 0) return BlockFace.WEST;
        if (dz > 0) return BlockFace.SOUTH;
        if (dz < 0) return BlockFace.NORTH;

        return BlockFace.SELF;
    }

    private boolean canAddToSmeltingSlot(CustomFurnace furnace, ItemStack item) {
        ItemStack current = furnace.getSmelting();
        if (current == null) return true;
        if (!current.getType().equals(item.getType())) return false;
        return current.getAmount() < current.getMaxStackSize();
    }

    private void addToSmeltingSlot(CustomFurnace furnace, ItemStack item) {
        ItemStack current = furnace.getSmelting();
        if (current == null) {
            furnace.getInventory().setSmelting(item.clone());
        } else {
            current.setAmount(current.getAmount() + item.getAmount());
            furnace.getInventory().setSmelting(current);
        }
    }

    private boolean canAddToFuelSlot(CustomFurnace furnace, ItemStack item) {
        ItemStack current = furnace.getFuel();
        if (current == null) return true;
        if (!current.getType().equals(item.getType())) return false;
        return current.getAmount() < current.getMaxStackSize();
    }

    private void addToFuelSlot(CustomFurnace furnace, ItemStack item) {
        ItemStack current = furnace.getFuel();
        if (current == null) {
            furnace.getInventory().setFuel(item.clone());
        } else {
            current.setAmount(current.getAmount() + item.getAmount());
            furnace.getInventory().setFuel(current);
        }
    }

    private boolean canHopperAcceptItem(Hopper hopper, ItemStack item) {
        for (ItemStack slot : hopper.getInventory().getContents()) {
            if (slot == null) return true;
            if (slot.getType().equals(item.getType()) && slot.getAmount() < slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void addItemToHopper(Hopper hopper, ItemStack item) {
        hopper.getInventory().addItem(item);
    }

    // Remove a single matching item from the hopper inventory (delayed execution)
    private void removeOneFromHopperDelayed(Hopper hopper, ItemStack originalEventItem) {
        var inv = hopper.getInventory();

        // Find the first stack that matches the original event item
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() == originalEventItem.getType()) {
                // Safety check: only remove if the stack has items (prevents duplication)
                if (stack.getAmount() > 0) {
                    if (stack.getAmount() > 1) {
                        ItemStack newStack = stack.clone();
                        newStack.setAmount(stack.getAmount() - 1);
                        inv.setItem(i, newStack);
                    } else {
                        inv.setItem(i, null);
                    }
                }
                return;
            }
        }

        Bukkit.getLogger().log(Level.WARNING, "[SpinWheel] Could not find item {0} in hopper for delayed removal",
                originalEventItem.getType());
    }

    public CustomFurnace getCustomFurnace(Block block) {
        Location loc = block.getLocation();
        for (CustomFurnace furnace : plugin.getCustomFurnaces()) {
            if (furnace.getLocation().equals(loc)) return furnace;
        }
        return null;
    }
}
