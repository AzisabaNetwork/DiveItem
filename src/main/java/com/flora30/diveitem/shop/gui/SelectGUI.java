package com.flora30.diveitem.shop.gui;

import com.flora30.divelib.util.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectGUI {
    public static Inventory getGUI(int id){
        Inventory inv = Bukkit.createInventory(null,27,"ショップ - 何をしますか？");
        GuiItem.INSTANCE.grayBack(inv);

        inv.setItem(11,getIcon(Material.BARREL,ChatColor.WHITE+"購入する",id));
        inv.setItem(15,getIcon(Material.SUNFLOWER, ChatColor.WHITE+"売却する",0));

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

    public static void onClick(InventoryClickEvent event){
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() instanceof PlayerInventory) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        switch (event.getSlot()) {
            case 11 -> {
                int id = getId(event.getClickedInventory().getItem(event.getSlot()));
                player.openInventory(BuyGUI.getGUI(player, id));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            case 15 -> {
                player.openInventory(SellGUI.getGUI());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }
    }

    private static int getId(ItemStack item){
        if (item == null || item.getItemMeta() == null){
            return -1;
        }
        ItemMeta meta = item.getItemMeta();

        return meta.getCustomModelData();
    }
}
