package com.flora30.diveitem.craft.gui;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.event.HelpEvent;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.tools.GuiItem;
import com.flora30.diveapi.tools.HelpType;
import com.flora30.diveitem.craft.CraftMain;
import com.flora30.diveitem.craft.Recipe;
import com.flora30.diveitem.item.ItemStackMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftListGUI {
    public static final List<Integer> listRegion = List.of(10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43);

    public static Inventory getGui(Player player) {
        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.CraftListGUI));
        List<Integer> recipeList = new ArrayList<>();
        // 作れるレシピを取得
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        recipeList.addAll(data.foundRecipeSet);
        recipeList.addAll(data.completedRecipeSet);
        // IDの昇順ソート
        Collections.sort(recipeList);

        Inventory gui = Bukkit.createInventory(null, 54, "クラフト一覧");
        GuiItem.grayBack(gui);

        for (int i = 0; i < 28; i++) {
            // 最初の2つは0なので省く→レシピの表示が2つ消えたので省かない
            if (i >= recipeList.size()) break;
            gui.setItem(listRegion.get(i), getIcon(recipeList.get(i)));
        }
        gui.setItem(4,GuiItem.getItem(Material.CRAFTING_TABLE));
        gui.setItem(53, GuiItem.getReturn());

        return gui;
    }

    private static ItemStack getIcon(int recipeId) {
        //Bukkit.getLogger().info("表示RecipeID : "+recipeId);
        if (!CraftMain.recipeMap.containsKey(recipeId)) return null;
        Recipe recipe = CraftMain.recipeMap.get(recipeId);

        ItemStack icon = ItemStackMain.getItem(recipeId);
        if (icon == null || icon.getItemMeta() == null) return null;
        ItemMeta meta = icon.getItemMeta();

        // loreを設定
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "＜ 必要な素材 ＞");
        for (Recipe.ItemAmount ia : recipe.itemAmounts) {
            ItemStack item = ItemStackMain.getItem(ia.itemId());
            if (item == null || item.getItemMeta() == null) continue;
            lore.add(item.getItemMeta().getDisplayName() + ChatColor.GOLD + " ‣ "+ ChatColor.WHITE + ia.amount() + "個");
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        icon.setAmount(recipe.amount);

        return icon;
    }

    public static void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        e.setCancelled(true);
        if (e.getClickedInventory() == e.getView().getBottomInventory()) return;
        Player player = (Player) e.getWhoClicked();
        if (!listRegion.contains(e.getSlot())) {
            if (e.getSlot() == 53) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK,1, 1);
                CoreAPI.openMenu((Player) e.getWhoClicked());
            }
            return;
        }

        // クラフトGUIを開く
        ItemStack item = e.getClickedInventory().getItem(e.getSlot());
        if (item == null || item.getItemMeta() == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        int recipeId = ItemStackMain.getItemID(item);
        Inventory craftGui = CraftGUI.getGui(player, recipeId);
        assert craftGui != null;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK,1, 1);
        player.openInventory(craftGui);
    }
}
