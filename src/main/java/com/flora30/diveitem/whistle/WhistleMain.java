package com.flora30.diveitem.whistle;

import com.flora30.divelib.data.player.LevelData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.util.PlayerItem;
import com.flora30.divelib.data.Whistle;
import com.flora30.divelib.data.WhistleObject;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WhistleMain {
    private final static Map<Integer, Whistle> whistleMap = WhistleObject.INSTANCE.getWhistleMap();
    private final static Map<Integer, Integer> whistleExpMap = WhistleObject.INSTANCE.getWhistleExpMap();

    public static void addWhistleExp(Player player, int exp) {
        // 現在のランク・経験値
        LevelData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLevelData();

        // まず：現在の経験値を足す
        data.setWhistleExp(data.getWhistleExp()+exp);

        // 次：ランクアップチェック
        // 次のランク・経験値
        int nextRank = data.getWhistleRank() + 1;
        int nextExp = whistleExpMap.get(nextRank);
        // ランクアップ条件を満たしていれば下へ
        if (data.getWhistleExp() < nextExp) return;

        // ランクアップ処理
        data.setWhistleExp(data.getWhistleExp() - nextExp);
        data.setWhistleRank(data.getWhistleRank() + 1);
        player.sendMessage(ChatColor.GOLD+ "笛ランクアップ！ " + getWhistleRankDisplay(whistleMap.get(nextRank - 1)) +ChatColor.GOLD+ " ‣ " + getWhistleRankDisplay(whistleMap.get(nextRank)));

        // 次のランクアップを確認
        addWhistleExp(player, 0);
    }

    public static String getWhistleRankDisplay(Whistle whistle) {
        String type;
        switch (whistle.getType()) {
            case Red -> type = "赤笛";
            case Blue -> type = "青笛";
            case Moon -> type = "月笛";
            case Black -> type = "黒笛";
            case White -> type = "白笛";
            default -> throw new IllegalStateException("Unexpected value: " + whistle.getType());
        }

        return ChatColor.WHITE + type + " ランク" + whistle.getRank();
    }

    public static int getArtifactValue(ItemStack item) {
        return PlayerItem.INSTANCE.getInt(item, "artifactValue");
    }
}
