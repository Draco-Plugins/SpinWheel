package sir_draco.spinwheel;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpinTimer extends BukkitRunnable {

    private final SpinWheel plugin;
    private final Player p;
    private int type;
    private int ticks = -1;
    private int times = 0;

    public SpinTimer(SpinWheel plugin, Player p, String type, int times) {
        this.plugin = plugin;
        this.p = p;
        getType(type);
        this.times = times;
    }

    @Override
    public void run() {
        ticks++;
        // 48, 144, 336, 528 Ticks are the times when the wheel should slow down
        if (ticks < 48) {
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            plugin.getWheel().spinWheel();
        }
        else if (ticks < 96 && ticks % 2 == 0) {
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            plugin.getWheel().spinWheel();
        }
        else if (ticks >= 96 && ticks < 144 && ticks % 4 == 0) {
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            plugin.getWheel().spinWheel();
        }
        else if (ticks >= 144 && ticks < 240 && ticks % 8 == 0) {
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            plugin.getWheel().spinWheel();
        }
        else if (ticks >= 260) {
            // Check what type of reward generation should happen
            int common = 0;
            int rare = 0;
            int epic = 0;
            int legendary = 0;
            int result;
            if (type == 4) {
                for (int i = 1; i <= times; i++) {
                    result = plugin.generateAward(p, -1);
                    if (result == 0) common++;
                    else if (result == 1) rare++;
                    else if (result == 2) epic++;
                    else if (result == 3) legendary++;
                }
            }
            else {
                result = plugin.generateAward(p, type);
                if (result == 0) common++;
                else if (result == 1) rare++;
                else if (result == 2) epic++;
                else if (result == 3) legendary++;
            }
            printResults(common, rare, epic, legendary);
            this.cancel();
        }
    }

    public void getType(String word) {
        if (word == null) {
            type = -1;
            return;
        }
        if (word.equalsIgnoreCase("common")) type = 0;
        else if (word.equalsIgnoreCase("rare")) type = 1;
        else if (word.equalsIgnoreCase("epic")) type = 2;
        else if (word.equalsIgnoreCase("legendary")) type = 3;
        else if (word.equalsIgnoreCase("all")) type = 4;
        else type = -1;
    }

    public void printResults(int common, int rare, int epic, int legendary) {
        Map<UUID, WheelStats> spins = plugin.getSpinsStats();
        Wheel wheel = plugin.getWheel();
        if (legendary != 0) {
            String plural;
            if (legendary == 1) plural = "item";
            else plural = "items";
            plugin.getServer().broadcastMessage(p.getDisplayName() + ChatColor.GREEN + " won " + ChatColor.AQUA +
                    legendary + ChatColor.GOLD + " LEGENDARY " + ChatColor.GREEN + plural + " in the spin wheel!");
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "They have won " + ChatColor.GOLD
                    + spins.get(p.getUniqueId()).getLegendary() + ChatColor.GREEN + " legendary items total");

            wheel.getWorld().strikeLightningEffect(wheel.getCenter().add(0, 2, 0));
            wheel.getWorld().playSound(wheel.getCenter(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
            plugin.fireworks(3);
            for (Player player : plugin.getServer().getOnlinePlayers())
                player.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);

            if (epic != 0) {
                String plural2;
                if (epic == 1) plural2 = "item";
                else plural2 = "items";
                plugin.getServer().broadcastMessage(p.getDisplayName() + ChatColor.GREEN + " won " + ChatColor.AQUA + epic
                        + ChatColor.LIGHT_PURPLE + " EPIC " + ChatColor.GREEN + plural2 + " in the spin wheel!");
                plugin.getServer().broadcastMessage(ChatColor.GREEN + "They have won " + ChatColor.LIGHT_PURPLE + spins.get(p.getUniqueId()).getEpic() + ChatColor.GREEN + " epic items");
            }
        }
        else if (epic != 0) {
            String plural;
            if (epic == 1) plural = "item";
            else plural = "items";
            plugin.getServer().broadcastMessage(p.getDisplayName() + ChatColor.GREEN + " won " + ChatColor.AQUA + epic
                    + ChatColor.LIGHT_PURPLE + " EPIC " + ChatColor.GREEN + plural + " in the spin wheel!");
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "They have won " + ChatColor.LIGHT_PURPLE + spins.get(p.getUniqueId()).getEpic() + ChatColor.GREEN + " epic items");
            wheel.getWorld().playSound(wheel.getCenter(), Sound.ITEM_GOAT_HORN_SOUND_1, 1, 1);
            plugin.fireworks(2);
        }
        else if (rare != 0) {
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            plugin.fireworks(1);
        }
        else p.playSound(p, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);

        if (rare != 0) {
            String plural;
            if (rare == 1) plural = "item";
            else plural = "items";
            p.sendRawMessage(ChatColor.GREEN + "You won " + ChatColor.AQUA + rare + ChatColor.BLUE + " RARE " + ChatColor.GREEN + plural + "!");
            p.sendRawMessage(ChatColor.GREEN + "You have received " + ChatColor.BLUE + spins.get(p.getUniqueId()).getRare() + ChatColor.GREEN + " rare items total");
        }

        if (common != 0) {
            String plural;
            if (common == 1) plural = "item";
            else plural = "items";
            p.sendRawMessage(ChatColor.GREEN + "You won " + ChatColor.WHITE + common + ChatColor.GREEN + " common " + plural + "!");
        }
    }
}
