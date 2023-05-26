package com.flora30.diveitem.craft;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divenew.data.LevelObject;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CraftMain {

    // 本でレシピ習得
    // できなければ経験値を追加
    public static void onUseRecipeBook(Player player, int recipeId, int anotherExp) {
        if (!learnRecipe(player,recipeId)) {
            CoreAPI.addExp(player,anotherExp);
        }
    }
    public static boolean learnRecipe(Player player, int recipeId) {
        ItemStack item = ItemMain.INSTANCE.getItem(recipeId);
        if (item == null || item.getItemMeta() == null) return false;
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if (data.getCompletedRecipeSet().contains(recipeId)) return false;
        if (data.getFoundRecipeSet().contains(recipeId)) return false;

        data.getFoundRecipeSet().add(recipeId);
        player.sendMessage("レシピ「 "+ item.getItemMeta().getDisplayName() + ChatColor.WHITE+" 」を習得しました！");
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN,1,1);
        return true;
    }
}
