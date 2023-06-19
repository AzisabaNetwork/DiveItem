package com.flora30.diveitem.loot.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.data.LayerObject;
import com.flora30.divelib.data.gimmick.action.ChestType;
import com.flora30.divelib.data.loot.LootObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootAdminGUI {

    public static void open(Player player, String lootID) {
        Inventory gui = create(lootID);
        player.openInventory(gui);
    }

    private static Inventory create(String lootID) {
        Inventory gui = Bukkit.createInventory(null,54,"loot報酬 - "+lootID);
        List<LootObject.ItemAmount> itemList = LootObject.INSTANCE.getLootItemMap().get(lootID);
        for (int i = 0; i < 54; i++){
            if (i >= itemList.size()){
                break;
            }

            ItemStack item = ItemMain.INSTANCE.getItem(itemList.get(i).getItemId());
            if (item == null) continue;
            item.setAmount(itemList.get(i).getAmount());
            gui.setItem(i,item);
        }

        return gui;
    }
}
