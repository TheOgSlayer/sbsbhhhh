package smp.jalebi.scoreboard;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
    private final File file;
    private final YamlConfiguration cfg;

    public DataStore(JalebiScoreboard plugin) {
        plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "data.yml");
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isEnabled(UUID id, Line line) {
        return cfg.getBoolean("players." + id + ".toggles." + line.name(), true);
    }

    public void setEnabled(UUID id, Line line, boolean on) {
        cfg.set("players." + id + ".toggles." + line.name(), on);
        save();
    }

    public long getShards(UUID id) {
        return cfg.getLong("players." + id + ".shards", 0);
    }

    public void setShards(UUID id, long amount) {
        cfg.set("players." + id + ".shards", Math.max(0, amount));
        save();
    }

    public synchronized void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
