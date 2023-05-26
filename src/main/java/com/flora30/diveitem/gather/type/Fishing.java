package com.flora30.diveitem.gather.type;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveitem.gather.GatherMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.mythic.DMythicUtil;
import com.flora30.divenew.data.GatherData;
import com.flora30.divenew.data.LayerObject;
import com.flora30.divenew.data.PointObject;
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
        if(playerData.getCurrentST() < staminaCost) {
            fishEvent.setCancelled(true);
            return false;
        }
        playerData.setCurrentST(playerData.getCurrentST() - staminaCost);
        return true;
    }

    @Override
    protected boolean checkMonster() {
        assert location.getWorld() != null;

        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        GatherData gatherData = LayerObject.INSTANCE.getGatherMap().get(LayerObject.INSTANCE.getLayerName(location));

        // 原生生物判定 : 初期 firstMonsterRate %
        // 良いイベント：ステータスはプラス値として反映
        double rate = (firstMonsterRate + fishPlusRate) * (2.0 - PointObject.INSTANCE.getGatherMonsterRate(data.getLevelData().getPointInt()));

        // 判定成功：Mobを沸かせる＋演出
        if(Math.random() < rate){
            //Bukkit.getLogger().info("GatherExecute: 原生生物判定");

            // 原生生物Mobを召喚する
            Entity mob = DMythicUtil.spawnMob(gatherData.getRandomFishMob(), location);
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
        ItemStack item = ItemMain.INSTANCE.getItem(dropId);


        assert fishEvent.getCaught() != null;
        assert item != null;
        ((Item)fishEvent.getCaught()).setItemStack(item);
    }
}
