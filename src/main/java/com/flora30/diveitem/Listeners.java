package com.flora30.diveitem;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.event.*;
import com.flora30.diveitem.craft.gui.CraftGUI;
import com.flora30.diveitem.craft.gui.CraftListGUI;
import com.flora30.diveitem.craft.gui.RecipeEditorGUI;
import com.flora30.diveitem.item.data.ItemDataListener;
import com.flora30.diveitem.gather.GatherTrigger;
import com.flora30.diveitem.item.ItemEntityMain;
import com.flora30.diveitem.item.ItemTrigger;
import com.flora30.diveitem.loot.LootTrigger;
import com.flora30.diveitem.loot.gui.LootAdminGUITrigger;
import com.flora30.diveitem.mythic.MythicMain;
import com.flora30.diveitem.rope.RopeListener;
import com.flora30.diveitem.shop.gui.BuyGUI;
import com.flora30.diveitem.shop.gui.SelectGUI;
import com.flora30.diveitem.shop.gui.SellGUI;
import com.flora30.diveitem.trade.TradeGUI;
import com.flora30.diveitem.trade.TradeMain;
import com.flora30.diveitem.whistle.WhistleGUI;
import com.flora30.diveconstant.data.item.ItemData;
import com.flora30.diveconstant.data.item.ItemDataObject;
import com.flora30.diveconstant.data.item.ItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Listeners implements Listener, CommandExecutor {
/*
    @EventHandler
    public void onCommand(ServerCommandEvent e){
        List<String> commands = Arrays.asList(e.getCommand().split(" "));

        switch (commands.get(0)) {
            case "item":
                ItemTrigger.onCommand(commands);
        }
    }
 */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getLogger().info("listen command");
        //コマンドの実行者がプレイヤーだった時
        if (sender instanceof Player player) {

            String subCommand = args.length == 0 ? "" : args[0];
            String[] subCommands = new String[10];
            for (int i = 1; i <= 10; i++) {
                try{
                    subCommands[i-1] = args[i];
                } catch (IllegalArgumentException| ArrayIndexOutOfBoundsException | NullPointerException e){
                    subCommands[i-1] = "";
                }
            }
            //subCommandに引数を入れる（null対応）

            switch (command.getName()) {
                case "item" -> {
                    ItemTrigger.onCommand(player, subCommand, subCommands[0], subCommands[1]);
                    return true;
                }
                case "loot" -> {
                    LootTrigger.onCommand(player, subCommand, subCommands[0], subCommands[1]);
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        String title = e.getWhoClicked().getOpenInventory().getTitle();
        switch (title) {
            case "クラフト" -> CraftGUI.onDrag(e);
        }
        if (title.contains("トレード -> ")) {
            TradeGUI.onDrag(e);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        String title = e.getWhoClicked().getOpenInventory().getTitle();
        switch (title) {
            case "ルートチェスト" -> LootTrigger.onInventoryClick(e);
            case "ショップ - 何をしますか？" -> SelectGUI.onClick(e);
            case "ショップ - 買う" -> BuyGUI.onClick(e);
            case "ショップ - 売る" -> SellGUI.onClick(e);
            case "クラフト一覧" -> CraftListGUI.onClick(e);
            case "クラフト" -> CraftGUI.onClick(e);
            case "笛ランク" -> WhistleGUI.onClick(e);
        }
        if(title.contains("クラフト（編集：")){
            RecipeEditorGUI.onClick(e);
        }
        if (title.contains("トレード -> ")) {
            TradeGUI.onClick(e);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        String title = e.getView().getTitle();
        switch(title){
            case "ショップ - 売る" -> SellGUI.onClose(e);
            case "クラフト" -> CraftGUI.onClose(e);
            case "笛ランク" -> WhistleGUI.onClose(e);
        }
        if(title.contains("宝箱")){
            LootTrigger.onInventoryClose(e);
        }
        if (title.contains("loot報酬")){
            LootAdminGUITrigger.onClose(e);
        }
        if(title.contains("クラフト（編集：")){
            RecipeEditorGUI.onClose(e);
        }
        if (title.contains("トレード -> ")) {
            TradeGUI.onClose(e);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        switch(e.getAction()){
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                return;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                RopeListener.onInteract(e);
                if(Objects.isNull(e.getClickedBlock())){
                    return;
                }
                switch(e.getClickedBlock().getType()){
                    case CHEST:
                        LootTrigger.onClick(e);
                        return;
                    default:
                }
            default:
        }
    }

    @EventHandler
    public void onPutItem(PutItemEntityEvent e) {
        ItemEntityMain.onPutItem(e);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        TradeMain.onInteract(e);
    }

    @EventHandler
    public void onFirstJoin(FirstJoinEvent e){
        ItemTrigger.onFirstJoin(e);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent e){
        ItemEntityMain.onPickupItem(e);
    }

    @EventHandler
    public void onGetItem(GetItemEvent e){
        ItemDataListener.onGetItem(e);
    }

    @EventHandler
    public void onSaveItem(SaveItemEvent e) {
        ItemDataListener.onSaveItem(e);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e){

        String title = e.getPlayer().getOpenInventory().getTitle();
        switch (title) {
            case "ショップ - 何をしますか？", "クラフト", "ショップ - 売る", "ショップ - 買う", "エンダーチェスト", "笛ランク", "クラフト一覧" -> e.setCancelled(true);
        }
        if(title.contains("クラフト（編集：")){
            e.setCancelled(true);
        }
        if (title.contains("トレード -> ")) {
            e.setCancelled(true);
        }

        ItemEntityMain.onDrop(e);
    }

    @EventHandler
    public void onMerge(ItemMergeEvent e) {
        ItemEntityMain.onMerge(e);
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent e) {
        ItemEntityMain.onDespawn(e);
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        // pvpは禁止
        Entity caught = e.getCaught();
        if (caught instanceof Player) {
            e.setCancelled(true);
            return;
        }

        GatherTrigger.onFish(e);
    }

    @EventHandler
    public void onLayerChange(LayerChangeEvent e){
        LootTrigger.onLayerChange(e);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // 100%の確率で遺物を削除
        Player player = e.getPlayer();
        int size = player.getInventory().getSize();
        int count = 0;
        for (int i = 0; i < size; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getItemMeta() == null) continue;
            ItemData data = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(item));
            if (data == null) continue;

            if (data.getType() == ItemType.Artifact) {
                player.getInventory().setItem(i,null);
                count++;
            }
        }
        player.sendMessage(ChatColor.WHITE + "全ての遺物 ("+count+"個) が失われた・・・");
    }


    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        LootTrigger.onPlace(e);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        LootTrigger.onBreak(e);
        GatherTrigger.onBreakBlock(e);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        RopeListener.onQuit(e);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        RopeListener.onPlayerKick(e);
        LootTrigger.onKick(e);

        if (!e.isCancelled()) {
            Bukkit.getLogger().warning(e.getPlayer().getDisplayName()+"がロープ・チェストの近くにいないため、フライキックしました");
        }
    }

    /*
    @EventHandler
    public void onLayerChange(LayerChangeEvent e){
        LootTrigger.onLayerChange(e);
    }

     */


    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e){
        ItemEntityMain.onAttackMob(e);
    }

    public static int gatherSendTick = 5;
    public static int ropeTick = 5;
    public static int lootSendTick = 10;
    public static int tradeGuiTick = 5;


    //とりあえずshiftキーで
    //1Tickごとに送られている
    private static int count = 0;
    public void onTimer() {
        count++;
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        ItemEntityMain.onTick();
        TradeMain.onTick();
        if (count % ropeTick == 0) {
            RopeListener.onTick();
        }
            /* tradeログ表示
        if (count % 10 == 0) {
            TradeMain.onTickLog();
        }
             */
        for (Player player : players) {
            MythicMain.scoreCheck(player);
            if (count % lootSendTick == 0) {
                LootTrigger.onTickSendChest(player);
            }
            if (count % gatherSendTick == 0){
                //DiveCore.plugin.asyncTask(() -> GatherTrigger.onGatherSendTick(player));
                GatherTrigger.onGatherSendTick(player);
            }
            if (count % ropeTick == 0) {
                RopeListener.onTickPlayer(player);
            }
            if (count % tradeGuiTick == 0) {
                TradeGUI.onTick(player);
            }
        }
    }
}
