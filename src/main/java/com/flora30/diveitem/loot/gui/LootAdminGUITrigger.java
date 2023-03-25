package com.flora30.diveitem.loot.gui;

import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.loot.LootGoods;
import com.flora30.diveitem.loot.LootConfig;
import com.flora30.diveitem.loot.LootMain;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootAdminGUITrigger {

    public static void onClose(InventoryCloseEvent e){
        Inventory gui = e.getInventory();
        LootGoods list = new LootGoods();

        for (int i = 0; i < 54; i++){
            ItemStack item = gui.getItem(i);
            if (item == null){
                continue;
            }

            int id = ItemStackMain.getItemID(item);
            if (id == -1){
                continue;
            }

            int amount = item.getAmount();

            list.addItem(id,amount);
            Bukkit.getLogger().info("報酬を追加："+id+"（"+amount+"個）");
        }

        String title = e.getView().getTitle();
        String[] split = title.replace("loot報酬 - ","").split(" - ");
        int level = 1;
        try{
            level = Integer.parseInt(split[1]);
        } catch (NumberFormatException ex){
            Bukkit.getLogger().info("[DiveCore-Loot] "+split[1]+"はレベルではありません");
        }
        String layer = split[0];

        LootMain.getLoot(layer).setLootGoods(level,list);
        Bukkit.getLogger().info("loot報酬を登録しました（"+split[0]+" - "+split[1]+"）");

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(layer);
    }
}
