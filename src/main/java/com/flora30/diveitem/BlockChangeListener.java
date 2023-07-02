package com.flora30.diveitem;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.flora30.divelib.DiveLib;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.diveitem.rope.RopeMain;
import com.flora30.diveitem.util.BlockUtil;
import com.flora30.divelib.data.LayerObject;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ConcurrentModificationException;

public class BlockChangeListener extends PacketAdapter {

    public BlockChangeListener() {
        super(DiveLib.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.BLOCK_CHANGE);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) return;

        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            PacketContainer packet = event.getPacket();

            BlockPosition position = packet.getBlockPositionModifier().read(0);
            WrappedBlockData data = packet.getBlockData().read(0);

            /*
            if (CoreAPI.isLogMode()) {
                //Bukkit.getLogger().info("position = "+position.getX()+","+position.getY()+","+position.getZ());
                //Bukkit.getLogger().info("material = "+data.getType());
            }
             */

            Location location = position.toLocation(event.getPlayer().getWorld());

            // 元のブロックに変更された場合
            if (data.getType() == location.getBlock().getType()) {
                // Loot用
                checkLoot(event,position);

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

    private void checkLoot(PacketEvent event, BlockPosition position){
        Location location = position.toLocation(event.getPlayer().getWorld());
        PlayerData pData = PlayerDataObject.INSTANCE.getPlayerDataMap().get(event.getPlayer().getUniqueId());
        if (pData == null) return;

        String lootLayer = LayerObject.INSTANCE.getLayerName(event.getPlayer().getLocation());
        if (!pData.getLayerData().getLootLayer().equals(lootLayer)) return;

        // 例外が出たら（途中で変更があった）やり直し
        while(true) {
            try {
                // 現在のエリアにあるlootの最大より多いID（その場合LootLocが取れない）があれば消す
                // IDではなくLocationで取り扱うようになったので、その可能性はないはず？
                //pData.getLayerData().getLootMap().entrySet().removeIf(i -> pData.getLayerData().isLootLocation(new BlockLoc(i.getKey())));

                for (Location lootLoc : pData.getLayerData().getLootMap().keySet()) {
                    // 右クリックじゃないタイミング（テレポート直後）で送られたときは？ -> 距離判定を入れてごまかす
                    if (event.getPlayer().getLocation().distance(location) > 5) {
                        continue;
                    }
                    if (lootLoc.distance(location) == 0) {

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
                        LootMain.openChest(event.getPlayer(), lootLoc);
                        return;
                    }
                }

                break;
            } catch (ConcurrentModificationException ignored) {}
        }
    }
}
