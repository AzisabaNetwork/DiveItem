package com.flora30.diveitem.trade;

import com.flora30.divelib.data.TradeData;
import com.flora30.divelib.data.TradeObject;
import com.flora30.divelib.data.TradePhase;
import com.flora30.divelib.util.GuiItem;
import com.flora30.divelib.util.PlayerItem;
import com.flora30.diveitem.DiveItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeGUI {
    private static final List<Integer> fromRegion = List.of(
            0,1,2,3,
            9,10,11,12,
            18,19,20,21,
            27,28,29);
    private static final List<Integer> toRegion = List.of(
            5,6,7,8,
            14,15,16,17,
            23,24,25,26,
            33,34,35);

    public static void openGUI(Player from, Player to) {
        Inventory inv = Bukkit.createInventory(null,36,"トレード -> " +to.getDisplayName());
        for (int i = 4; i < 36; i = i + 9) {
            inv.setItem(i, GuiItem.INSTANCE.getItem(Material.GRAY_STAINED_GLASS_PANE));
        }
        inv.setItem(30, getIncompleteIcon());

        from.openInventory(inv);
    }

    private static ItemStack getIncompleteIcon() {
        ItemStack icon = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "クリック ‣ 送るアイテムを決定する");
        icon.setItemMeta(meta);
        return icon;
    }

    private static ItemStack getCompletedIcon() {
        ItemStack icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + "送るアイテムを決定しました");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "クリック ‣ 送るアイテムを選び直す");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    public static void onDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    /**
     * 自分の更新
     * タイトルの条件分岐済み
     */
    public static void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        switch (event.getClick()) {
            // ダブルクリックは無効化
            case DOUBLE_CLICK -> {
                event.setCancelled(true);
                return;
            }
            // Shiftクリックは下のみ無効化
            case SHIFT_LEFT,SHIFT_RIGHT -> {
                if (event.getClickedInventory() == event.getView().getBottomInventory()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (event.getClickedInventory() == event.getView().getBottomInventory()) return;


        TradeData data = TradeObject.INSTANCE.getTradeMap().get(event.getWhoClicked().getUniqueId());
        Inventory inv = event.getClickedInventory();
        // 自分の場所
        if (fromRegion.contains(event.getSlot())) {
            if (data.getPhase() != TradePhase.Prepare) {
                event.setCancelled(true);
                return;
            }

            // 更新は遅らせる -> クリック後のインベントリを読み取る
            DiveItem.plugin.delayedTask(1, () -> {
                // 送るアイテムを更新する
                for (int i = 0; i < 15; i++) {
                    data.getItems().set(i,inv.getItem(fromRegion.get(i)));
                    // 背景板が送られる対策
                    if (data.getItems().get(i) != null && data.getItems().get(i).getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
                        data.getItems().set(i, null);
                    }

                    // デバッグ：送るアイテムを表示
                    /*
                    if (data.items.get(i) != null && data.items.get(i).getItemMeta() != null) {
                        event.getWhoClicked().sendMessage(data.items.get(i).getItemMeta().getDisplayName() + " -> "+data.items.get(i).getAmount()+"個");
                    }
                     */
                }
            });



            return;
        }

        event.setCancelled(true);

        // 準備ボタン分岐
        if (event.getSlot() == 30) {

            // 準備完了
            if (data.getPhase() == TradePhase.Prepare) {
                for (int i : fromRegion) {
                    if (inv.getItem(i) == null) {
                        inv.setItem(i,GuiItem.INSTANCE.getItem(Material.GRAY_STAINED_GLASS_PANE));
                    }
                }
                inv.setItem(30,getCompletedIcon());
                data.setPhase(TradePhase.Complete);
                ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_BEEHIVE_ENTER,1,1);
            }
            // 準備中に戻す
            else {
                for (int i : fromRegion) {
                    ItemStack item = inv.getItem(i);
                    if (item != null && item.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                        inv.setItem(i,null);
                    }
                }
                inv.setItem(30,getIncompleteIcon());
                data.setPhase(TradePhase.Prepare);
                ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_BEEHIVE_EXIT,1,1);
            }
        }
    }

    /**
     * 中断時の返却
     * タイトルの条件分岐済み
     */
    public static void onClose(InventoryCloseEvent event) {
        TradeData data = TradeObject.INSTANCE.getTradeMap().get(event.getPlayer().getUniqueId());
        // トレードが完了した後はデータが消えている
        if (data == null) return;

        // トレードが完了していない場合
        // 自分の手持ちを返却する
        Player player = (Player) event.getPlayer();
        for (ItemStack item : data.getItems()) {
            if (item != null) {
                PlayerItem.INSTANCE.giveItem(player,item);
            }
        }
        player.sendMessage("トレードが中断されたため、アイテムを返却しました");
        // 自分の状態を初期に戻す(相手を記憶しておく)
        UUID to = data.getTo();
        TradeObject.INSTANCE.getTradeMap().remove(event.getPlayer().getUniqueId());

        // 相手が存在する場合 → 強制キャンセル
        if (TradeObject.INSTANCE.getTradeMap().containsKey(to)) {
            Player toPlayer = Bukkit.getPlayer(to);
            if (toPlayer != null) {
                toPlayer.closeInventory();
            }
        }
    }


    /**
     * 相手側の更新、トレード判定
     */
    public static void onTick(Player player) {
        if (!player.getOpenInventory().getTitle().contains("トレード -> ")) return;

        TradeData data = TradeObject.INSTANCE.getTradeMap().get(player.getUniqueId());
        Player to = Bukkit.getPlayer(data.getTo());
        if (to == null) {
            player.closeInventory();
            player.sendMessage("相手が見つからないため、トレードを中断しました");
            return;
        }

        // トレードを試す
        if (TradeMain.trade(player,to)) return;

        // 相手側を表示する領域の更新
        Inventory inv = player.getOpenInventory().getTopInventory();
        TradeData toData = TradeObject.INSTANCE.getTradeMap().get(data.getTo());
        for (int i = 0; i < 15; i++) {
            ItemStack item = toData.getItems().get(i);
            if (item != null) {
                inv.setItem(toRegion.get(i),item);
            }
            else {
                inv.setItem(toRegion.get(i), toData.getPhase() == TradePhase.Prepare ? null : GuiItem.INSTANCE.getItem(Material.GRAY_STAINED_GLASS_PANE));
            }
        }
        inv.setItem(32, toData.getPhase() == TradePhase.Prepare ? null : GuiItem.INSTANCE.getItem(Material.GRAY_STAINED_GLASS_PANE));
        //player.sendMessage(to.getDisplayName()+"のプレビュー表示");
    }
}
