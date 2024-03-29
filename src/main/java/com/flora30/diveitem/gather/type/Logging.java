package com.flora30.diveitem.gather.type;

import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.gather.GatherMain;
import com.flora30.diveitem.mythic.DMythicUtil;
import com.flora30.diveitem.util.BlockUtil;
import com.flora30.divelib.data.GatherData;
import com.flora30.divelib.data.LayerObject;
import com.flora30.divelib.data.PointObject;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.*;
import org.bukkit.entity.Player;

import static com.flora30.diveitem.gather.GatherMain.bedrockTick;

public class Logging extends Gather{
    // 伐採演出に使うMobの名前
    private final String effectMobName = "LoggingBlock";
    // 伐採演出にかかる時間
    private final int animationTime = 15;

    /**
     * 採集システム（伐採）
     */
    public Logging(Player player, Location location, int toolId) {
        super(player, location, toolId);
        normalSound = Sound.BLOCK_WOOD_BREAK;
        execute();
    }

    /**
     * 原生生物のスポーン判定を行う
     * @return 処理を続けて大丈夫？（スポーンされなければ True）
     */
    @Override
    protected boolean checkMonster() {
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        GatherData gatherData = LayerObject.INSTANCE.getGatherMap().get(LayerObject.INSTANCE.getLayerName(location));

        // 原生生物判定 : 初期 firstMonsterRate %
        // 不都合なイベント：ステータスはマイナス値として反映
        double rate = firstMonsterRate * PointObject.INSTANCE.getGatherMonsterRate(data.getLevelData().getPointInt());

        // 判定成功：Mobを沸かせる＋演出
        if(Math.random() < rate){
            Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
            if(!BlockUtil.isIgnoreBlockType(spawnLoc.getBlock())){
                spawnLoc = player.getLocation();
            }
            MythicMobs.inst().getMobManager().spawnMob(gatherData.getRandomMob(), spawnLoc);

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
        location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0.5, 0.5), 1, 1, 0.1, 0.1, 0.1, Material.OAK_WOOD.createBlockData());
        location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0.5, 0.5), 1, 1, 0.1, 0.1, 0.1, Material.OAK_WOOD.createBlockData());

        //その間、そこはバリアブロックにしておく
        for (int i = 0; i < animationTime; i++){
            DiveItem.plugin.delayedTask(i, () -> player.sendBlockChange(location, Bukkit.createBlockData(Material.BARRIER)));
        }

        //アニメーションが終わったら、一定時間岩盤ブロックにする
        DiveItem.plugin.delayedTask(animationTime, () -> {
            player.sendBlockChange(location, Bukkit.createBlockData(Material.BEDROCK));
            // bedrockTickだけ維持される
            PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getGatherBedrockMap().put(location, bedrockTick);
        });
    }
}
