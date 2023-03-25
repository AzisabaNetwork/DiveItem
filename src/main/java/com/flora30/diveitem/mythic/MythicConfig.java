package com.flora30.diveitem.mythic;

import com.flora30.diveapi.tools.Config;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.craft.CookMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;

public class MythicConfig extends Config {
    private final File file;

    public MythicConfig() {
        file = new File(DiveItem.plugin.getDataFolder(), File.separator + "mythic.yml");
    }

    @Override
    public void load() {
        Bukkit.getLogger().info("[DiveCore-Mythic]mm連携の読み込みを開始します...");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        MythicMain.mythicObjectives = new HashSet<>(config.getStringList("scores"));
        MythicMain.mythicObjectives.add(CookMain.coolDownID);

        Bukkit.getLogger().info("[DiveCore-Mechanic]mm連携の読み込みが完了しました");
    }

    @Override
    public void save() {

    }
}