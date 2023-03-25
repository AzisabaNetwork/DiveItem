package com.flora30.diveitem.rope;

import com.flora30.diveapi.tools.BlockLoc;
import com.flora30.diveitem.DiveItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ConcurrentModificationException;

public class RopeListener {

    public static void onInteract(PlayerInteractEvent event) {
        /* 回収機能　バランスの都合で無効化
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }

        for (Rope rope : RopeMain.ropeSet) {
            if (event.getPlayer().getUniqueId() == rope.owner && rope.getFirstLoc().distance(event.getPlayer().getLocation()) <= 1.3) {
                RopeMain.drop(rope);
            }
        }
         */
    }

    // Player→CraftPlayer→ServerPlayer→ServerGamePacketListenerImplのTick で飛行判定kickが行われている
    // kickを判断しているaboveGroundTickCountはprivateなのでこっちから触れない
    // ServerGamePacketListenerImplは2000行くらいあるので、上書きしに行くと作業コストが半端ない
    // ログが流れる以外のデメリットは無い
    public static void onPlayerKick(PlayerKickEvent event) {
        // 原因が飛行判定の時
        if (event.getReason().toLowerCase().contains("flying is not enabled")) {
            // 死んでる場合はkickしない
            if (event.getPlayer().isDead()) {
                event.setCancelled(true);
                return;
            }

            for (Rope rope : RopeMain.ropeSet) {
                // ロープを表示中
                if (rope.lookingPlayers.contains(event.getPlayer())) {
                    // ロープのすぐ側にいる
                    if (isNearRope(event.getPlayer(), rope)) {
                        event.setCancelled(true);
                        //Bukkit.getLogger().info("[Rope] "+event.getPlayer().getDisplayName()+" はロープの中にいたため、flyKick判定を解除しました");
                        return;
                    }
                }
            }
        }
    }

    private static boolean isNearRope(Player player, Rope rope) {
        BlockLoc loc = new BlockLoc(player.getLocation());

        for (BlockLoc ropeLoc : rope.locations) {
            if (isNear(ropeLoc, loc)) return true;
        }
        for (BlockLoc ropeLoc : rope.alterLocations) {
            if (isNear(ropeLoc, loc)) return true;
        }
        return false;
    }

    private static boolean isNear(BlockLoc loc1, BlockLoc loc2) {
        if (loc1.world != loc2.world) return false;
        return loc1.distance(loc2) <= 5;
    }

    public static void onTick() {
        // 見ている人がいなくなったら削除する
        // 猶予期間を20countだけ用意する
        for (Rope rope : RopeMain.ropeSet) {
            if (rope.lookingPlayers.isEmpty()) {
                rope.failCount++;
            }
            else {
                rope.failCount = 0;
            }
        }
        RopeMain.ropeSet.removeIf(rope -> rope.failCount >= 20);
    }

    public static void onQuit(PlayerQuitEvent e) {
        for (Rope rope : RopeMain.ropeSet) {
            rope.lookingPlayers.remove(e.getPlayer());
        }
    }

    public static void onTickPlayer(Player player) {
        RopeMain.update(player);
    }
}
