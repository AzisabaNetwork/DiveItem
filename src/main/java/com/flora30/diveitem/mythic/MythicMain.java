package com.flora30.diveitem.mythic;

import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.data.MythicObject;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MythicMain {
    private static final Set<String> mythicObjectives = MythicObject.INSTANCE.getMythicObjectives();

    public static void scoreCheck(Player player){
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if (data == null){
            return;
        }
        Map<String,Integer> coolDownMap = data.getCoolDownMap();

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
