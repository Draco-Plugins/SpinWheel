package sir_draco.spinwheel;

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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {
    private final SpinWheel plugin;

    public PlayerListener(SpinWheel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        if (!plugin.getSpinsStats().containsKey(e.getPlayer().getUniqueId())) {
            plugin.loadSpins(e.getPlayer());
            return;
        }
        WheelStats stats = new WheelStats(1, 0, 0, 0, 0);
        plugin.getSpinsStats().put(e.getPlayer().getUniqueId(), stats);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        try {
            plugin.saveSpins(e.getPlayer());
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Failed to save spins for player: " + e.getPlayer().getName());
        }

        plugin.getSpinsStats().remove(e.getPlayer().getUniqueId());
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
    public void placeCustomBlock(BlockPlaceEvent e) {
        if (plugin.isGriefPreventionEnabled() && plugin.checkForClaim(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (!e.getBlockPlaced().getType().equals(Material.SPAWNER)) return;
        placeCustomSpawner(e);
    }

    private void placeCustomSpawner(BlockPlaceEvent e) {
        if (e.getItemInHand().getItemMeta() == null) return;
        if (!e.getItemInHand().getItemMeta().hasCustomModelData()) return;
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

    @EventHandler (ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent e) {
        if (plugin.getWheel() == null) return;
        if (plugin.getWheel().getLocations().contains(e.getBlock().getLocation())) e.setCancelled(true);
    }
}
