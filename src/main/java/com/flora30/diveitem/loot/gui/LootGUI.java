package com.flora30.diveitem.loot.gui;

import com.flora30.diveapi.data.ItemData;
import com.flora30.diveapi.data.Point;
import com.flora30.diveapi.data.player.LevelData;
import com.flora30.diveapi.event.HelpEvent;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.tools.HelpType;
import com.flora30.diveitem.item.data.ItemDataMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.loot.Loot;
import com.flora30.diveitem.loot.LootGood;
import com.flora30.diveitem.loot.LootGoods;
import com.flora30.diveitem.loot.LootMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootGUI {

    public static void open(Player player, Loot loot, int level) {
        if(CoreAPI.getPlayerData(player.getUniqueId()) == null) return;

        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.LootChestGUI));
        Inventory gui = create(player, loot, level);
        player.openInventory(gui);
    }


    private static Inventory create(Player player, Loot loot, int level) {
        LevelData data = CoreAPI.getPlayerData(player.getUniqueId()).levelData;
        //プレイヤーデータの取得
        double luckyRate = Point.convertLuckyRate(data.pointLuc);
        //Bukkit.getLogger().info("[Loot]LuckyRate = "+luckyRate);

        //ラッキーチェスト判定
        boolean isLucky = Math.random() <= luckyRate;
        //演出
        if (isLucky) {
            player.sendMessage("豪華なチェストを見つけた！");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1, 1);
            player.spawnParticle(Particle.FLAME, player.getLocation(), 20, 0.1, 0.1, 0.1, 0.1);
        }

        //名前の＋を取り出してGUIを新規作成
        int maxSlot = LootMain.getLootLevel(level).getChestSlot();
        String plus = LootMain.getLootLevel(level).getTitlePlus();
        Inventory gui = Bukkit.createInventory(null, maxSlot,   "宝箱 " + plus);
        int slotPlaced = 0;

        //報酬の配置
        LootGoods lootGoods = loot.getLootGood(level);
        List<LootGood> lootGoodList = new ArrayList<>(lootGoods.getItemList());
        Collections.shuffle(lootGoodList);
        //報酬リストを回す
        for (LootGood good : lootGoodList) {
            ItemData iData = ItemDataMain.getItemData(good.getItemID());
            if (iData == null) {
                continue;
            }
            double rate = iData.rarity.rate;
            //入手するかの判定
            if (Math.random() > rate) {
                //レアドロップ失敗時：アイテムを失敗時のものに変更
                good = LootMain.failedLoot;
            }

            //アイテムの取得
            ItemStack item = ItemStackMain.getItem(good.getItemID());
            item.setAmount(good.getAmount());

            //gui上にランダム配置
            int slot = (int) Math.round(Math.random() * (gui.getSize() - 1));
            while (gui.getItem(slot) != null) {
                slot++;
                if (slot >= maxSlot) {
                    slot = 0;
                }
            }
            gui.setItem(slot, item);
            slotPlaced++;

            //インベントリが埋まったら抜ける
            if (isLucky) {
                if (slotPlaced >= gui.getSize()) {
                    break;
                }
            } else if (slotPlaced >= gui.getSize() / 3) {
                break;
            }
        }

        return gui;
    }
}
