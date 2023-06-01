package com.flora30.diveitem.craft.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.util.GuiItem;
import com.flora30.diveitem.craft.CraftConfig;
import com.flora30.diveitem.craft.CraftMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divelib.data.recipe.HideType;
import com.flora30.divelib.data.recipe.Recipe;
import com.flora30.divelib.data.recipe.RecipeObject;
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
        Recipe recipe = RecipeObject.INSTANCE.getRecipeMap().get(recipeId);
        if (recipe == null) recipe = new Recipe(new int[12],new HideType[12],1);
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "クラフト（編集："+recipeId+"）");
        GuiItem.INSTANCE.grayBack(gui);

        // 表示アイテムを取得してguiの左側に置く
        for (int i = 0; i < 12; i++) {
            gui.setItem(recipeRegion.get(i), ItemMain.INSTANCE.getItem(recipe.getMaterials()[i]));

            if (recipe.getHides()[i] == null) {
                gui.setItem(craftRegion.get(i),null);
                continue;
            }

            switch (recipe.getHides()[i]) {
                case White -> gui.setItem(craftRegion.get(i), new ItemStack(Material.WHITE_WOOL));
                case Gray -> gui.setItem(craftRegion.get(i), new ItemStack(Material.GRAY_WOOL));
                case Black -> gui.setItem(craftRegion.get(i), new ItemStack(Material.BLACK_WOOL));
                default -> gui.setItem(craftRegion.get(i), null);
            }
        }

        // その他を置く
        ItemStack product = ItemMain.INSTANCE.getItem(recipeId);
        assert product != null;
        product.setAmount(recipe.getAmount());
        gui.setItem(4, product);
        gui.setItem(0,GuiItem.INSTANCE.getItem(Material.WOODEN_AXE));
        gui.setItem(2,GuiItem.INSTANCE.getItem(Material.WOODEN_AXE));
        gui.setItem(6,GuiItem.INSTANCE.getItem(Material.WOODEN_AXE));
        gui.setItem(8,GuiItem.INSTANCE.getItem(Material.WOODEN_AXE));

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
        int recipeId = ItemMain.INSTANCE.getItemId(inv.getItem(4));

        int[] materials = new int[12];
        HideType[] hides = new HideType[12];

        for (int i = 0; i < 12; i++) {
            int slot = recipeRegion.get(i);
            ItemStack item = e.getInventory().getItem(slot);
            if (item == null) continue;

            int id = ItemMain.INSTANCE.getItemId(item);
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

        RecipeObject.INSTANCE.getRecipeMap().put(recipeId,recipe);
        CraftConfig.save(recipeId);

        e.getPlayer().sendMessage("レシピ"+recipeId+"（"+product.getItemMeta().getDisplayName()+"）を保存しました");
    }
}
