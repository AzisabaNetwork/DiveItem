package com.flora30.diveitem.craft;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveitem.item.ItemStackMain;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CraftMain {
    //完成品ID | レシピ
    public static Map<Integer,Recipe> recipeMap = new HashMap<>();

    // 本でレシピ習得
    // できなければ経験値を追加
    public static void onUseRecipeBook(Player player, int recipeId, int anotherExp) {
        if (!learnRecipe(player,recipeId)) {
            CoreAPI.addExp(player,anotherExp);
        }
    }
    public static boolean learnRecipe(Player player, int recipeId) {
        ItemStack item = ItemStackMain.getItem(recipeId);
        if (item == null || item.getItemMeta() == null) return false;
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if (data.completedRecipeSet.contains(recipeId)) return false;
        if (data.foundRecipeSet.contains(recipeId)) return false;

        data.foundRecipeSet.add(recipeId);
        player.sendMessage("レシピ「 "+ item.getItemMeta().getDisplayName() + ChatColor.WHITE+" 」を習得しました！");
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN,1,1);
        return true;
    }
}
