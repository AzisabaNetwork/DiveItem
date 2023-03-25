package com.flora30.diveitem.loot.gui;

import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.loot.LootGoods;
import com.flora30.diveitem.loot.LootMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootAdminGUI {

    public static void open(Player player, String layer, int level) {
        Inventory gui = create(layer, level);
        player.openInventory(gui);
    }

    private static Inventory create(String layer, int level) {
        Inventory gui = Bukkit.createInventory(null,54,"loot報酬 - "+layer+" - "+level);
        LootGoods goods = LootMain.getLoot(layer).getLootGood(level);
        for (int i = 0; i < 54; i++){
            if (i >= goods.getItemList().size()){
                break;
            }
            ItemStack item = ItemStackMain.getItem(goods.getGood(i).getItemID());
            item.setAmount(goods.getGood(i).getAmount());
            gui.setItem(i,item);
        }

        return gui;
    }
}
