package com.flora30.diveitem.loot.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.diveitem.loot.LootConfig;
import com.flora30.divelib.data.loot.LootObject;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class LootAdminGUITrigger {

    public static void onClose(InventoryCloseEvent e){
        Inventory gui = e.getInventory();
        ArrayList<LootObject.ItemAmount> list = new ArrayList<>();

        for (int i = 0; i < 54; i++){
            ItemStack item = gui.getItem(i);
            if (item == null){
                continue;
            }

            int id = ItemMain.INSTANCE.getItemId(item);
            if (id == -1){
                continue;
            }

            int amount = item.getAmount();

            list.add(new LootObject.ItemAmount(id,amount));
            Bukkit.getLogger().info("報酬を追加："+id+"（"+amount+"個）");
        }

        String title = e.getView().getTitle();
        String lootID = title.replace("loot報酬 - ","");
        LootObject.INSTANCE.getLootItemMap().put(lootID, list);
        Bukkit.getLogger().info("loot報酬を登録しました（"+lootID+"）");

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(lootID);
    }
}
