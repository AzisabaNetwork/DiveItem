package com.flora30.diveitem.item;

import com.flora30.diveapi.event.CreateItemEvent;
import com.flora30.diveapi.event.GetItemEvent;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.gather.GatherConfig;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemStackMain {
    private static final ItemManager itemManager = new ItemManager((MythicMobs) Bukkit.getPluginManager().getPlugin("MythicMobs"));

    //一般用（アイテムそのもの）
    private static final Map<Integer, ItemStack> itemMap = new HashMap<>();
    private static final Map<Integer, String> mythicItemMap = new HashMap<>();


    //アイテムを追加＋保存する
    public static void setItem(int id, ItemStack item){
        if (mythicItemMap.containsKey(id)){
            mythicItemMap.remove(id);
        }else if(!itemMap.containsKey(id)){
            //どちらにもない＝新規作成
            CreateItemEvent event = new CreateItemEvent(id);
            Bukkit.getPluginManager().callEvent(event);
        }
        itemMap.put(id,item);

        ItemConfig ls = new ItemConfig();
        ls.save(id);
    }
    public static boolean setMMItem(int id, String mmName){
        ItemStack item = callMMItem(mmName);
        if(Objects.isNull(item)){
            return false;
        }

        if(itemMap.containsKey(id)){
            itemMap.remove(id);
        }
        else if(!mythicItemMap.containsKey(id)){
            //どちらにもない＝新規作成
            CreateItemEvent event = new CreateItemEvent(id);
            Bukkit.getPluginManager().callEvent(event);
        }
        mythicItemMap.put(id,mmName);
        Bukkit.getLogger().info(id + " : " + mmName);

        ItemConfig ls = new ItemConfig();
        ls.save(id);
        return true;
    }

    //追加（イベント発生なし）
    public static void putItem(int id, ItemStack item){
        itemMap.put(id,item);
    }
    public static void putMythicItem(int id, String name){
        mythicItemMap.put(id,name);
        Bukkit.getLogger().info(id + " : " + name);
    }


    // MMアイテムの呼び出し
    public static ItemStack callMMItem(String mmName){
        return itemManager.getItemStack(mmName);
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


    public static ItemStack getItem(int id){
        if (id == -1){
            return null;
        }

        GetItemEvent event = new GetItemEvent(id);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            Bukkit.getLogger().info("[DiveCore-Item]ID-"+id+"の情報生成がキャンセルされました（アイテム不足？）");
            return getNeutralItem(id);
        }
        return GatherConfig.setMineBlock(id, event.getItem());
    }

    // Asyncしてる
    public static ItemStack getItemAsync(int id){
        GetItemEvent event = new GetItemEvent(id,null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            Bukkit.getLogger().info("[DiveCore-Item]ID-"+id+"の情報生成がキャンセルされました（アイテム不足？）");
            return getNeutralItem(id);
        }
        return GatherConfig.setMineBlock(id, event.getItem());
    }

    public static ItemStack getItemWithValue(int id, String s1) {
        if (id == -1){
            return null;
        }

        GetItemEvent event = new GetItemEvent(id, s1);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            Bukkit.getLogger().info("[DiveCore-Item]ID-"+id+"の情報生成がキャンセルされました（アイテム不足？）");
            return getNeutralItem(id);
        }
        return GatherConfig.setMineBlock(id, event.getItem());
    }

    public static ItemStack getNeutralItem(int id){
        if (id == -1){
            return null;
        }

        if(mythicItemMap.containsKey(id)){
            return callMMItem(mythicItemMap.get(id));
        }
        else if (itemMap.containsKey(id)){
            return itemMap.get(id).clone();
        }
        else {
            return null;
        }
    }

    public static String getMythicName(int id){
        return mythicItemMap.get(id);
    }


    public static int getItemID(ItemStack item) {
        if (item == null || item.getItemMeta() == null)
            return -1;
        return Integer.parseInt(item.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(DiveItem.plugin, "id"), PersistentDataType.STRING, "-1"));
    }
}
