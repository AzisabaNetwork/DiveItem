package com.flora30.diveitem.rope;

import com.flora30.diveapi.data.item.RopeData;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveapi.tools.BlockLoc;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.item.data.ItemDataMain;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.diveitem.util.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class Rope {
    // ロープのItemID
    public final int itemId;
    // ロープを構成する座標
    public final List<BlockLoc> locations;
    // ロープを構成する座標（上下の階層）
    public final List<BlockLoc> alterLocations;
    // ロープの周囲に人がいない回数
    public int failCount = 0;
    // 上昇ロープ
    public boolean isUpper = false;

    // 置いたプレイヤー
    public final UUID owner;
    // 見ているプレイヤー
    public Set<Player> lookingPlayers = new HashSet<>();

    public Rope(Player player, int itemId) {
        this.itemId = itemId;
        this.owner = player.getUniqueId();

        //ロープを構成する座標を作成する
        RopeData ropeData = ItemDataMain.getItemData(itemId).ropeData;
        Location firstLoc = player.getLocation().clone().add(0,0.5,0);

        locations = getLocations(player,firstLoc,ropeData);
        alterLocations = new ArrayList<>();

        String layerName = RegionAPI.getLayerName(firstLoc);
        if (firstLoc.getBlockY() < 60) {
            Vector vector = RegionAPI.getLayerVector(layerName, true);
            if (vector != null) {
                alterLocations.addAll(getLocations(player,firstLoc.clone().add(vector),ropeData));
            }
        } else if (firstLoc.getBlockY() > 220) {
            Vector vector = RegionAPI.getLayerVector(layerName, false);
            if (vector != null) {
                alterLocations.addAll(getLocations(player,firstLoc.clone().add(vector),ropeData));
            }
        }
    }

    /**
     * 起点をもとに、ロープを構成する座標を作成する
     */
    private List<BlockLoc> getLocations(Player player, Location loc, RopeData data) {
        List<BlockLoc> locations = new ArrayList<>();

        isUpper = data.isUpper;
        boolean isFallNext = false;
        Vector forward = player.getLocation().getDirection().setY(0).normalize();


        for(int i = 0; i < data.length; i++) {

            // 登録する location を設定
            if(isFallNext){
                if (isUpper) {
                    loc = loc.clone().add(0,1,0);
                } else {
                    loc = loc.clone().add(0,-1,0);
                }
            } else {
                loc = loc.clone().add(forward);
            }

            // その場所が通過できない時
            if (!BlockUtil.isIgnoreBlockType(loc.getBlock())){
                break;
            }
            // 既に別のロープが置かれた場所の時
            if (RopeMain.isRopeLocation(player,loc)) {
                break;
            }
            // チェストの場所の時
            if (LootMain.isExistLootLocation(player,loc)) {
                break;
            }

            // 上昇ロープの場合は、上で判定
            if (isUpper) {
                isFallNext = BlockUtil.isIgnoreBlockType(loc.clone().add(0, 1, 0).getBlock());
            }
            // 下のブロックが通過できる時 = 「次」を下にする
            else {
                isFallNext = BlockUtil.isIgnoreBlockType(loc.clone().add(0, -1, 0).getBlock());
            }

            locations.add(new BlockLoc(loc));
        }

        /*
        int size = locations.size();
        Bukkit.getLogger().info("location created");
        for(int i = 0; i < size; i++) {
            BlockLoc blockLoc = locations.get(i);
            Bukkit.getLogger().info("Block "+i+" = "+blockLoc.x+","+blockLoc.y+","+blockLoc.z);
        }
         */

        return locations;
    }


    public BlockLoc getFirstLoc() {
        return locations.get(0);
    }

    public BlockLoc getLastLoc() {
        return locations.get(locations.size() - 1);
    }

    public BlockLoc getAlterFirstLoc() {
        if (alterLocations.size() == 0) return null;
        return alterLocations.get(0);
    }

    public BlockLoc getAlterLastLoc() {
        if (alterLocations.size() == 0) return null;
        return alterLocations.get(alterLocations.size() - 1);
    }

    public void display(Player player, boolean isInstant) {
        // 最後のブロック
        Material lastMaterial = isUpper ? Material.SCAFFOLDING : Material.BARREL;
        // 演出時間
        int effectTime = isInstant ? 0 : 2;

        // ロープの生成演出
        int size = locations.size();
        int alterSize = alterLocations.size();
        for(int i = 0; i < size - 1; i++) {
            int finalI = i;
            DiveItem.plugin.delayedTask(i * effectTime, () -> {
                player.sendBlockChange(locations.get(finalI).getLocation(), Material.SCAFFOLDING.createBlockData());
                if (!isInstant) {
                    player.playSound(locations.get(finalI).getLocation(), Sound.BLOCK_LADDER_PLACE, 1, 1);
                }
            });
        }
        DiveItem.plugin.delayedTask(size * effectTime, () -> player.sendBlockChange(getLastLoc().getLocation(), lastMaterial.createBlockData()));
        // ロープの生成演出
        for(int i = 0; i < alterSize - 1; i++) {
            int finalI = i;
            DiveItem.plugin.delayedTask(i * effectTime, () -> {
                player.sendBlockChange(alterLocations.get(finalI).getLocation(), Material.SCAFFOLDING.createBlockData());
                if (!isInstant) {
                    player.playSound(alterLocations.get(finalI).getLocation(), Sound.BLOCK_LADDER_PLACE, 1, 1);
                }
            });
        }
        if (alterSize != 0) {
            DiveItem.plugin.delayedTask(alterSize * effectTime, () -> player.sendBlockChange(getAlterLastLoc().getLocation(), lastMaterial.createBlockData()));
        }
    }

    public boolean isNear(Player player) {
        BlockLoc playerLoc = new BlockLoc(player.getLocation());

        BlockLoc first = getFirstLoc();
        BlockLoc last = getLastLoc();
        BlockLoc alterFirst = getAlterFirstLoc();
        BlockLoc alterLast = getAlterLastLoc();

        // locations の判定
        if (first.world == playerLoc.world) {
            if (first.distance(playerLoc) <= RopeMain.showRange) {
                return true;
            }
            if (last.distance(playerLoc) <= RopeMain.showRange) {
                return true;
            }
        }

        // alterLocations の判定
        if (alterFirst == null) return false;
        if (alterFirst.world == playerLoc.world) {
            if (alterFirst.distance(playerLoc) <= RopeMain.showRange) {
                return true;
            }
            return alterLast.distance(playerLoc) <= RopeMain.showRange;
        }
        return false;
    }
}
