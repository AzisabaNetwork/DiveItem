package com.flora30.diveitem.gather.type;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.data.Point;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.gather.GatherLayerData;
import com.flora30.diveitem.gather.GatherMain;
import com.flora30.diveitem.mythic.DMythicUtil;
import com.flora30.diveitem.util.BlockUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import static com.flora30.diveitem.gather.GatherMain.bedrockTick;

public class Mining extends Gather{
    // 採掘演出に使うMobの名前
    private final String effectMobName = "MiningBlock";
    // 採掘演出にかかる時間
    private final int animationTime = 15;

    /**
     * 採集システム（ツルハシで採掘）を作成する
     */
    public Mining(Player player, Location location, int toolId) {
        super(player, location, toolId);
        execute();
    }

    /**
     * 原生生物のスポーン判定を行う
     * @return 処理を続けて大丈夫？（スポーンされなければ True）
     */
    @Override
    protected boolean checkMonster() {
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        GatherLayerData gatherLayerData = GatherMain.gatherLayerMap.get(RegionAPI.getLayerName(location));

        // 原生生物判定 : 初期 firstMonsterRate %
        // 不都合なイベント：ステータスはマイナス値として反映
        double rate = firstMonsterRate * Point.convertGatherMonsterRate(data.levelData.pointInt);

        // 判定成功：Mobを沸かせる＋演出
        if(Math.random() < rate){
            Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
            if(!BlockUtil.isIgnoreBlockType(spawnLoc.getBlock())){
                spawnLoc = player.getLocation();
            }
            MythicMobs.inst().getMobManager().spawnMob(gatherLayerData.getRandomMob(), spawnLoc);

            return false;
        }
        return true;
    }

    /**
     * 採集時の演出を行う
     */
    @Override
    protected void gatherEffect() {
        assert location.getWorld() != null;

        // Mobを召喚する
        DMythicUtil.spawnMob(effectMobName, location.clone().add(0.5, 0, 0.5));
        // パーティクルを再生する
        location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0.5, 0.5), 10, 1, 0.1, 0.1, 0.1, Material.STONE.createBlockData());
        location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0.5, 0.5), 10, 1, 0.1, 0.1, 0.1, Material.STONE.createBlockData());

        //その間、そこはバリアブロックにしておく
        for (int i = 0; i < animationTime; i++){
            DiveItem.plugin.delayedTask(i, () -> player.sendBlockChange(location, Bukkit.createBlockData(Material.BARRIER)));
        }

        //アニメーションが終わったら、一定時間岩盤ブロックにする
        DiveItem.plugin.delayedTask(animationTime, () -> {
            player.sendBlockChange(location, Bukkit.createBlockData(Material.BEDROCK));
            // bedrockTickだけ維持される
            CoreAPI.getPlayerData(player.getUniqueId()).gatherBedrockMap.put(location, bedrockTick);
        });
    }
}
