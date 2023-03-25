package com.flora30.diveitem.shop.gui;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.tools.GuiItem;
import com.flora30.diveapi.tools.PlayerItem;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.shop.ShopGood;
import com.flora30.diveitem.shop.ShopMain;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BuyGUI {
    public static Inventory getGUI(Player player, int id){
        Inventory inv = Bukkit.createInventory(null,45,"ショップ - 買う");
        GuiItem.grayBack(inv);
        inv.setItem(4,getIcon(Material.BARREL,ChatColor.WHITE+"商品をクリック ‣ 買う",id));

        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        inv.setItem(5,getIcon(Material.GOLD_NUGGET,ChatColor.GOLD+"所持金 ‣ "+data.money+"G",0));

        List<ShopGood> goodSet = ShopMain.shopMap.get(id).getGoodsList();

        int i = 0;
        for (ShopGood good : goodSet){
            inv.setItem(getSlot(i), getGoodIcon(good));
            i++;
            if (i >= 21){
                break;
            }
        }
        for (;i<21;i++){
            inv.setItem(getSlot(i),null);
        }

        return inv;
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

    private static int getShopId(ItemStack item){
        if (item == null || item.getItemMeta() == null){
            return -1;
        }
        ItemMeta meta = item.getItemMeta();

        return meta.getCustomModelData();
    }

    private static ItemStack getGoodIcon(ShopGood good){
        ItemStack item = ItemStackMain.getItem(good.getItemId());
        item.setAmount(good.getAmount());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        List<String> lore = meta.getLore();
        assert lore != null;
        lore.add(0, ChatColor.GOLD +"――――――――――――");
        lore.add(0,ChatColor.WHITE +"必要金額 ‣ "+ChatColor.GOLD+good.getMoney());
        lore.add(0, ChatColor.GOLD +"――――――――――――");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private static int getSlot(int number){
        if (number <= 6){
            return number + 10;
        }
        else if(number <= 13){
            return number + 12;
        }
        else{
            return number + 14;
        }
    }

    private static int getNumber(int slot){
        if (10 <= slot && slot <= 16){
            return slot - 10;
        }

        if(19 <= slot && slot <= 25){
            return slot - 12;
        }

        if (28 <= slot && slot <= 34){
            return slot - 14;
        }
        return -1;
    }


    public static void onClick(InventoryClickEvent event){
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() instanceof PlayerInventory) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        int shopId = getShopId(event.getClickedInventory().getItem(4));

        //買う商品の入っている場所
        int number = getNumber(event.getSlot());
        if (number == -1){
            return;
        }

        int i = 0;
        ShopGood good = null;
        //商品を検索
        for (ShopGood insGood : ShopMain.shopMap.get(shopId).getGoodsList()){
            if (i != number){
                i++;
                continue;
            }

            good = insGood;
            break;
        }
        if (good == null){
            return;
        }

        //購入判定
        buy(player,good);
    }

    public static void buy(Player player, ShopGood good){
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        int require = good.getMoney();

        if (data.money < require){
            player.sendMessage(ChatColor.RED+"所持金が足りないようだ……");
            return;
        }

        data.money = data.money - require;
        player.getOpenInventory().getTopInventory().setItem(5,getIcon(Material.GOLD_NUGGET,ChatColor.GOLD+"所持金 ‣ "+data.money+"G",0));

        //アイテムを渡す
        ItemStack item = ItemStackMain.getItem(good.getItemId());
        item.setAmount(good.getAmount());
        PlayerItem.giveItem(player,item);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        player.sendMessage(meta.getDisplayName()+ChatColor.GREEN+"を購入しました");
        player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS,1,1);
    }
}
