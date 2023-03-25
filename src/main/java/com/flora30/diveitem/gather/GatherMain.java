package com.flora30.diveitem.gather;

import com.flora30.diveapi.data.ItemData;
import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveapi.tools.PacketUtil;
import com.flora30.diveapi.tools.ToolType;
import com.flora30.diveitem.Listeners;
import com.flora30.diveitem.item.data.ItemDataMain;
import com.flora30.diveitem.gather.type.Fishing;
import com.flora30.diveitem.gather.type.Logging;
import com.flora30.diveitem.gather.type.Mining;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.*;

public class GatherMain {
    // 階層ごとの採集の情報
    public static final Map<String, GatherLayerData> gatherLayerMap = new HashMap<>();

    // 岩盤packetを送るtick数
    public static int bedrockTick = 200;


    public static void gather(Player player, Location location, int toolId){

        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if(data == null) return;

        // GatherDataを取得
        ItemData itemData = ItemDataMain.getItemData(toolId);
        if(itemData == null) return;
        ToolType type = itemData.gatherData.toolType;


        // GatherLayerを取得
        String layerName = RegionAPI.getLayerName(location);
        GatherLayerData gatherLayerData = gatherLayerMap.get(layerName);
        if(gatherLayerData == null){
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

        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if(data == null) return;

        // GatherDataを取得
        ItemData itemData = ItemDataMain.getItemData(toolId);
        if(itemData == null) return;
        ToolType type = itemData.gatherData.toolType;
        if(type != ToolType.Fishing) return;


        //Bukkit.getLogger().info("Gather item data passed");

        // GatherLayerを取得
        String layerName = RegionAPI.getLayerName(player.getLocation());
        GatherLayerData gatherLayerData = gatherLayerMap.get(layerName);
        if(gatherLayerData == null){
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
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if(data == null) return;

        Set<Location> removeSet = new HashSet<>();
        for(Location loc : data.gatherBedrockMap.keySet()){
            // カウントを進める
            int remain = data.gatherBedrockMap.get(loc);
            remain -= Math.min(remain - Listeners.gatherSendTick, 0);
            data.gatherBedrockMap.put(loc, remain);

            // カウントによって分岐
            // カウントが 0 の場合 = Mapから消す（「終了した」packetを送る）
            if(remain == 0){
                removeSet.add(loc);
                PacketUtil.sendBlockChangePacket(player, loc.getBlock().getBlockData().getMaterial(), loc);
            }
            else{
                // カウントがある場合 = packetを送る
                PacketUtil.sendBlockChangePacket(player, Material.BEDROCK, loc);
            }
        }

        // カウントが 0 のデータを消す
        data.gatherBedrockMap.keySet().removeAll(removeSet);
    }
}