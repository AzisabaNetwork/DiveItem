package com.flora30.diveitem.mythic;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MythicMain {
    public static Set<String> mythicObjectives = new HashSet<>();

    public static void scoreCheck(Player player){
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if (data == null){
            return;
        }
        Map<String,Integer> coolDownMap = data.coolDownMap;

        for (String name : mythicObjectives){
            if (!coolDownMap.containsKey(name)){
                coolDownMap.put(name,0);
            }
            else if(coolDownMap.get(name) > 0){
                coolDownMap.put(name, coolDownMap.get(name) - 1);
            }
        }
    }
}
