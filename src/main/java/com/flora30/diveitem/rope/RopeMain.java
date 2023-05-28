package com.flora30.diveitem.rope;

import com.flora30.divelib.BlockLoc;
import com.flora30.divelib.ItemMain;
import com.flora30.divelib.data.Rope;
import com.flora30.divelib.data.RopeObject;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.event.HelpEvent;
import com.flora30.divelib.event.HelpType;
import com.flora30.divelib.util.PlayerItem;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.item.data.ItemDataMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.diveitem.util.BlockUtil;
import com.flora30.diveconstant.data.LayerObject;
import com.flora30.diveconstant.data.item.ItemData;
import com.flora30.diveconstant.data.item.ItemDataObject;
import com.flora30.diveconstant.data.item.RopeData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class RopeMain {
    private final static int showRange = RopeObject.INSTANCE.getShowRange();

    private final static Set<Rope> ropeSet = RopeObject.INSTANCE.getRopeSet();

    /**
     * ロープアイテムを使う
     */
    public static void use(Player player, int itemId) {
        ItemData data = ItemDataObject.INSTANCE.getItemDataMap().get(itemId);
        if(data == null || data.getRopeData() == null) return;

        //ロープの消費は MythicMobs 側で行う

        Rope rope = create(player, itemId);
        if (isRopeLocation(player, player.getLocation())) {
            player.sendMessage("ロープの作成に失敗しました（重ね置きはできません）");
            return;
        }
        if (rope.getLocations().size() <= 1) {
            player.sendMessage("ロープの作成に失敗しました（横・下方向に伸びていきます）");
            return;
        }

        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.Rope));
        ropeSet.add(rope);
        addPlayer(rope, player);
        //Bukkit.getLogger().info(player.getDisplayName() + "がロープを追加しました");
    }

    /**
     * ロープを作成する
     * @param player プレイヤー
     * @param itemId ロープのID
     * @return 作成したロープ
     */
    private static Rope create(Player player, int itemId){
        UUID owner = player.getUniqueId();

        //ロープを構成する座標を作成する
        RopeData ropeData = ItemDataObject.INSTANCE.getItemDataMap().get(itemId).getRopeData();
        Location firstLoc = player.getLocation().clone().add(0,0.5,0);

        List<BlockLoc> locations = getLocations(player,firstLoc,ropeData);
        List<BlockLoc> alterLocations = new ArrayList<>();

        String layerName = LayerObject.INSTANCE.getLayerName(firstLoc);
        if (firstLoc.getBlockY() < 60) {
            org.bukkit.util.Vector vector = RegionAPI.getLayerVector(layerName, true);
            if (vector != null) {
                alterLocations.addAll(getLocations(player,firstLoc.clone().add(vector),ropeData));
            }
        } else if (firstLoc.getBlockY() > 220) {
            org.bukkit.util.Vector vector = RegionAPI.getLayerVector(layerName, false);
            if (vector != null) {
                alterLocations.addAll(getLocations(player,firstLoc.clone().add(vector),ropeData));
            }
        }

        return new Rope(
                itemId,
                owner,
                locations,
                alterLocations,
                ropeData.isUpper(),
                new HashSet<>()
        );
    }
    /**
     * ロープの座標を作成する
     */
    private static List<BlockLoc> getLocations(Player player, Location loc, RopeData data) {
        List<BlockLoc> locations = new ArrayList<>();

        boolean isUpper = data.isUpper();
        boolean isFallNext = false;
        Vector forward = player.getLocation().getDirection().setY(0).normalize();


        for(int i = 0; i < data.getLength(); i++) {

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



    /**
     * ロープを回収する（Owner限定）
     */
    public static void drop(Rope rope) {
        Player owner = Bukkit.getPlayer(rope.getOwner());
        if(owner == null) return;

        for (Player player : rope.getLookingPlayers()) {
            removePlayer(rope, player);
        }

        PlayerItem.INSTANCE.giveItem(owner, ItemMain.INSTANCE.getItem(rope.getId()));
    }

    /**
     * ロープの範囲情報を更新する
     * @param player 動いたプレイヤー
     */
    public static void update(Player player) {
        BlockLoc loc = new BlockLoc(player.getLocation());

        for (Rope rope : ropeSet) {
            if (rope.getLookingPlayers().contains(player)) {
                // 範囲から出た
                if (!(rope.isNear(player))) {
                    removePlayer(rope, player);
                    continue;
                }

                // 範囲の中にいる時にずっと生成し続けたらチカチカする
                // クリック時のairパケットを除外する形で

                // ロープの中にいる
                if (isInRope(loc, rope)) {
                    player.setFallDistance(0);
                }
                continue;
            }

            // 周囲に新しく入った
            if (!(rope.getLookingPlayers().contains(player)) && rope.isNear(player)) {
                addPlayer(rope, player);
            }
        }
    }

    /**
     * ロープの範囲から出た時の処理
     */
    private static void removePlayer(Rope rope, Player player) {
        rope.getLookingPlayers().remove(player);
        //Bukkit.getLogger().info(player.getDisplayName() + "がロープの範囲から出ました");

        // プレイヤーの視界からロープを消す
        for (BlockLoc loc : rope.getLocations()) {
            player.sendBlockChange(loc.getLocation(), loc.getBlock().getBlockData());
        }
        for (BlockLoc loc : rope.getAlterLocations()) {
            player.sendBlockChange(loc.getLocation(), loc.getBlock().getBlockData());
        }
    }

    /**
     * ロープの範囲に入った時の処理
     */
    private static void addPlayer(Rope rope, Player player) {
        rope.getLookingPlayers().add(player);
        //Bukkit.getLogger().info(player.getDisplayName() + "がロープの範囲に入りました");

        display(rope,player, false);
    }

    /**
     * ロープを表示する
     */
    private static void display(Rope rope, Player player, boolean isInstant){
        // 最後のブロック
        Material lastMaterial = rope.isUpper() ? Material.SCAFFOLDING : Material.BARREL;
        // 演出時間
        int effectTime = isInstant ? 0 : 2;

        // ロープの生成演出
        int size = rope.getLocations().size();
        int alterSize = rope.getAlterLocations().size();
        for(int i = 0; i < size - 1; i++) {
            int finalI = i;
            DiveItem.plugin.delayedTask(i * effectTime, () -> {
                player.sendBlockChange(rope.getLocations().get(finalI).getLocation(), Material.SCAFFOLDING.createBlockData());
                if (!isInstant) {
                    player.playSound(rope.getLocations().get(finalI).getLocation(), Sound.BLOCK_LADDER_PLACE, 1, 1);
                }
            });
        }

        DiveItem.plugin.delayedTask(size * effectTime, () -> player.sendBlockChange(rope.getLocations().get(rope.getLocations().size()-1).getLocation(), lastMaterial.createBlockData()));
        // ロープの生成演出
        for(int i = 0; i < alterSize - 1; i++) {
            int finalI = i;
            DiveItem.plugin.delayedTask(i * effectTime, () -> {
                player.sendBlockChange(rope.getAlterLocations().get(finalI).getLocation(), Material.SCAFFOLDING.createBlockData());
                if (!isInstant) {
                    player.playSound(rope.getAlterLocations().get(finalI).getLocation(), Sound.BLOCK_LADDER_PLACE, 1, 1);
                }
            });
        }
        if (alterSize != 0) {
            DiveItem.plugin.delayedTask(alterSize * effectTime, () -> player.sendBlockChange(rope.getAlterLocations().get(rope.getAlterLocations().size()-1).getLocation(), lastMaterial.createBlockData()));
        }
    }

    public static boolean isRopeLocation(Player player, Location loc) {
        BlockLoc blockLoc = new BlockLoc(loc);
        return isRopeLocation(player, blockLoc);
    }

    public static boolean isRopeLocation(Player player, BlockLoc blockLoc) {
        // エラーでやり直し（setが変更された）
        while(true) {
            try{
                for (Rope rope : ropeSet) {
                    if (!rope.getLookingPlayers().contains(player)) {
                        continue;
                    }

                    //ここが重い
                    if(isInRope(blockLoc, rope)) return true;
                }
                return false;
            }catch (ConcurrentModificationException ignored){}
        }
    }

    public static boolean isInRope(BlockLoc loc, Rope rope) {
        if (rope.getLocations().contains(loc)) return true;
        return rope.getAlterLocations().contains(loc);
    }
}
