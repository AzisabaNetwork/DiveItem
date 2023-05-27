package com.flora30.diveitem.whistle;

import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveapin.event.HelpEvent;
import com.flora30.diveapin.event.HelpType;
import com.flora30.diveapin.util.GuiItem;
import com.flora30.diveapin.util.GuiItemType;
import com.flora30.diveapin.util.PlayerItem;
import com.flora30.divenew.data.Whistle;
import com.flora30.divenew.data.WhistleObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WhistleGUI {
    public static final List<Integer> sendRegion = List.of(10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33);
    public static final int sendPoint = 34;

    public static Inventory getGui(Player player) {
        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.WhistleGUI));

        Inventory inv = Bukkit.createInventory(null,45,"笛ランク");
        GuiItem.INSTANCE.grayBack(inv);

        inv.setItem(4, getWhistleIcon(player));
        inv.setItem(sendPoint, getSendIcon());
        for (int slot : sendRegion) {
            inv.setItem(slot, null);
        }

        return inv;
    }

    private static ItemStack getSendIcon() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        List<String> lore = new ArrayList<>();

        // 名前
        meta.setDisplayName(ChatColor.GOLD + "<<" + ChatColor.WHITE + " クリックで遺物を納品する " + ChatColor.GOLD +">>");

        // 説明
        lore.add("");
        lore.add(ChatColor.WHITE + "空いた場所に遺物を置いてください");
        lore.add(ChatColor.YELLOW + "遺物価値" + ChatColor.WHITE + "によって、" + ChatColor.YELLOW + "貢献値" + ChatColor.WHITE + "が上昇します");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getWhistleIcon(Player player) {
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        int exp = data.getLevelData().getWhistleExp();
        int nextExp = WhistleObject.INSTANCE.getWhistleExpMap().get(data.getLevelData().getWhistleRank() + 1);
        Whistle whistle = WhistleObject.INSTANCE.getWhistleMap().get(data.getLevelData().getWhistleRank());

        ItemStack item;
        switch (whistle.getType()) {
            case Red -> item = GuiItem.INSTANCE.getItem(GuiItemType.WhistleRed);
            case Blue -> item = GuiItem.INSTANCE.getItem(GuiItemType.WhistleBlue);
            case Moon -> item = GuiItem.INSTANCE.getItem(GuiItemType.WhistleMoon);
            case Black -> item = GuiItem.INSTANCE.getItem(GuiItemType.WhistleBlack);
            case White -> item = GuiItem.INSTANCE.getItem(GuiItemType.WhistleWhite);
            default -> throw new IllegalStateException("Unexpected value: " + whistle.getType());
        }

        // 貢献値について
        List<String> lore = new ArrayList<>();
        lore.add("");
        String expText = ChatColor.GOLD + "現在の貢献値 ‣ " + ChatColor.WHITE + exp;
        String nextExpText = ChatColor.GOLD +"（次まで ‣ " + ChatColor.WHITE + nextExp + ChatColor.GOLD + " ）";
        lore.add(expText + nextExpText);

        // 機能について
        lore.add("");
        lore.add(ChatColor.GOLD + "帰還可能な深度 ‣ " + ChatColor.WHITE + whistle.getReturnDepth());
        lore.add(ChatColor.GOLD + "エンダーチェストの容量 ‣ " + ChatColor.WHITE + whistle.getEnderCapacity());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(WhistleMain.getWhistleRankDisplay(whistle));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) return;
        int slot = e.getSlot();

        // 遺物を入れる場所：自由に出し入れできる
        if (sendRegion.contains(slot)) return;

        // その他の場所：アイテムを取得しない
        e.setCancelled(true);
        if (slot != sendPoint) return;

        // 遺物を納品する場所
        int exp = 0;
        for (int i : sendRegion) {
            ItemStack item = e.getClickedInventory().getItem(i);
            if (item == null) continue;
            int artifactValue = WhistleMain.getArtifactValue(item);
            // アーティファクトではない場合は-1が返ってくる（たぶん NullPointer の分岐）
            if (artifactValue > 0) {
                exp += artifactValue;
                e.getClickedInventory().setItem(i, null);
            }
        }

        // 貢献値を適用して表示を出す
        if (exp == 0) {
            e.getWhoClicked().sendMessage("遺物を確認できませんでした・・・");
        }
        else {
            e.getWhoClicked().sendMessage("貢献値が " +ChatColor.GOLD+ exp + ChatColor.WHITE + " 増加しました");
            WhistleMain.addWhistleExp((Player) e.getWhoClicked(),exp);
            // 経験値もあげる
            CoreAPI.addExp((Player) e.getWhoClicked(), exp * 5);

            // GUI更新
            e.getClickedInventory().setItem(4, getWhistleIcon((Player) e.getWhoClicked()));
        }
    }

    public static void onClose(InventoryCloseEvent e) {
        for (int slot : sendRegion) {
            if (e.getInventory().getItem(slot) != null) {
                PlayerItem.INSTANCE.giveItem((Player) e.getPlayer(), e.getInventory().getItem(slot));
            }
        }
    }
}
