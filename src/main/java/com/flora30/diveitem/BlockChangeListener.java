package com.flora30.diveitem;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.flora30.diveapi.DiveAPI;
import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveitem.loot.Loot;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.diveitem.rope.Rope;
import com.flora30.diveitem.rope.RopeMain;
import com.flora30.diveitem.util.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;

import java.util.ConcurrentModificationException;

public class BlockChangeListener extends PacketAdapter {

    public BlockChangeListener() {
        super(DiveAPI.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.BLOCK_CHANGE);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) return;

        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            PacketContainer packet = event.getPacket();

            BlockPosition position = packet.getBlockPositionModifier().read(0);
            WrappedBlockData data = packet.getBlockData().read(0);

            if (CoreAPI.isLogMode()) {
                //Bukkit.getLogger().info("position = "+position.getX()+","+position.getY()+","+position.getZ());
                //Bukkit.getLogger().info("material = "+data.getType());
            }


            // Loot用
            // 光ブロック → air の時も取得するけど無視で
            if (data.getType() == Material.AIR || data.getType() == Material.WATER) {
                Location location = position.toLocation(event.getPlayer().getWorld());
                PlayerData pData = CoreAPI.getPlayerData(event.getPlayer().getUniqueId());
                if (pData == null) return;

                String lootLayer = RegionAPI.getLayerName(event.getPlayer().getLocation());
                if (!pData.layerData.lootLayer.equals(lootLayer)) return;

                Loot loot = LootMain.getLoot(lootLayer);

                // 例外が出たら（途中で変更があった）やり直し
                while(true) {
                    try {
                        // 現在のエリアにあるlootの最大より多いID（その場合LootLocが取れない）があれば消す
                        pData.layerData.lootMap.entrySet().removeIf(i -> loot.getLootLoc(i.getKey()) == null);

                        for (int lootId : pData.layerData.lootMap.keySet()) {
                            // 右クリックじゃないタイミング（テレポート直後）で送られたときは？ -> 距離判定を入れてごまかす
                            if (event.getPlayer().getLocation().distance(location) > 5) {
                                continue;
                            }
                            if (loot.getLootLoc(lootId).getLocation().distance(location) == 0) {

                                if(event.getPlayer().getGameMode() == GameMode.CREATIVE && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.STONE_AXE) {
                                    LootMain.unregisterChest(event.getPlayer(),location);
                                    return;
                                }
                                if(event.getPlayer().getGameMode() == GameMode.CREATIVE && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.CHEST) {
                                    event.setCancelled(true);
                                    return;
                                }

                                //Bukkit.getLogger().info("loot_open -> position = " + position.getX() + "," + position.getY() + "," + position.getZ());
                                //Bukkit.getLogger().info("loot_open -> material = " + data.getType());

                                event.setCancelled(true);
                                LootMain.openChest(event.getPlayer(), loot.getLootLoc(lootId).getLocation());
                                return;
                            }
                        }

                        break;
                    } catch (ConcurrentModificationException ignored) {}
                }
                // ロープ保護
                // 例外が出たら（途中で変更があった）やり直し
                while(true) {
                    try {
                        if (RopeMain.isRopeLocation(event.getPlayer(), location)) {
                            event.setCancelled(true);
                        }
                        break;
                    } catch (ConcurrentModificationException ignored) {}
                }
            }
            // 光が非同期処理でロープを打ち消すのを防止
            else if (data.getType() == Material.LIGHT) {
                Location location = position.toLocation(event.getPlayer().getWorld());
                while(true) {
                    try {
                        if (RopeMain.isRopeLocation(event.getPlayer(), location)) {
                            event.setCancelled(true);
                        }
                        break;
                    } catch (ConcurrentModificationException ignored) {}
                }
            }
            // ロープ以外の通過可能ブロックが送られたとき
            else if (data.getType() != Material.SCAFFOLDING && BlockUtil.isIgnoreBlockType(data.getType())) {
                Location location = position.toLocation(event.getPlayer().getWorld());
                // ロープ保護
                while(true) {
                    try {
                        if (RopeMain.isRopeLocation(event.getPlayer(), location)) {
                            event.setCancelled(true);
                        }
                        break;
                    } catch (ConcurrentModificationException ignored) {}
                }
            }
        }
    }
}
