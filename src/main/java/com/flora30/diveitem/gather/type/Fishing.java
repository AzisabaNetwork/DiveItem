package com.flora30.diveitem.gather.type;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.data.Point;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveitem.gather.GatherLayerData;
import com.flora30.diveitem.gather.GatherMain;
import com.flora30.diveitem.item.ItemEntityMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.mythic.DMythicUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import net.minecraft.world.entity.projectile.FishingHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Fishing extends Gather{
    // 演出に使うMobの名前
    //private final String effectMobName = "FishEffect";

    private final double fishPlusRate = 0.3;

    private final PlayerFishEvent fishEvent;

    /**
     * 採集システムを使う（釣り）
     */
    public Fishing(Player player, Location location, int toolId, PlayerFishEvent fishEvent) {
        super(player, location, toolId);
        normalSound = Sound.ENTITY_FISH_SWIM;
        this.fishEvent = fishEvent;
        //Bukkit.getLogger().info("Fishing System : Caught = " + fishEvent.getCaught());

        execute();
    }

    /**
     * スタミナが足りない時は釣りをキャンセルする
     */
    @Override
    protected boolean checkStamina(){
        if(playerData.currentST < staminaCost) {
            fishEvent.setCancelled(true);
            return false;
        }
        playerData.currentST -= staminaCost;
        return true;
    }

    @Override
    protected boolean checkMonster() {
        assert location.getWorld() != null;

        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        GatherLayerData gatherLayerData = GatherMain.gatherLayerMap.get(RegionAPI.getLayerName(location));

        // 原生生物判定 : 初期 firstMonsterRate %
        // 良いイベント：ステータスはプラス値として反映
        double rate = (firstMonsterRate + fishPlusRate) * (2.0 - Point.convertGatherMonsterRate(data.levelData.pointInt));

        // 判定成功：Mobを沸かせる＋演出
        if(Math.random() < rate){
            //Bukkit.getLogger().info("GatherExecute: 原生生物判定");

            // 原生生物Mobを召喚する
            Entity mob = DMythicUtil.spawnMob(gatherLayerData.getRandomFishMob(), location);
            mob.setVelocity(new Vector(0,1,0));

            // バニラの釣りEntityを消す
            fishEvent.getCaught().remove();


            // 演出を召喚する
            //DMythicUtil.spawnMob(effectMobName, location);
            location.getWorld().playSound(location, Sound.ENTITY_FISH_SWIM, SoundCategory.NEUTRAL, 1, 1);

            return false;
        }
        return true;
    }

    @Override
    protected void gatherEffect() {
        assert location.getWorld() != null;

        // 演出を召喚する
        //DMythicUtil.spawnMob(effectMobName, location);
    }

    @Override
    protected void drop() {
        //Bukkit.getLogger().info("GatherExecute: ドロップ成功");
        int dropId = GetDropId();

        // ドロップアイテムが存在しない時
        if(dropId == -1){
            //Bukkit.getLogger().info("[DiveCore-Gather]ドロップアイテムがありません");
            fishEvent.setCancelled(true);
            return;
        }

        // 釣りItemを入れ替える
        ItemStack item = ItemStackMain.getItem(dropId);


        assert fishEvent.getCaught() != null;
        assert item != null;
        ((Item)fishEvent.getCaught()).setItemStack(item);
    }
}
