package smp.jalebi.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class JalebiScoreboard extends JavaPlugin implements Listener {
    private DataStore data;
    private BoardManager boards;
    private ScoreboardGui gui;

    @Override
    public void onEnable() {
        data = new DataStore(this);
        boards = new BoardManager(this);
        gui = new ScoreboardGui(this);

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(gui, this);

        // refresh every second
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) boards.update(p);
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        data.save();
    }

    public DataStore data() { return data; }
    public BoardManager boards() { return boards; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> boards.update(e.getPlayer()), 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        boards.forget(e.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (cmd.getName().equalsIgnoreCase("scoreboard")) {
            if (!(s instanceof Player p)) {
                s.sendMessage("Players only.");
                return true;
            }
            gui.open(p);
            return true;
        }

        // /shards <give|take|set|check> <player> [amount]
        if (a.length < 2) {
            s.sendMessage("§cUsage: /shards <give|take|set|check> <player> [amount]");
            return true;
        }
        Player t = Bukkit.getPlayerExact(a[1]);
        if (t == null) {
            s.sendMessage("§cPlayer not online.");
            return true;
        }
        long cur = data.getShards(t.getUniqueId());
        if (a[0].equalsIgnoreCase("check")) {
            s.sendMessage("§d" + t.getName() + " has " + cur + " shards.");
            return true;
        }
        if (a.length < 3) {
            s.sendMessage("§cSpecify an amount.");
            return true;
        }
        long amt;
        try {
            amt = Long.parseLong(a[2]);
        } catch (NumberFormatException ex) {
            s.sendMessage("§cInvalid number.");
            return true;
        }
        switch (a[0].toLowerCase()) {
            case "give" -> data.setShards(t.getUniqueId(), cur + amt);
            case "take" -> data.setShards(t.getUniqueId(), cur - amt);
            case "set" -> data.setShards(t.getUniqueId(), amt);
            default -> {
                s.sendMessage("§cUnknown action.");
                return true;
            }
        }
        boards.update(t);
        s.sendMessage("§aDone. " + t.getName() + " now has " + data.getShards(t.getUniqueId()) + " shards.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        if (c.getName().equalsIgnoreCase("shards") && a.length == 1) return List.of("give", "take", "set", "check");
        return null;
    }
}
