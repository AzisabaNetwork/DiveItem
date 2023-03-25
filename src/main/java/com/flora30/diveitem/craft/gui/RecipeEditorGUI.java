package com.flora30.diveitem.craft.gui;

import com.flora30.diveapi.tools.GuiItem;
import com.flora30.diveitem.craft.CraftConfig;
import com.flora30.diveitem.craft.CraftMain;
import com.flora30.diveitem.craft.HideType;
import com.flora30.diveitem.craft.Recipe;
import com.flora30.diveitem.item.ItemStackMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.flora30.diveitem.craft.gui.CraftGUI.*;

public class RecipeEditorGUI {

    public static Inventory getGui(int recipeId) {
        // 準備
        Recipe recipe = CraftMain.recipeMap.get(recipeId);
        if (recipe == null) recipe = new Recipe(new int[12],new HideType[12],1);
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "クラフト（編集："+recipeId+"）");
        GuiItem.grayBack(gui);

        // 表示アイテムを取得してguiの左側に置く
        for (int i = 0; i < 12; i++) {
            gui.setItem(recipeRegion.get(i), ItemStackMain.getItem(recipe.materials[i]));

            if (recipe.hides[i] == null) {
                gui.setItem(craftRegion.get(i),null);
                continue;
            }

            switch (recipe.hides[i]) {
                case White -> gui.setItem(craftRegion.get(i), new ItemStack(Material.WHITE_WOOL));
                case Gray -> gui.setItem(craftRegion.get(i), new ItemStack(Material.GRAY_WOOL));
                case Black -> gui.setItem(craftRegion.get(i), new ItemStack(Material.BLACK_WOOL));
                default -> gui.setItem(craftRegion.get(i), null);
            }
        }

        // その他を置く
        ItemStack product = ItemStackMain.getItem(recipeId);
        assert product != null;
        product.setAmount(recipe.amount);
        gui.setItem(4, product);
        gui.setItem(0,GuiItem.getItem(Material.WOODEN_AXE));
        gui.setItem(2,GuiItem.getItem(Material.WOODEN_AXE));
        gui.setItem(6,GuiItem.getItem(Material.WOODEN_AXE));
        gui.setItem(8,GuiItem.getItem(Material.WOODEN_AXE));

        return gui;
    }

    public static void onClick(InventoryClickEvent e){
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) return;
        if (craftRegion.contains(e.getSlot())) return;
        if (recipeRegion.contains(e.getSlot())) return;
        if (e.getSlot() == 4) return;

        e.setCancelled(true);
    }


    public static void onClose(InventoryCloseEvent e) {
        boolean mExist = false;
        Inventory inv = e.getInventory();
        int recipeId = ItemStackMain.getItemID(inv.getItem(4));

        int[] materials = new int[12];
        HideType[] hides = new HideType[12];

        for (int i = 0; i < 12; i++) {
            int slot = recipeRegion.get(i);
            ItemStack item = e.getInventory().getItem(slot);
            if (item == null) continue;

            int id = ItemStackMain.getItemID(item);
            if (id == -1) continue;

            materials[i] = id;
            mExist = true;
        }

        if (!mExist) {
            e.getPlayer().sendMessage("素材は一つ以上必要です");
            return;
        }

        for (int i = 0; i < 12; i++) {
            int slot = craftRegion.get(i);
            ItemStack item = e.getInventory().getItem(slot);
            if (item == null) continue;

            switch (item.getType()) {
                case WHITE_WOOL -> hides[i] = HideType.White;
                case GRAY_WOOL -> hides[i] = HideType.Gray;
                case BLACK_WOOL -> hides[i] = HideType.Black;
            }
        }
        ItemStack product = inv.getItem(4);
        if (product == null || product.getItemMeta() == null) {
            e.getPlayer().sendMessage("レシピ"+recipeId+"の保存に失敗しました");
            return;
        }

        Recipe recipe = new Recipe(materials,hides,product.getAmount());

        CraftMain.recipeMap.put(recipeId,recipe);
        CraftConfig.save(recipeId);

        e.getPlayer().sendMessage("レシピ"+recipeId+"（"+product.getItemMeta().getDisplayName()+"）を保存しました");
    }
}
