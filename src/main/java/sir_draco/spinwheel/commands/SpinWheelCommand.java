package sir_draco.spinwheel.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sir_draco.spinwheel.SpinWheel;
import sir_draco.spinwheel.Wheel;

import java.io.File;

public class SpinWheelCommand implements CommandExecutor {

    private final SpinWheel plugin;

    public SpinWheelCommand(SpinWheel plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("spinwheel");
        if (command != null) command.setExecutor(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player p)) {
            if (strings[0].equalsIgnoreCase("givespin")) {
                if (strings.length < 3) return false;

                Player target = null;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!player.getName().equalsIgnoreCase(strings[1])) continue;
                    target = player;
                }
                if (target == null) return false;

                int amount;
                try {
                    amount = Integer.parseInt(strings[2]);
                }
                catch (Exception e) {
                    return false;
                }

                plugin.getSpinsStats().get(target.getUniqueId()).changeSpins(amount);
                target.sendRawMessage(ChatColor.GREEN + "You have just received " + ChatColor.AQUA + amount
                        + ChatColor.GREEN + " wheel spins");
                target.playSound(target, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                return true;
            }
            return false;
        }

        if (strings.length == 0) {
            p.sendRawMessage(ChatColor.RED + "Usage: /spinwheel <createwheel|removewheel|endloot|settime|getreward|" +
                    "givespin|opspin|resetstats|superfurnace>");
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return true;
        }

        if (strings[0].equalsIgnoreCase("createwheel")) {
            if (plugin.getWheel() != null) plugin.getWheel().remove();
            plugin.setWheel(new Wheel(p.getLocation().getBlock().getLocation()));
            plugin.saveWheel();
            p.sendRawMessage(ChatColor.GREEN + "Wheel has been created");
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("removewheel")) {
            if (plugin.getWheel() == null) {
                p.sendRawMessage(ChatColor.RED + "There is no active wheel to remove");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }
            else {
                p.sendRawMessage(ChatColor.GREEN + "Wheel has been removed");
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            plugin.getWheel().remove();
            plugin.setWheel(null);
            plugin.getConfig().set("Wheel", null);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("endloot")) {
            boolean endLoot = !plugin.isEndLoot();
            plugin.setEndLoot(endLoot);
            File file = new File(plugin.getDataFolder(), "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("EndLoot", endLoot);
            try {
                config.save(file);
            } catch (Exception e) {
                p.sendRawMessage(ChatColor.RED + "Error saving end loot boolean");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            p.sendRawMessage(ChatColor.GREEN + "End loot has been set to " + ChatColor.AQUA + endLoot);
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("settime")) {
            if (strings.length < 2) {
                p.sendRawMessage(ChatColor.RED + "Usage: /spinwheel settime <time>");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            int time;
            try {
                time = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                p.sendRawMessage(ChatColor.RED + "Invalid Number");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            File file = new File(plugin.getDataFolder(), "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("Time", time);
            try {
                config.save(file);
                plugin.reloadConfig(); // Reload config so new time is used immediately
            } catch (Exception e) {
                p.sendRawMessage(ChatColor.RED + "Error saving time");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            p.sendRawMessage(ChatColor.GREEN + "Time has been set to " + ChatColor.AQUA + time);
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("getreward")) {
            if (strings.length < 2) {
                double chance = Math.random();
                if (chance < 0.65) plugin.dropItem(p, getCommon(p), p.getLocation());
                else if (chance < 0.9) plugin.dropItem(p, getRare(p), p.getLocation());
                else if (chance < 0.99) plugin.dropItem(p, getEpic(p), p.getLocation());
                else plugin.dropItem(p, getLegendary(p), p.getLocation());
                return true;
            }

            if (strings[1].equalsIgnoreCase("common")) plugin.dropItem(p, getCommon(p), p.getLocation());
            else if (strings[1].equalsIgnoreCase("rare")) plugin.dropItem(p, getRare(p), p.getLocation());
            else if (strings[1].equalsIgnoreCase("epic")) plugin.dropItem(p, getEpic(p), p.getLocation());
            else if (strings[1].equalsIgnoreCase("legendary")) plugin.dropItem(p, getLegendary(p), p.getLocation());
            else {
                p.sendRawMessage(ChatColor.RED + "Usage: /spinwheel getreward <common|rare|epic|legendary>");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }
            return true;
        }
        else if (strings[0].equalsIgnoreCase("givespin")) {
            if (strings.length < 3) {
                p.sendRawMessage(ChatColor.RED + "Usage: /spinwheel givespin <player> <amount>");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            Player target = null;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.getName().equalsIgnoreCase(strings[1])) continue;
                target = player;
            }
            if (target == null) {
                p.sendRawMessage(ChatColor.RED + "That player isn't online or doesn't exist");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            int amount;
            try {
                amount = Integer.parseInt(strings[2]);
            }
            catch (Exception e) {
                p.sendRawMessage(ChatColor.RED + "That is not a number");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            plugin.getSpinsStats().get(target.getUniqueId()).changeSpins(amount);
            target.sendRawMessage(ChatColor.GREEN + "You have just received " + ChatColor.AQUA + amount
                    + ChatColor.GREEN + " wheel spins");
            target.playSound(target, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            p.sendRawMessage(ChatColor.GREEN + "Successfully changed " + ChatColor.AQUA + strings[1] + ChatColor.GREEN + " spins");
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("opspin")) {
            if (plugin.getOpSpins().contains(p)) {
                plugin.removeOpSpins(p);
                p.sendRawMessage(ChatColor.YELLOW + "You are no longer spinning as an operator");
            }
            else {
                plugin.addOpSpins(p);
                p.sendRawMessage(ChatColor.YELLOW + "You are now spinning as an operator");
            }
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("resetstats")) {
            if (strings.length < 2) {
                p.sendRawMessage(ChatColor.RED + "Usage: /spinwheel resetstats <player>");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            Player target = null;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.getName().equalsIgnoreCase(strings[1])) continue;
                target = player;
            }
            if (target == null) {
                p.sendRawMessage(ChatColor.RED + "That player isn't online or doesn't exist");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            plugin.getSpinsStats().get(target.getUniqueId()).setSpins(0);
            plugin.getSpinsStats().get(target.getUniqueId()).setRare(0);
            plugin.getSpinsStats().get(target.getUniqueId()).setEpic(0);
            plugin.getSpinsStats().get(target.getUniqueId()).setLegendary(0);
            target.sendRawMessage(ChatColor.GREEN + "Your wheel spin stats have been reset");
            target.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            p.sendRawMessage(ChatColor.GREEN + "Successfully reset their stats");
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }
        else if (strings[0].equalsIgnoreCase("superfurnace")) {
            if (p.getInventory().firstEmpty() == -1) {
                p.sendRawMessage(ChatColor.RED + "You have no open inventory spaces");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return true;
            }

            if (strings.length == 1) {
                ItemStack item = plugin.fastFurnace(1);
                p.getInventory().addItem(item);
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                return true;
            }

            int type;
            try {
                type = Integer.parseInt(strings[1]);
            }
            catch (Exception e) {
                p.sendRawMessage(ChatColor.RED + "That type of furnace doesn't exist");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            if (type < 1 || type > 4) {
                p.sendRawMessage(ChatColor.RED + "That type of furnace doesn't exist");
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return false;
            }

            p.getInventory().addItem(plugin.fastFurnace(type));
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return true;
        }

        p.sendRawMessage(ChatColor.RED + "Usage: /spinwheel <createwheel|removewheel|endloot|settime|getreward|" +
                "givespin|opspin|resetstats|superfurnace>");
        p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        return true;
    }

    public ItemStack getCommon(Player p) {
        p.sendRawMessage(ChatColor.GREEN + "You have received a common item");
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        return plugin.getCommonItems().get(plugin.randomSlot(plugin.getCommonItems().size()));
    }

    public ItemStack getRare(Player p) {
        p.sendRawMessage(ChatColor.GREEN + "You have received a rare item");
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        return plugin.getRareItems().get(plugin.randomSlot(plugin.getRareItems().size()));
    }

    public ItemStack getEpic(Player p) {
        p.sendRawMessage(ChatColor.GREEN + "You have received an epic item");
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        return plugin.getEpicItems().get(plugin.randomSlot(plugin.getEpicItems().size()));
    }

    public ItemStack getLegendary(Player p) {
        p.sendRawMessage(ChatColor.GREEN + "You have received a legendary item");
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        return plugin.getLegendaryItems().get(plugin.randomSlot(plugin.getLegendaryItems().size()));
    }
}
