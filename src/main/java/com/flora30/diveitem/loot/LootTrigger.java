package com.flora30.diveitem.loot;

import com.flora30.diveapi.data.Layer;
import com.flora30.diveapi.data.Point;
import com.flora30.diveapi.event.LayerChangeEvent;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.plugins.ItemAPI;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveapi.tools.BlockLoc;
import com.flora30.diveitem.loot.gui.LootAdminGUI;
import com.flora30.diveitem.rope.Rope;
import com.flora30.diveitem.rope.RopeMain;
import com.flora30.diveitem.util.PlayerItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class LootTrigger {
    public static void onClick(PlayerInteractEvent e){
        /*
        Block block = e.getClickedBlock();
        if(block == null || !block.getType().equals(Material.CHEST)){
            if (block == null){
                Bukkit.getLogger().info("block = null");
            }
            else{
                Bukkit.getLogger().info("block = "+block.getType());
            }
            return;
        }
        //chestを右クリックした場合のみ
        Bukkit.getLogger().info("chest右クリック判定");
        e.setCancelled(true);
        LootMain.openChest(e.getPlayer(),block.getLocation());

         */
    }


    public static void onInventoryClick(InventoryClickEvent e){
    }

    public static void onInventoryClose(InventoryCloseEvent e){
        //チェストを削除
        int id = CoreAPI.getPlayerData(e.getPlayer().getUniqueId()).layerData.openLootLocID;
        LootMain.closeChest((Player) e.getPlayer(),id);
    }

    public static void onCommand(Player player, String subCommand, String sub2, String sub3){
        if (subCommand.equals("register")){
            LootMain.registerChest(player);
        }
        if (subCommand.equals("admin")){
            try{
                int level = Integer.parseInt(sub3);
                LootAdminGUI.open(player, sub2, level);
            } catch (NumberFormatException e){
                player.sendMessage("loot admin [layer] [lv]");
            }
        }
        if (subCommand.equals("respawn")){
            LootMain.executeRandomSpawn(player,player.getLocation());
            player.sendMessage("ルートチェストを再生成しました");
        }

    }

    public static void onPlace(BlockPlaceEvent e){
        if (e.getBlockPlaced().getType() == Material.CHEST){
            LootMain.registerChest(e.getPlayer(),e.getBlockPlaced().getLocation());
        }
    }

    // BlockChangeListenerへ移動
    public static void onBreak(BlockBreakEvent event){
        if (event.getBlock().getType() == Material.CHEST){
            LootMain.unregisterChest(event.getPlayer(), event.getBlock().getLocation());
        }
    }

    public static void onLayerChange(LayerChangeEvent event){
        Player player = Bukkit.getPlayer(event.getUuid());
        if(player == null) return;

        String groupName = RegionAPI.getLayer(event.getNextLayer()).groupName;
        String lootLayer = CoreAPI.getPlayerData(event.getUuid()).layerData.lootLayer;

        // 同じ階層にあるものはgroupNameを同じにしてチェストの再生成しないようにできる
        if (!groupName.equals(lootLayer)){
            LootMain.executeRandomSpawn(player, player.getLocation());
        }
    }

    public static void onTickSendChest(Player player){
        //10tickごとの処理
       LootMain.sendChest(player);
    }

    /*
    public static void onLayerChange(LayerChangeEvent event){
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null){
            return;
        }
        LootMain.executeRandomSpawn(player,player.getLocation());
    }

     */

    public static void onKick(PlayerKickEvent event) {
        // 原因が飛行判定の時
        if (event.getReason().toLowerCase().contains("flying is not enabled")) {
            // 死んでる場合はkickしない
            if (event.getPlayer().isDead()) {
                event.setCancelled(true);
                return;
            }

            // ルートチェストの上に乗っている（下9ブロックをチェック）
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(1, -0.5, 1)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(1, -0.5, 0)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(1, -0.5, -1)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(0, -0.5, 1)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(0, -0.5, 0)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(0, -0.5, -1)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(-1, -0.5, 1)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(-1, -0.5, 0)))){
                event.setCancelled(true);
            }
            if (ItemAPI.isLootLocation(new BlockLoc(event.getPlayer().getLocation().add(-1, -0.5, -1)))){
                event.setCancelled(true);
            }
        }
    }
}
