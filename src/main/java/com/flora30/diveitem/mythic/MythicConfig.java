package com.flora30.diveitem.mythic;

import com.flora30.divelib.util.Config;
import com.flora30.diveitem.DiveItem;
import com.flora30.divelib.data.MythicObject;
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

    public static final String coolDownID = "Cook";
    @Override
    public void load() {
        Bukkit.getLogger().info("[DiveCore-Mythic]mm連携の読み込みを開始します...");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        MythicObject.INSTANCE.getMythicObjectives().addAll(config.getStringList("scores"));
        MythicObject.INSTANCE.getMythicObjectives().add(coolDownID);

        Bukkit.getLogger().info("[DiveCore-Mechanic]mm連携の読み込みが完了しました");
    }

    @Override
    public void save() {

    }
}