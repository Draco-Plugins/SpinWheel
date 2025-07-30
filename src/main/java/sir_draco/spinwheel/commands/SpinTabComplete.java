package sir_draco.spinwheel.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class SpinTabComplete implements Listener {

    public SpinTabComplete() {
        // Register the listener
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        List<String> words = new ArrayList<>();
        String buffer = e.getBuffer();
        Player p = (Player) e.getSender();
        if (buffer.contains("spinwheel getreward ") || buffer.contains("sw getreward ")) words = getSpinTypes();
        else if (buffer.contains("spinwheel givespin ") || buffer.contains("spinwheel resetstats ")
        || buffer.contains("sw givespin ") || buffer.contains("sw resetstats ")) words = getPlayers();
        else if (buffer.contains("spinwheel ") || buffer.contains("sw ")) words = getSpinCommands();
        else if (buffer.contains("spin ")) {
            if (p.hasPermission("wheel.admin")) words = getSpinTypes();
            words.add("all");
            words.add("stats");
        }
        else return;
        e.setCompletions(getCompletions(buffer, words));
    }

    public List<String> getCompletions(String buffer, List<String> words) {
        ArrayList<String> completions = new ArrayList<>();
        String[] segment = buffer.split(" ");

        if (segment.length == 1) return words;
        if (segment.length > 1 && buffer.endsWith(" ")) return words;

        for (String word : words) if (matchPrefix(segment[segment.length - 1], word)) completions.add(word);
        return completions;
    }

    public boolean matchPrefix(String input, String name) {
        if (input.length() > name.length()) return false;
        for (int i = 0; i < input.length(); i++) if (input.charAt(i) != name.charAt(i)) return false;
        return true;
    }

    public List<String> getSpinTypes() {
        ArrayList<String> words = new ArrayList<>();
        words.add("common");
        words.add("epic");
        words.add("legendary");
        words.add("rare");
        return words;
    }

    public List<String> getSpinCommands() {
        ArrayList<String> words = new ArrayList<>();
        words.add("createwheel");
        words.add("endloot");
        words.add("getreward");
        words.add("givespin");
        words.add("opspin");
        words.add("removewheel");
        words.add("resetstats");
        words.add("settime");
        words.add("superfurnace");
        words.add("spawner");
        return words;
    }

    public List<String> getPlayers() {
        ArrayList<String> words = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) words.add(p.getName());
        return words;
    }
}
