package com.flora30.diveitem.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class DMythicUtil {
    /**
     * Mobを指定した場所にスポーンさせる
     */
    public static Entity spawnMob(String mobName, Location location){
        try {
            return MythicMobs.inst().getAPIHelper().spawnMythicMob(mobName, location);
        } catch (InvalidMobTypeException e) {
            Bukkit.getLogger().info(ChatColor.YELLOW + "[DiveCore-Mythic] Mob名 " + mobName + " が見つかりません");
            return null;
        }
    }
}
