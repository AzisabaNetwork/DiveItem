package com.flora30.diveitem.shop.gui;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveapin.util.GuiItem;
import com.flora30.diveapin.util.PlayerItem;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divenew.data.ShopItem;
import com.flora30.divenew.data.ShopObject;
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
        GuiItem.INSTANCE.grayBack(inv);
        inv.setItem(4,getIcon(Material.BARREL,ChatColor.WHITE+"商品をクリック ‣ 買う",id));

        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        inv.setItem(5,getIcon(Material.GOLD_NUGGET,ChatColor.GOLD+"所持金 ‣ "+data.getMoney()+"G",0));

        List<ShopItem> shopItems = ShopObject.INSTANCE.getShopMap().get(id);

        int i = 0;
        for (ShopItem item : shopItems){
            inv.setItem(getSlot(i), getGoodIcon(item));
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

    private static ItemStack getGoodIcon(ShopItem shopItem){
        ItemStack item = ItemMain.INSTANCE.getItem(shopItem.getItemId());
        item.setAmount(shopItem.getAmount());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        List<String> lore = meta.getLore();
        assert lore != null;
        lore.add(0, ChatColor.GOLD +"――――――――――――");
        lore.add(0,ChatColor.WHITE +"必要金額 ‣ "+ChatColor.GOLD+shopItem.getMoney());
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
        ShopItem shopItem = null;
        //商品を検索
        for (ShopItem insItem : ShopObject.INSTANCE.getShopMap().get(shopId)){
            if (i != number){
                i++;
                continue;
            }

            shopItem = insItem;
            break;
        }
        if (shopItem == null){
            return;
        }

        //購入判定
        buy(player,shopItem);
    }

    public static void buy(Player player, ShopItem shopItem){
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        int require = shopItem.getMoney();

        if (data.getMoney() < require){
            player.sendMessage(ChatColor.RED+"所持金が足りないようだ……");
            return;
        }

        data.setMoney(data.getMoney()-require);
        player.getOpenInventory().getTopInventory().setItem(5,getIcon(Material.GOLD_NUGGET,ChatColor.GOLD+"所持金 ‣ "+data.getMoney()+"G",0));

        //アイテムを渡す
        ItemStack item = ItemMain.INSTANCE.getItem(shopItem.getItemId());
        item.setAmount(shopItem.getAmount());
        PlayerItem.INSTANCE.giveItem(player,item);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        player.sendMessage(meta.getDisplayName()+ChatColor.GREEN+"を購入しました");
        player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS,1,1);
    }
}
