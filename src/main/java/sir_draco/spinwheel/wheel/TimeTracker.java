package sir_draco.spinwheel.wheel;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import sir_draco.spinwheel.SpinWheel;

public class TimeTracker extends BukkitRunnable {

    private final SpinWheel plugin;

    public TimeTracker(SpinWheel plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getSpinsStats().containsKey(p.getUniqueId())) continue;
            if (plugin.getEssentials().getUser(p).isAfk()) continue;
            plugin.getSpinsStats().get(p.getUniqueId()).increaseTime();

            int effectiveTime = plugin.getEffectiveWaitTime(p.getUniqueId());
            WheelStats playerStats = plugin.getSpinsStats().get(p.getUniqueId());

            if (playerStats.getTime() >= effectiveTime) {
                playerStats.setTime(0);

                // Try to add a spin, but check if player is at max limit
                if (playerStats.tryAddSpins(1)) {
                    int minutes = effectiveTime / 60;
                    p.sendRawMessage(ChatColor.GREEN + "You just received a wheel spin for " + ChatColor.AQUA +
                             minutes + " Minutes " + ChatColor.GREEN + "of active play time");
                    p.sendRawMessage(ChatColor.GREEN + "Head to spawn and use " + ChatColor.AQUA + "/spin ");
                    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                } else {
                    // Player is at max spins - notify them
                    int minutes = effectiveTime / 60;
                    p.sendRawMessage(ChatColor.YELLOW + "You earned a spin for " + ChatColor.AQUA +
                             minutes + " Minutes " + ChatColor.YELLOW + "of play time, but you're at the maximum limit of " +
                             ChatColor.RED + WheelStats.getMaxSpins() + " spins!");
                    p.sendRawMessage(ChatColor.YELLOW + "Use some spins first before earning more!");
                    p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                }
            }
        }
    }
}
