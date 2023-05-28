package com.flora30.diveitem.trade;

import com.flora30.divelib.data.TradeData;
import com.flora30.divelib.data.TradeObject;
import com.flora30.divelib.data.TradePhase;
import com.flora30.divelib.event.HelpEvent;
import com.flora30.divelib.event.HelpType;
import com.flora30.divelib.util.PlayerItem;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeMain {
    private static final Map<UUID, TradeData> tradeMap = TradeObject.INSTANCE.getTradeMap();

    public static boolean trade(Player p1, Player p2) {
        TradeData data1 = tradeMap.get(p1.getUniqueId());
        TradeData data2 = tradeMap.get(p2.getUniqueId());
        if (data1 == null || data2 == null) return false;
        if (data1.getPhase() != TradePhase.Complete || data2.getPhase() != TradePhase.Complete) return false;

        // アイテムをあげる
        for (ItemStack item : data1.getItems()) {
            if (item != null) PlayerItem.INSTANCE.giveItem(p2,item);
        }
        for (ItemStack item : data2.getItems()) {
            if (item != null) PlayerItem.INSTANCE.giveItem(p1,item);
        }

        tradeMap.remove(p1.getUniqueId());
        tradeMap.remove(p2.getUniqueId());

        p1.closeInventory();
        p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
        p1.sendMessage(ChatColor.GREEN + "トレードが完了しました！");
        p2.closeInventory();
        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
        p2.sendMessage(ChatColor.GREEN + "トレードが完了しました！");

        return true;
    }

    public static void onInteract(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().isSneaking()) return;
        if (event.getRightClicked().getType() != EntityType.PLAYER) return;
        if (event.getRightClicked().hasMetadata("NPC")) return;

        if (event.getRightClicked() instanceof Player to) {
            invite(event.getPlayer(),to);
        }
    }

    public static void invite(Player from, Player to) {
        Bukkit.getPluginManager().callEvent(new HelpEvent(from, HelpType.Trade));

        TradeData fromData = tradeMap.get(from.getUniqueId());
        TradeData toData = tradeMap.get(to.getUniqueId());

        // 相手が他の人とトレード中の場合
        if (toData != null && toData.getPhase() != TradePhase.Invite) {
            from.sendMessage(ChatColor.YELLOW+to.getDisplayName()+"は他の人とトレード中のようです・・・");
            return;
        }

        // 自分のデータを新規作成（新しく開始した場合、トレード申請を切り替えた場合）
        // 相手がトレード中だった場合は新規作成せずに戻るので、その後に行う
        if (fromData == null || fromData.getTo() != to.getUniqueId()) {
            fromData = new TradeData(to.getUniqueId());
            tradeMap.put(from.getUniqueId(),fromData);
        }

        // トレード開始（承認した場合）
        if (toData != null && toData.getPhase() == TradePhase.Invite && toData.getTo() == from.getUniqueId()) {
            fromData.setPhase(TradePhase.Prepare);
            toData.setPhase(TradePhase.Prepare);

            TradeGUI.openGUI(from,to);
            TradeGUI.openGUI(to,from);
        }
        else {
            // 誘った場合（初回）
            if (fromData.getInviteTime() == 0) {
                to.sendMessage(ChatColor.GREEN+from.getDisplayName()+" からトレードの申請が来ています。相手に Shift + 右クリックでトレードを開始できます");
                from.sendMessage(ChatColor.GREEN+to.getDisplayName()+" にトレードの申請を行いました");
            }

            fromData.setInviteTime(1);
        }
    }

    public static void onTickLog() {
        Bukkit.getLogger().info("----trade log----");
        for (UUID key : tradeMap.keySet()) {
            Player player = Bukkit.getPlayer(key);
            if (player != null) {
                Bukkit.getLogger().info(player.getDisplayName() +" have ↓");
                TradeData data = tradeMap.get(key);
                for (ItemStack item : data.getItems()) {
                    if (item != null && item.getItemMeta() != null) {
                        Bukkit.getLogger().info(item.getItemMeta().getDisplayName() + " - "+item.getAmount());
                    }
                }
            }
        }
    }

    // 招待の時間経過 → キャンセル
    public static void onTick() {
        for (UUID playerId : tradeMap.keySet()) {
            TradeData data = tradeMap.get(playerId);
            if (data.getPhase() == TradePhase.Invite) {
                data.setInviteTime(data.getInviteTime()+1);

                // キャンセル時の言葉
                if (data.getInviteTime() >= 200) {
                    Player player = Bukkit.getPlayer(playerId);
                    Player to = Bukkit.getPlayer(data.getTo());
                    if (player == null) continue;
                    if (to != null) {
                        player.sendMessage(ChatColor.YELLOW+to.getDisplayName()+"へのトレード申請は、時間経過でキャンセルされました");
                    }
                    else {
                        OfflinePlayer toOff = Bukkit.getOfflinePlayer(data.getTo());
                        player.sendMessage(ChatColor.YELLOW+toOff.getName()+"へのトレード申請は、時間経過でキャンセルされました");
                    }
                }
            }
        }

        tradeMap.entrySet().removeIf(d -> d.getValue().getInviteTime() >= 200);
    }
}
