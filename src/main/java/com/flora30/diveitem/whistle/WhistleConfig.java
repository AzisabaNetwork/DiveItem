package com.flora30.diveitem.whistle;

import com.flora30.diveapi.data.Whistle;
import com.flora30.diveapi.tools.Config;
import com.flora30.diveapi.tools.WhistleType;
import com.flora30.diveitem.DiveItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class WhistleConfig extends Config {

    @Override
    public void load() {
        File file = new File(DiveItem.plugin.getDataFolder(),File.separator+"whistle.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection sec1 = config.getConfigurationSection("Whistle");
        if (sec1 == null){
            config.createSection("Whistle");
            sec1 = config.getConfigurationSection("Whistle");
        }

        // whistle.ymlの読み込み
        double first = loadOrDefault("Whistle",sec1,"FirstExp",30.0);
        double rate = loadOrDefault("Whistle",sec1,"IncreaseRate",1.05);
        double plus = loadOrDefault("Whistle",sec1,"IncreasePlus",5);
        int limit = loadOrDefault("Whistle",sec1,"Limit",50);

        //自動計算
        calcLvMap(first,rate,plus,limit);

        ConfigurationSection sec2 = config.getConfigurationSection("Level");
        if (sec2 == null){
            config.createSection("Level");
            sec2 = config.getConfigurationSection("Level");
            assert sec2 != null;
        }

        for (int i = 0; i < limit; i++) {
            Whistle whistle = new Whistle();
            whistle.type = WhistleType.Red;
            whistle.rank = 1;
            whistle.enderCapacity = 0;
            whistle.returnDepth = 0;

            // 前レベルから
            if (WhistleMain.whistleMap.containsKey(i-1)) {
                Whistle before = WhistleMain.whistleMap.get(i-1);
                whistle.type = before.type;
                whistle.rank = before.rank+1;
                whistle.returnDepth = before.returnDepth;
                whistle.enderCapacity = before.enderCapacity;
            }

            // configがある場合
            ConfigurationSection secLv = sec2.getConfigurationSection(String.valueOf(i));
            if (secLv != null) {
                // configの中身を足し算
                if (secLv.contains("Type")) {
                    whistle.type = WhistleType.valueOf(secLv.getString("Type"));
                    whistle.rank = 1;
                }
                whistle.enderCapacity += secLv.getInt("EnderCapacity",0);
                whistle.returnDepth += secLv.getInt("ReturnDepth",0);
            }

            WhistleMain.whistleMap.put(i,whistle);
        }

        Bukkit.getLogger().info("[DiveItem-Whistle]笛ランクの読み込みが完了しました");
    }

    @Override
    public void save() {
    }


    private void calcLvMap(double firstExp, double increaseRate, double plus, int limit){
        double current = firstExp;
        WhistleMain.whistleExpMap.put(1, (int) firstExp);
        for (int i = 2; i <= limit; i++){
            current = current * increaseRate + plus;
            WhistleMain.whistleExpMap.put(i, (int) current);
        }
    }
}
