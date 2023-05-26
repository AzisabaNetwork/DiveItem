package com.flora30.diveitem.gather;

import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveapin.util.PacketUtil;
import com.flora30.diveitem.Listeners;
import com.flora30.diveitem.gather.type.Fishing;
import com.flora30.diveitem.gather.type.Logging;
import com.flora30.diveitem.gather.type.Mining;
import com.flora30.divenew.data.GatherData;
import com.flora30.divenew.data.LayerObject;
import com.flora30.divenew.data.item.ItemData;
import com.flora30.divenew.data.item.ItemDataObject;
import com.flora30.divenew.data.item.ToolType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.*;

public class GatherMain {

    // 岩盤packetを送るtick数
    public static int bedrockTick = 200;


    public static void gather(Player player, Location location, int toolId){

        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if(data == null) return;

        // ToolDataを取得
        ItemData itemData = ItemDataObject.INSTANCE.getItemDataMap().get(toolId);
        if(itemData == null || itemData.getToolData() == null) return;
        ToolType type = itemData.getToolData().getToolType();


        // GatherDataを取得
        String layerName = LayerObject.INSTANCE.getLayerName(location);
        GatherData gatherData = LayerObject.INSTANCE.getGatherMap().get(layerName);
        if(gatherData == null){
            return;
        }

        //Bukkit.getLogger().info("採掘データは十分");

        // 採集システムを実行する
        // ToolTypeによって分岐
        switch (type) {
            case Mining -> {
                //Bukkit.getLogger().info("Miningを行う");
                new Mining(player, location, toolId);
            }
            case Logging -> {
                //Bukkit.getLogger().info("Loggingを行う");
                new Logging(player, location, toolId);
            }
        }
    }

    // 釣り用
    public static void gather(Player player, PlayerFishEvent event, int toolId){

        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if(data == null) return;

        // ToolDataを取得
        ItemData itemData = ItemDataObject.INSTANCE.getItemDataMap().get(toolId);
        if(itemData == null || itemData.getToolData() == null) return;
        ToolType type = itemData.getToolData().getToolType();
        if(type != ToolType.Fishing) return;


        //Bukkit.getLogger().info("Gather item data passed");

        // GatherDataを取得
        String layerName = LayerObject.INSTANCE.getLayerName(player.getLocation());
        GatherData gatherData = LayerObject.INSTANCE.getGatherMap().get(layerName);
        if(gatherData == null){
            return;
        }

        if(event.getCaught() == null) {
            return;
        }

        //Bukkit.getLogger().info("採集データは十分");

        // 採集システムを実行する
        //Bukkit.getLogger().info("Fishingを行う");
        new Fishing(player, event.getCaught().getLocation(), toolId, event);
    }

    /**
     * 岩盤ブロックを送る判定（gatherSendTickごとに発生）
     * Map操作とパケットでブロック変化、たぶん重くない
     * 同期処理（MapからのRemoveがあるので、同期されてないとエラー吐く）
     * 万が一重かったらExceptionをIgnoreする（今までの処理終わってる時のエラーなので、そのまま閉じていい）
     */
    public static void checkBedrock(Player player){
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if(data == null) return;

        Set<Location> removeSet = new HashSet<>();
        for(Location loc : data.getGatherBedrockMap().keySet()){
            // カウントを進める
            int remain = data.getGatherBedrockMap().get(loc);
            remain -= Math.min(remain - Listeners.gatherSendTick, 0);
            data.getGatherBedrockMap().put(loc, remain);

            // カウントによって分岐
            // カウントが 0 の場合 = Mapから消す（「終了した」packetを送る）
            if(remain == 0){
                removeSet.add(loc);
                PacketUtil.INSTANCE.sendBlockChangePacket(player, loc.getBlock().getBlockData().getMaterial(), loc);
            }
            else{
                // カウントがある場合 = packetを送る
                PacketUtil.INSTANCE.sendBlockChangePacket(player, Material.BEDROCK, loc);
            }
        }

        // カウントが 0 のデータを消す
        data.getGatherBedrockMap().keySet().removeAll(removeSet);
    }
}