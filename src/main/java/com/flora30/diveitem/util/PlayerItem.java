package com.flora30.diveitem.util;

import com.flora30.diveapi.plugins.ItemAPI;
import com.flora30.diveitem.DiveItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerItem {

    public static int countItem(Player player, int id, boolean isOnlyPlayerInv){
        int count = 0;
        Inventory inventory = player.getInventory();
        for(ItemStack item : inventory){
            if(getInt(item,"id") == id){
                count += item.getAmount();
            }
        }

        if (!isOnlyPlayerInv){
            //エンダーチェスト判定
            Inventory ender = player.getEnderChest();
            for (ItemStack item : ender){
                if(getInt(item,String.valueOf(id)) == id){
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

    public static void takeItem(Player player, int id, int amount){
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory){
            //ID一致したとき
            if (item == null){
                continue;
            }
            if (ItemAPI.getItemID(item) == id){
                int current =item.getAmount();
                //currentが多い＝終了
                if (current > amount ){
                    item.setAmount(current - amount);
                    amount = 0;
                }
                //amount以下
                else{
                    amount -= item.getAmount();
                    inventory.remove(item);
                }
            }

            if (amount <= 0){
                break;
            }
        }
    }

    public static void limitItem(Player player, int id, int amount){
        Inventory inventory = player.getInventory();
        Inventory ender = player.getEnderChest();
        for (ItemStack item : inventory){
            //ID一致したとき
            if (item == null){
                continue;
            }
            if (ItemAPI.getItemID(item) == id){
                int current =item.getAmount();
                //currentが多い＝終了
                if (current > amount ){
                    item.setAmount(current - amount);
                    //エンダーチェストへ移動
                    ItemStack decreased = item.clone();
                    decreased.setAmount(amount);
                    ender.addItem(decreased);
                    amount = 0;
                }
                //amount以下＝エンダーチェストへ移動
                else{
                    amount -= item.getAmount();
                    ender.addItem(item);
                    inventory.remove(item);
                }
            }

            if (amount <= 0){
                break;
            }
        }

        ItemMeta meta = ItemAPI.getNeutralItem(id).getItemMeta();
        if (meta == null){
            Bukkit.getLogger().info("[DiveCore-Limit]アイテム["+id+"]のmeta取得に失敗しました");
            return;
        }
        String name = meta.getDisplayName();
        player.sendMessage(ChatColor.GRAY+name+"が所持数制限を超えたため、"+amount+"個エンダーチェストに送りました");
    }

    public static String getString(ItemStack item, String key){
        if(item == null ||item.getItemMeta() == null){
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(DiveItem.plugin,key), PersistentDataType.STRING);
    }

    public static int getInt(ItemStack item, String key) {
        if (item == null || item.getItemMeta() == null) {
            return -1;
        }
        try {
            return Integer.parseInt(item.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(DiveItem.plugin, key), PersistentDataType.STRING, "-1"));
        } catch (NullPointerException e) {
            return -1;
        }
    }

    /*
    public static double getDouble(ItemStack item, String key){
            if (item == null ||item.getItemMeta() == null) {
                return -1;
            }
            try {
                return item.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(DiveCore.plugin, key), PersistentDataType.DOUBLE, -1.0);
            } catch (NullPointerException e) {
                return -1;
            }
    }

     */
}
