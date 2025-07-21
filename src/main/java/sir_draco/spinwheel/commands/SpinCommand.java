package sir_draco.spinwheel.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import sir_draco.spinwheel.SpinTimer;
import sir_draco.spinwheel.SpinWheel;
import sir_draco.spinwheel.WheelStats;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SpinCommand implements CommandExecutor {

    public static final String NOW_HAVE = "You now have ";
    public static final String RECEIVED = "You have received ";
    public static final String SPINS_REMAINING = " spins remaining";
    private final SpinWheel plugin;

    public SpinCommand(SpinWheel plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("spin");
        if (command != null) command.setExecutor(this);
    }
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player p)) return false;
        // Check that they have a spin available
        if (plugin.getSpinsStats().get(p.getUniqueId()).getSpins() == 0 && !plugin.getOpSpins().contains(p)) {
            p.sendRawMessage(ChatColor.RED + "You have no spins available");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return true;
        }

        // Check that wheel is active on the server
        if (plugin.getWheel() == null) {
            p.sendRawMessage(ChatColor.RED + "The wheel is not currently active");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return true;
        }

        // Check that it isn't close to a restart
        if (playerCanSpin(p)) return true;

        // Check if the player wants to use all their spins at once
        if (strings.length == 1 && strings[0].equalsIgnoreCase("all")) {
            WheelStats stats = plugin.getSpinsStats().get(p.getUniqueId());
            int spins = stats.getSpins();
            stats.changeSpins(-spins);
            p.sendRawMessage(ChatColor.GREEN + NOW_HAVE + ChatColor.AQUA + plugin.getSpinsStats().get(p.getUniqueId()).getSpins()
                    + ChatColor.GREEN + SPINS_REMAINING);
            p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, 2, 1);
            plugin.getWheel().setSpinning(true);
            SpinTimer spin = new SpinTimer(plugin, p, "all", spins);
            spin.runTaskTimer(plugin, 40, 1);
            return true;
        }

        // Check if they want their stats
        if (strings.length == 1 && strings[0].equalsIgnoreCase("stats")) {
            WheelStats stats = plugin.getSpinsStats().get(p.getUniqueId());
            p.sendRawMessage(ChatColor.GREEN + RECEIVED + ChatColor.AQUA + stats.getRare() + ChatColor.GREEN + " rare rewards");
            p.sendRawMessage(ChatColor.GREEN + RECEIVED + ChatColor.AQUA + stats.getEpic() + ChatColor.GREEN + " epic rewards");
            p.sendRawMessage(ChatColor.GREEN + RECEIVED + ChatColor.AQUA + stats.getLegendary() + ChatColor.GREEN + " legendary rewards");
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }

        // Check if they want to gamble a specific number of spins
        if (specificSpinCount(strings, p)) return true;

        // Check if operator doesn't want spin count to change
        if (p.hasPermission("wheel.admin") && plugin.getOpSpins().contains(p)) {
            plugin.getSpinsStats().get(p.getUniqueId()).changeSpins(1);
        }

        // Spin the wheel
        plugin.getSpinsStats().get(p.getUniqueId()).changeSpins(-1);
        p.sendRawMessage(ChatColor.GREEN + NOW_HAVE + ChatColor.AQUA + plugin.getSpinsStats().get(p.getUniqueId()).getSpins()
                + ChatColor.GREEN + SPINS_REMAINING);
        p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, 2, 1);
        plugin.getWheel().setSpinning(true);

        // Check for specified reward
        SpinTimer spin;
        if (strings.length == 1 && p.hasPermission("wheel.admin")) spin = new SpinTimer(plugin, p, strings[0], 0);
        else spin = new SpinTimer(plugin, p, "none", 0); // Times can be 0 because it isn't used

        spin.runTaskTimer(plugin, 40, 1);
        return true;
    }

    private boolean specificSpinCount(String[] strings, Player p) {
        if (strings.length == 1) {
            try {
                int requestedSpins = Integer.parseInt(strings[0]);
                if (requestedSpins <= 0) {
                    p.sendRawMessage(ChatColor.RED + "Please enter a positive number of spins");
                    p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                WheelStats stats = plugin.getSpinsStats().get(p.getUniqueId());
                int availableSpins = stats.getSpins();

                // Check if player has enough spins (unless they're an admin with infinite spins)
                if (!plugin.getOpSpins().contains(p) && requestedSpins > availableSpins) {
                    p.sendRawMessage(ChatColor.RED + "You only have " + ChatColor.AQUA + availableSpins + ChatColor.RED + " spins available");
                    p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                // Deduct the spins
                stats.changeSpins(-requestedSpins);
                p.sendRawMessage(ChatColor.GREEN + NOW_HAVE + ChatColor.AQUA + plugin.getSpinsStats().get(p.getUniqueId()).getSpins()
                        + ChatColor.GREEN + SPINS_REMAINING);
                p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, 2, 1);
                plugin.getWheel().setSpinning(true);
                SpinTimer spin = new SpinTimer(plugin, p, "multiple", requestedSpins);
                spin.runTaskTimer(plugin, 40, 1);
                return true;
            } catch (NumberFormatException e) {
                // Not a number, continue to normal processing
                p.sendRawMessage(ChatColor.RED + "Invalid number of spins specified. Please enter a valid number.");
            }
        }
        return false;
    }

    private boolean playerCanSpin(Player p) {
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String[] formattedTime = currentTime.format(formatter).split(":");
        int hour = Integer.parseInt(formattedTime[0]);
        int minutes = Integer.parseInt(formattedTime[1]);
        if (hour == 8 && minutes == 59) {
            p.sendRawMessage(ChatColor.RED + "Wait until after the server restart!");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return true;
        }

        if (plugin.getWheel().isSpinning()) {
            p.sendRawMessage(ChatColor.RED + "The wheel is already spinning");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return true;
        }

        // Check if the player is close enough
        Location wheel = plugin.getWheel().getCenter();
        Location pLoc = p.getLocation();
        double dist = Math.sqrt(Math.pow(wheel.getX() - pLoc.getX(), 2) + Math.pow(wheel.getY() - pLoc.getY(), 2) + Math.pow(wheel.getZ() - pLoc.getZ(), 2));
        if (Math.abs(dist) > 20) {
            p.sendRawMessage(ChatColor.RED + "You are too far away from the wheel");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return true;
        }
        return false;
    }
}
