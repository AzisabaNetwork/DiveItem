package com.flora30.diveitem.shop.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.util.GuiItem;
import com.flora30.divelib.util.PlayerItem;
import com.flora30.diveitem.item.data.ItemDataMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveconstant.data.item.ItemDataObject;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SellGUI {

    public static Inventory getGUI(){
        Inventory inv = Bukkit.createInventory(null,45,"ショップ - 売る");
        GuiItem.INSTANCE.grayBack(inv);
        inv.setItem(4,getIcon(Material.SUNFLOWER, ChatColor.WHITE+"緑色をクリック ‣ 売る",0));

        for (int i : getItemSlotList()){
            inv.setItem(i,null);
        }

        inv.setItem(40,getIcon(Material.LIME_STAINED_GLASS_PANE,ChatColor.WHITE+"売る",0));

        return inv;
    }

    private static List<Integer> getItemSlotList(){
        List<Integer> itemSlotList = new ArrayList<>();

        itemSlotList.add(10);
        itemSlotList.add(11);
        itemSlotList.add(12);
        itemSlotList.add(13);
        itemSlotList.add(14);
        itemSlotList.add(15);
        itemSlotList.add(16);

        itemSlotList.add(19);
        itemSlotList.add(20);
        itemSlotList.add(21);
        itemSlotList.add(22);
        itemSlotList.add(23);
        itemSlotList.add(24);
        itemSlotList.add(25);

        itemSlotList.add(28);
        itemSlotList.add(29);
        itemSlotList.add(30);
        itemSlotList.add(31);
        itemSlotList.add(32);
        itemSlotList.add(33);
        itemSlotList.add(34);

        return itemSlotList;
    }


    private static ItemStack getIcon(Material material, String name, int model){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName(name);
        meta.setCustomModelData(model);

        item.setItemMeta(meta);
        return item;
    }

    public static void onClick(InventoryClickEvent event){
        if (event.getClickedInventory() == null || event.getClickedInventory() instanceof PlayerInventory){
            return;
        }
        if (getItemSlotList().contains(event.getSlot())){
            return;
        }
        event.setCancelled(true);
        if (event.getSlot() == 40){
            sell((Player) event.getWhoClicked(), event.getClickedInventory());
        }
    }

    private static void sell(Player player, Inventory inventory){
        int total = 0;
        for (int slot : getItemSlotList()){
            ItemStack item = inventory.getItem(slot);
            if (item == null){
                continue;
            }
            if (item.getItemMeta() == null){
                returnItem(player,inventory,slot);
                continue;
            }
            //売るアイテムのID
            int id = ItemMain.INSTANCE.getItemId(item);
            if (id == -1){
                returnItem(player,inventory,slot);
                continue;
            }
            //売るアイテムの金額
            double money = ItemDataObject.INSTANCE.getItemDataMap().get(id).getMoney();
            if (money == 0){
                returnItem(player,inventory,slot);
                continue;
            }
            //売るアイテムの数
            int amount = item.getAmount();

            //売却
            total += money * amount;
            inventory.setItem(slot,null);
        }

        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        data.setMoney(data.getMoney()+total);

        if (total != 0){
            player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS,1,1);
            player.sendMessage(ChatColor.GREEN+"アイテムを売却しました(+"+total+"G)");
        }
    }

    private static void returnItem(Player player, Inventory inventory, int slot){
        PlayerItem.INSTANCE.giveItem(player,inventory.getItem(slot));
        inventory.setItem(slot,null);
    }

    public static void onClose(InventoryCloseEvent event){
        boolean isReturned = false;
        Inventory inventory = event.getInventory();
        for (int slot : getItemSlotList()){
            ItemStack item = inventory.getItem(slot);
            if (item == null){
                continue;
            }
            returnItem((Player) event.getPlayer(),event.getInventory(),slot);
            isReturned = true;
        }

        if (isReturned){
            event.getPlayer().sendMessage(ChatColor.GRAY+"アイテムを返却しました");
        }
    }
}
