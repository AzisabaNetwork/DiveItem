package com.flora30.diveitem.rope;

import com.flora30.diveapin.data.RopeObject;
import com.flora30.diveapin.util.Config;
import com.flora30.diveitem.DiveItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class RopeConfig extends Config {
    private final File file;

    public RopeConfig(){
        file = new File(DiveItem.plugin.getDataFolder(),File.separator+"rope.yml");
    }

    @Override
    public void load() {
        Bukkit.getLogger().info("[DiveItem-Rope]ギミックの読み込みを開始します...");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        RopeObject.INSTANCE.setShowRange(loadOrDefault("Rope", config, "showRange", 30));

        Bukkit.getLogger().info("[DiveItem-Rope]ギミックの読み込みが完了しました");
    }

    @Override
    public void save() {

    }
}
