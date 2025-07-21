package sir_draco.spinwheel.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import sir_draco.spinwheel.SpinWheel;

public class NextSpinCommand implements CommandExecutor {

    private final SpinWheel plugin;

    public NextSpinCommand(SpinWheel plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("nextspin");
        if (command != null) command.setExecutor(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player p)) return false;
        if (!plugin.getSpinsStats().containsKey(p.getUniqueId())) {
            Bukkit.getLogger().warning("Player " + p.getName() + " does not have a spin tracker");
            return false;
        }
        int time = plugin.getSpinsStats().get(p.getUniqueId()).getTime();
        int totalTime = plugin.getEffectiveWaitTime(p.getUniqueId());
        int minutesLeft = (totalTime - time) / 60;
        int secondsLeft = (totalTime - time) % 60;
        p.sendRawMessage(ChatColor.GREEN + "You have " + ChatColor.AQUA + minutesLeft + " minutes " + secondsLeft
                + " seconds " + ChatColor.GREEN + "until your next spin");
        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        return true;
    }
}
