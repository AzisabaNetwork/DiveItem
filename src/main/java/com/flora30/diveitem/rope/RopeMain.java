package com.flora30.diveitem.rope;

import com.flora30.diveapi.data.ItemData;
import com.flora30.diveapi.event.HelpEvent;
import com.flora30.diveapi.tools.BlockLoc;
import com.flora30.diveapi.tools.HelpType;
import com.flora30.diveapi.tools.PlayerItem;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.item.data.ItemDataMain;
import com.flora30.diveitem.item.ItemStackMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

public class RopeMain {
    public static int showRange;

    public static Set<Rope> ropeSet = new HashSet<>();

    /**
     * ロープアイテムを使う
     */
    public static void use(Player player, int itemId) {
        ItemData data = ItemDataMain.getItemData(itemId);
        if(data == null || data.ropeData == null) return;

        //ロープの消費は MythicMobs 側で行う

        Rope rope = new Rope(player, itemId);
        if (isRopeLocation(player, player.getLocation())) {
            player.sendMessage("ロープの作成に失敗しました（重ね置きはできません）");
            return;
        }
        if (rope.locations.size() <= 1) {
            player.sendMessage("ロープの作成に失敗しました（横・下方向に伸びていきます）");
            return;
        }

        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.Rope));
        ropeSet.add(rope);
        addPlayer(rope, player);
        //Bukkit.getLogger().info(player.getDisplayName() + "がロープを追加しました");
    }


    /**
     * ロープを回収する（Owner限定）
     */
    public static void drop(Rope rope) {
        Player owner = Bukkit.getPlayer(rope.owner);
        if(owner == null) return;

        for (Player player : rope.lookingPlayers) {
            removePlayer(rope, player);
        }

        PlayerItem.giveItem(owner, ItemStackMain.getItem(rope.itemId));
    }

    /**
     * ロープの範囲情報を更新する
     * @param player 動いたプレイヤー
     */
    public static void update(Player player) {
        BlockLoc loc = new BlockLoc(player.getLocation());

        for (Rope rope : ropeSet) {
            if (rope.lookingPlayers.contains(player)) {
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
            if (!(rope.lookingPlayers.contains(player)) && rope.isNear(player)) {
                addPlayer(rope, player);
            }
        }
    }

    /**
     * ロープの範囲から出た時の処理
     */
    private static void removePlayer(Rope rope, Player player) {
        rope.lookingPlayers.remove(player);
        //Bukkit.getLogger().info(player.getDisplayName() + "がロープの範囲から出ました");

        // プレイヤーの視界からロープを消す
        for (BlockLoc loc : rope.locations) {
            player.sendBlockChange(loc.getLocation(), loc.getBlock().getBlockData());
        }
        for (BlockLoc loc : rope.alterLocations) {
            player.sendBlockChange(loc.getLocation(), loc.getBlock().getBlockData());
        }
    }

    /**
     * ロープの範囲に入った時の処理
     */
    private static void addPlayer(Rope rope, Player player) {
        rope.lookingPlayers.add(player);
        //Bukkit.getLogger().info(player.getDisplayName() + "がロープの範囲に入りました");

        rope.display(player, false);
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
                    if (!rope.lookingPlayers.contains(player)) {
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
        if (rope.locations.contains(loc)) return true;
        return rope.alterLocations.contains(loc);
    }
}
