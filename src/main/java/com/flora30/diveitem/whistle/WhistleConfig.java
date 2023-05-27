package com.flora30.diveitem.whistle;

import com.flora30.diveapin.util.Config;
import com.flora30.diveitem.DiveItem;
import com.flora30.divenew.data.Whistle;
import com.flora30.divenew.data.WhistleObject;
import com.flora30.divenew.data.WhistleType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

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

        Map<Integer,Whistle> whistleMap = WhistleObject.INSTANCE.getWhistleMap();

        for (int i = 0; i < limit; i++) {
            // 初期値
            int rank = 1;
            WhistleType type = WhistleType.Red;
            int returnDepth = 0;
            int enderCapacity = 0;


            // 前レベルから
            if (whistleMap.containsKey(i-1)) {
                Whistle before = whistleMap.get(i-1);
                type = before.getType();
                rank = before.getRank()+1;
                returnDepth = before.getReturnDepth();
                enderCapacity = before.getEnderCapacity();
            }

            // configがある場合
            ConfigurationSection secLv = sec2.getConfigurationSection(String.valueOf(i));
            if (secLv != null) {
                // configの中身を足し算
                if (secLv.contains("Type")) {
                    type = WhistleType.valueOf(secLv.getString("Type"));
                    rank = 1;
                }
                enderCapacity += secLv.getInt("EnderCapacity",0);
                returnDepth += secLv.getInt("ReturnDepth",0);
            }
            whistleMap.put(i,new Whistle(rank,type,returnDepth,enderCapacity));
        }

        Bukkit.getLogger().info("[DiveItem-Whistle]笛ランクの読み込みが完了しました");
    }

    @Override
    public void save() {
    }


    private void calcLvMap(double firstExp, double increaseRate, double plus, int limit){
        double current = firstExp;
        WhistleObject.INSTANCE.getWhistleExpMap().put(1, (int) firstExp);
        for (int i = 2; i <= limit; i++){
            current = current * increaseRate + plus;
            WhistleObject.INSTANCE.getWhistleExpMap().put(i, (int) current);
        }
    }
}
