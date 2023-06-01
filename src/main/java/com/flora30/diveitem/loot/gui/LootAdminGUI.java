package com.flora30.diveitem.loot.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.divelib.data.loot.Loot;
import com.flora30.divelib.data.loot.LootObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootAdminGUI {

    public static void open(Player player, String layer, int level) {
        Inventory gui = create(layer, level);
        player.openInventory(gui);
    }

    private static Inventory create(String layer, int level) {
        Inventory gui = Bukkit.createInventory(null,54,"loot報酬 - "+layer+" - "+level);
        List<Loot.ItemAmount> itemList = LootObject.INSTANCE.getLootMap().get(layer).getItemList().get(level);
        for (int i = 0; i < 54; i++){
            if (i >= itemList.size()){
                break;
            }

            ItemStack item = ItemMain.INSTANCE.getItem(itemList.get(i).getItemId());
            item.setAmount(itemList.get(i).getAmount());
            gui.setItem(i,item);
        }

        return gui;
    }
}
