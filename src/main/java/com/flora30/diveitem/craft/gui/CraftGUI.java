package com.flora30.diveitem.craft.gui;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.util.GuiItem;
import com.flora30.divelib.util.PlayerItem;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.craft.CraftMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divelib.data.item.ItemDataObject;
import com.flora30.divelib.data.recipe.Recipe;
import com.flora30.divelib.data.recipe.RecipeObject;
import com.flora30.divelib.data.recipe.RecipePhase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CraftGUI {
    static final List<Integer> recipeRegion = List.of(10,11,12, 19,20,21, 28,29,30, 37,38,39);
    static final List<Integer> craftRegion = List.of(14,15,16, 23,24,25, 32,33,34, 41,42,43);
    static final int[] backRegion = {0,1,2,3, 5,6,7,8,9, 13, 17,18, 22, 26,27, 31, 35,36, 40, 44,45,46,47,48,49,50,51,52};
    static final int returnPoint = 53;

    public static Inventory getGui(Player player, int recipeId) {
        // レシピの段階を取得
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        RecipePhase phase = null;
        if (data.getFoundRecipeSet().contains(recipeId)) phase = RecipePhase.Found;
        if (data.getCompletedRecipeSet().contains(recipeId)) phase = RecipePhase.Completed;
        // レシピを習得していない場合（想定外）
        if (phase == null) return null;

        // 準備
        Recipe recipe = RecipeObject.INSTANCE.getRecipeMap().get(recipeId);
        Inventory gui = Bukkit.createInventory(null, 54, "クラフト");
        GuiItem.INSTANCE.grayBack(gui);

        // 表示アイテムを取得してguiの左側に置く
        ItemStack[] materials = null;
        switch (phase) {
            case Found -> materials = recipe.getFoundMaterials();
            case Completed -> materials = recipe.getCompletedMaterials();
        }
        for (int i = 0; i < 12; i++) {

            if (materials[i] == null) {
                gui.setItem(recipeRegion.get(i), null);
            }
            else {
                gui.setItem(recipeRegion.get(i), materials[i].clone());
            }

            gui.setItem(craftRegion.get(i),null);
        }

        // その他を置く
        ItemStack product = ItemMain.INSTANCE.getItem(recipeId);
        assert product != null;
        product.setAmount(recipe.getAmount());
        gui.setItem(4, product);
        gui.setItem(returnPoint,GuiItem.INSTANCE.getReturn());

        return gui;
    }

    public static void craftCheck(Player player) {
        if (!player.getOpenInventory().getTitle().equals("クラフト")) return;

        // レシピ・完成品を取得
        Inventory inv = player.getOpenInventory().getTopInventory();
        ItemStack product = inv.getItem(4);
        if (product == null) return;
        int recipeId = ItemMain.INSTANCE.getItemId(product);
        Recipe recipe = RecipeObject.INSTANCE.getRecipeMap().get(recipeId);
        // 合っている場合は continue
        int minAmount = 64;
        for (int i = 0; i < 12; i++) {
            ItemStack item = inv.getItem(craftRegion.get(i));
            if (item == null) {
                if (recipe.getMaterials()[i] == 0) continue;
                return;
            }
            if (ItemMain.INSTANCE.getItemId(item) == recipe.getMaterials()[i]) {
                minAmount = Math.min(item.getAmount(), minAmount);
                continue;
            }
            return;
        }

        // クラフトの場所から素材アイテムを一つずつ減らす
        for (int i = 0; i < 12; i++) {
            ItemStack item = inv.getItem(craftRegion.get(i));
            if (item == null) continue;
            item.setAmount(item.getAmount() - minAmount);
        }

        // 完成品の数 = 元々の数 * クラフト回数
        int amount = product.getAmount() * minAmount;

        // 完成品を渡す
        int giveTime = 0;
        while(amount > 0) {
            ItemStack give = product.clone();
            give.setAmount(Math.min(amount, 64));

            // 時間をずらすことで、いっぱいになった時の対策
            DiveItem.plugin.delayedTask(giveTime, () -> {
                PlayerItem.INSTANCE.giveItem(player,give);
            });
            giveTime++;

            amount -= 64;
        }

        // 演出
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1.5F);
        setBack(inv, Material.YELLOW_STAINED_GLASS_PANE);
        DiveItem.plugin.delayedTask(10,() -> setBack(inv, Material.GRAY_STAINED_GLASS_PANE));

        // Foundだった場合、Completedにセットしなおす
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if (data.getFoundRecipeSet().contains(recipeId)){
            data.getCompletedRecipeSet().add(recipeId);
            ItemStack[] materials = recipe.getCompletedMaterials();
            for (int i = 0; i < 12; i++) {
                inv.setItem(recipeRegion.get(i), materials[i]);
            }
            data.getFoundRecipeSet().remove(recipeId);
        }
    }

    public static void onDrag(InventoryDragEvent e) {
        // クラフトの場所以外に置かれたものがある場合、キャンセル
        for (int slot : e.getRawSlots()) {
            // プレイヤーの場所ではない（プレイヤーの場所の場合54以上） || クラフトの場所ではない
            if (slot < 54) {
                if (!craftRegion.contains(slot)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        DiveItem.plugin.delayedTask(1,() -> craftCheck((Player) e.getWhoClicked()));
    }


    public static void onClick(InventoryClickEvent e) {
        // クラフトの場所以外をクリックした場合は何もしない
        if (e.getClickedInventory() == null)  return;
        switch (e.getClick()) {
            // ダブルクリック無効化
            case DOUBLE_CLICK -> {
                e.setCancelled(true);
                return;
            }
            // シフトはプレイヤー側からのみ無効化
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        // プレイヤー側インベントリの場合は何もしない
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) return;
        // クラフトの場所以外をクリックした場合は無効化
        if (!craftRegion.contains(e.getSlot())) {
            //Bukkit.getLogger().info("クラフト場所以外をクリック - "+e.getSlot());
            e.setCancelled(true);
            if (e.getSlot() == returnPoint) {
                Player player = (Player) e.getWhoClicked();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK,1, 1);
                player.openInventory(CraftListGUI.getGui(player));
            }
            return;
        }

        DiveItem.plugin.delayedTask(1,() -> craftCheck((Player) e.getWhoClicked()));
    }

    private static void setBack(Inventory inv, Material material) {
        for (int i : backRegion) {
            ItemStack backItem = inv.getItem(i);
            assert backItem != null;
            if (ItemMain.INSTANCE.getItemId(backItem) != -1) continue;
            if (backItem.getType() != material) {
                backItem.setType(material);
            }
        }
    }

    /**
     * クラフト場所に残っていたものを返却
     */
    public static void onClose(InventoryCloseEvent e) {
        for (int i : craftRegion) {
            if (e.getInventory().getItem(i) != null) {
                PlayerItem.INSTANCE.giveItem((Player) e.getPlayer(), e.getInventory().getItem(i));
            }
        }
    }
}
