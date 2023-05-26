package com.flora30.diveitem.item;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.event.CreateItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemStackMain {


    //アイテムを追加＋保存する
    public static void setItem(int id, ItemStack item){
        if (ItemMain.INSTANCE.getMythicItemMap().containsKey(id)){
            ItemMain.INSTANCE.getMythicItemMap().remove(id);
        }else if(!ItemMain.INSTANCE.getItemMap().containsKey(id)){
            //どちらにもない＝新規作成
            CreateItemEvent event = new CreateItemEvent(id);
            Bukkit.getPluginManager().callEvent(event);
        }
        ItemMain.INSTANCE.getItemMap().put(id,item);

        ItemConfig ls = new ItemConfig();
        ls.save(id);
    }
    public static boolean setMMItem(int id, String mmName){
        ItemStack item = ItemMain.INSTANCE.callMMItem(mmName);

        if(ItemMain.INSTANCE.getItemMap().containsKey(id)){
            ItemMain.INSTANCE.getItemMap().remove(id);
        }
        else if(!ItemMain.INSTANCE.getMythicItemMap().containsKey(id)){
            //どちらにもない＝新規作成
            CreateItemEvent event = new CreateItemEvent(id);
            Bukkit.getPluginManager().callEvent(event);
        }
        ItemMain.INSTANCE.getMythicItemMap().put(id,mmName);
        Bukkit.getLogger().info(id + " : " + mmName);

        ItemConfig ls = new ItemConfig();
        ls.save(id);
        return true;
    }

    /*
    public static String getAnotherLore(String mmName, String key){
        Optional<MythicItem> ins = itemManager.getItem(mmName);
        if (ins.isPresent()){
            return ins.get().getConfig().getString(key);
        }
        return null;
    }

     */

}
