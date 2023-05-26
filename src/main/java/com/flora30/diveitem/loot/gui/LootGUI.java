package com.flora30.diveitem.loot.gui;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.data.player.LevelData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveapin.event.HelpEvent;
import com.flora30.diveapin.event.HelpType;
import com.flora30.divenew.data.PointObject;
import com.flora30.divenew.data.item.ItemData;
import com.flora30.divenew.data.item.ItemDataObject;
import com.flora30.divenew.data.loot.Loot;
import com.flora30.divenew.data.loot.LootObject;
import org.bukkit.Bukkit;
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
        if(PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()) == null) return;

        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.LootChestGUI));
        Inventory gui = create(player, loot, level);
        player.openInventory(gui);
    }


    private static Inventory create(Player player, Loot loot, int level) {
        LevelData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLevelData();
        //プレイヤーデータの取得
        double luckyRate = PointObject.INSTANCE.getLuckyRate(data.getPointLuc());
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
        int maxSlot = LootObject.INSTANCE.getLootLevelList().get(level).getChestSlot();
        String plus = LootObject.INSTANCE.getLootLevelList().get(level).getTitlePlus();
        Inventory gui = Bukkit.createInventory(null, maxSlot,   "宝箱 " + plus);
        int slotPlaced = 0;

        //報酬の配置
        List<Loot.ItemAmount> itemList = loot.getItemList().get(level);
        Collections.shuffle(itemList);
        //報酬リストを回す
        for (Loot.ItemAmount ia : itemList) {
            ItemData iData = ItemDataObject.INSTANCE.getItemDataMap().get(ia.getItemId());
            if (iData == null) {
                continue;
            }
            double rate = ItemDataObject.INSTANCE.getDropRateMap().get(iData.getRarity());

            //入手するかの判定
            ItemStack item;
            if (Math.random() <= rate) {
                //アイテムの取得
                item = ItemMain.INSTANCE.getItem(ia.getItemId());
                item.setAmount(ia.getAmount());
            }
            else {
                //レアドロップ失敗時：アイテムを失敗時のものに変更
                item = ItemMain.INSTANCE.getItem(LootObject.INSTANCE.getFailedLoot().getItemId());
                item.setAmount(LootObject.INSTANCE.getFailedLoot().getAmount());
            }

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
