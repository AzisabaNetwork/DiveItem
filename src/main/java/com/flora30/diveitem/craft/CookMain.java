package com.flora30.diveitem.craft;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.tools.PlayerItem;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.mythic.DMythicUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CookMain {

    //焼けるものID|焼いた後ID
    public static final Map<Integer,Integer> cookMap = new HashMap<>();
    public static final String coolDownID = "Cook";

    // 伐採演出に使うMobの名前
    private static final String effectMobName = "CookFire";

    public static boolean cook(Player player, Location baseLoc) {
        // 焼くクールダウンを取得
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if (data == null) return false;
        if (data.coolDownMap.containsKey(coolDownID)) {
            int coolDown = data.coolDownMap.get(coolDownID);
            if (coolDown != 0) return false;
        }

        // 焼けるものIDを取得
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getItemMeta() == null) return false;
        int cookId = ItemStackMain.getItemID(item);

        // 焼いた後を取得
        if (!cookMap.containsKey(cookId)) return false;
        ItemStack cookedItem = ItemStackMain.getItem(cookMap.get(cookId));

        // 交換
        item.setAmount(item.getAmount() - 1);
        PlayerItem.giveItem(player,cookedItem);
        data.coolDownMap.put(coolDownID, 10);

        // 演出
        player.playSound(baseLoc, Sound.BLOCK_FIRE_EXTINGUISH,1,1);
        player.playSound(baseLoc,Sound.BLOCK_CAMPFIRE_CRACKLE,1,1);
        player.spawnParticle(Particle.CLOUD,baseLoc,20,0.5,0.5,0.1,0.1);
        // Mobを召喚する
        DMythicUtil.spawnMob(effectMobName, baseLoc.clone().add(0.5, 0, 0.5));
        return true;
    }
}
