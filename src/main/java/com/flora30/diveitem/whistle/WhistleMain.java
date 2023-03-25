package com.flora30.diveitem.whistle;

import com.flora30.diveapi.data.Whistle;
import com.flora30.diveapi.data.player.LevelData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveitem.util.PlayerItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WhistleMain {
    public static Map<Integer, Whistle> whistleMap = new HashMap<>();
    public static Map<Integer, Integer> whistleExpMap = new HashMap<>();

    public static void addWhistleExp(Player player, int exp) {
        // 現在のランク・経験値
        LevelData data = CoreAPI.getPlayerData(player.getUniqueId()).levelData;

        // まず：現在の経験値を足す
        data.whistleExp += exp;

        // 次：ランクアップチェック
        // 次のランク・経験値
        int nextRank = data.whistleRank + 1;
        int nextExp = whistleExpMap.get(nextRank);
        // ランクアップ条件を満たしていれば下へ
        if (data.whistleExp < nextExp) return;

        // ランクアップ処理
        data.whistleExp -= nextExp;
        data.whistleRank += 1;
        player.sendMessage(ChatColor.GOLD+ "笛ランクアップ！ " + getWhistleRankDisplay(whistleMap.get(nextRank - 1)) +ChatColor.GOLD+ " ‣ " + getWhistleRankDisplay(whistleMap.get(nextRank)));

        // 次のランクアップを確認
        addWhistleExp(player, 0);
    }

    public static String getWhistleRankDisplay(Whistle whistle) {
        String type;
        switch (whistle.type) {
            case Red -> type = "赤笛";
            case Blue -> type = "青笛";
            case Moon -> type = "月笛";
            case Black -> type = "黒笛";
            case White -> type = "白笛";
            default -> throw new IllegalStateException("Unexpected value: " + whistle.type);
        }

        return ChatColor.WHITE + type + " ランク" + whistle.rank;
    }

    public static int getArtifactValue(ItemStack item) {
        return PlayerItem.getInt(item, "artifactValue");
    }
}
