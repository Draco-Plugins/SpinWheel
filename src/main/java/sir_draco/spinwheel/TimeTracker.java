package sir_draco.spinwheel;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

            if (plugin.getSpinsStats().get(p.getUniqueId()).getTime() >= effectiveTime) {
                plugin.getSpinsStats().get(p.getUniqueId()).setTime(0);
                plugin.getSpinsStats().get(p.getUniqueId()).changeSpins(1);
                int minutes = effectiveTime / 60;
                p.sendRawMessage(ChatColor.GREEN + "You just received a wheel spin for " + ChatColor.AQUA +
                         minutes + " Minutes " + ChatColor.GREEN + "of active play time");
                p.sendRawMessage(ChatColor.GREEN + "Head to spawn and use " + ChatColor.AQUA + "/spin ");
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }
    }
}
