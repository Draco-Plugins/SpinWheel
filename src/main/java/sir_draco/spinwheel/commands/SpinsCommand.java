package sir_draco.spinwheel.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import sir_draco.spinwheel.SpinWheel;

public class SpinsCommand implements CommandExecutor {

    private final SpinWheel plugin;

    public SpinsCommand(SpinWheel plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("spins");
        if (command != null) command.setExecutor(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player p)) return false;
        if (strings.length == 1 && p.hasPermission("wheel.admin")) {
            Player player = plugin.getServer().getPlayer(strings[0]);
            if (player == null) {
                p.sendRawMessage(ChatColor.RED + "This player is not online");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return true;
            }
            p.sendRawMessage(ChatColor.GREEN + player.getDisplayName() + " has " + ChatColor.AQUA + plugin.getSpinsStats().get(player.getUniqueId()).getSpins() + ChatColor.GREEN + " spins");
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }
        p.sendRawMessage(ChatColor.GREEN + "You have " + ChatColor.AQUA + plugin.getSpinsStats().get(p.getUniqueId()).getSpins() + ChatColor.GREEN + " spins");
        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        return true;
    }
}
