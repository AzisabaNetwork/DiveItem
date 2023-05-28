package com.flora30.diveitem.loot.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.loot.LootConfig;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.diveconstant.data.loot.Loot;
import com.flora30.diveconstant.data.loot.LootObject;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootAdminGUITrigger {

    public static void onClose(InventoryCloseEvent e){
        Inventory gui = e.getInventory();
        ArrayList<Loot.ItemAmount> list = new ArrayList<>();

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

            list.add(new Loot.ItemAmount(id,amount));
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

        LootObject.INSTANCE.getLootMap().get(layer).getItemList().set(level,list);
        Bukkit.getLogger().info("loot報酬を登録しました（"+split[0]+" - "+split[1]+"）");

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(layer);
    }
}
