package com.flora30.diveitem.item;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.event.FirstJoinEvent;
import com.flora30.divelib.util.PlayerItem;
import com.flora30.diveitem.craft.gui.RecipeEditorGUI;
import com.flora30.diveitem.util.NumberUtil;
import com.flora30.divelib.data.item.ItemData;
import com.flora30.divelib.data.item.ItemDataObject;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class ItemTrigger {
    public static void onCommand(List<String> commands) {

        switch (commands.get(1)) {
            case "log" -> {
                if (commands.size() < 3) {
                    Bukkit.getLogger().info("/item log [ID]");
                    return;
                }
                int id;
                try {
                    id = Integer.parseInt(commands.get(2));
                    LogItemData(id);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().info("/item log [ID]");
                }
            }
            default -> Bukkit.getLogger().info("/item log|recipe [ID]");
        }
    }
    private static void LogItemData(int id) {
        ItemData data = ItemDataObject.INSTANCE.getItemDataMap().get(id);
        if(data == null){
            Bukkit.getLogger().info("[ID-" + id + "]は登録されていません");
            return;
        }
        Bukkit.getLogger().info("[ID-" + id + "]の情報を表示します");
        Bukkit.getLogger().info("* area = " + data.getArea());
        Bukkit.getLogger().info("* type = " + data.getType());
        Bukkit.getLogger().info("* level = " + data.getLevel());
        Bukkit.getLogger().info("* rarity = " + data.getRarity());
    }

    // 初期アイテム
    public static void onFirstJoin(FirstJoinEvent e) {
        Bukkit.getLogger().info("FirstJoinEvent listened");
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(1));

        ItemStack rope = ItemMain.INSTANCE.getItem(3005);
        rope.setAmount(16);
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), rope);


        ItemStack potion = ItemMain.INSTANCE.getItem(3001);
        potion.setAmount(4);
        PlayerItem.INSTANCE.giveItem(e.getPlayer(),potion);

        ItemStack food = ItemMain.INSTANCE.getItem(3008);
        food.setAmount(20);
        PlayerItem.INSTANCE.giveItem(e.getPlayer(),food);

        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(3012));
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(3013));
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(3014));
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(1001));

        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(1002));
        Bukkit.getLogger().info("FirstJoin - 1002 item give");

        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(1003));
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(1004));
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(3018));
        PlayerItem.INSTANCE.giveItem(e.getPlayer(), ItemMain.INSTANCE.getItem(3019));

        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(e.getPlayer().getUniqueId());
        data.setMoney(1000);

        e.getPlayer().sendMessage("ギルドからの支給品を受け取りました！奈落の旅へようこそ！");
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,1,1);
    }

    public static void onCommand(Player player, String args1, String args2, String args3){
        //""=null
        switch (args1) {
            case "set" -> {
                int id;
                try {
                    id = Integer.parseInt(args2);
                    ItemStack item = player.getInventory().getItemInMainHand().clone();
                    item.setAmount(1);
                    ItemStackMain.setItem(id, item);
                    player.sendMessage("[ID-" + id + "]にアイテムを登録しました");
                } catch (NumberFormatException e) {
                    player.sendMessage("/item set [ID]");
                }
            }
            case "setMM" -> {
                int id;
                try {
                    id = Integer.parseInt(args2);
                    if (ItemStackMain.setMMItem(id, args3)) {
                        player.sendMessage("[ID-" + id + "]にMMアイテム「" + args3 + "」を登録しました");
                    } else {
                        player.sendMessage("MMアイテム「" + args3 + "」は存在しません");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("/item setMM [ID] [MMname]");
                }
            }
            case "get" -> {
                int id, amount;
                try {
                    id = Integer.parseInt(args2);
                    amount = NumberUtil.parseInt(args3, 1);

                    ItemStack item = ItemMain.INSTANCE.getItem(id);
                    if (item == null) {
                        player.sendMessage("[ID-" + args2 + "]のアイテムは存在しません");
                        return;
                    }

                    item.setAmount(amount);
                    player.getInventory().addItem(item);
                    player.sendMessage("[ID-" + id + "]のアイテムを"+amount+"個取得しました");
                } catch (NumberFormatException e) {
                    player.sendMessage("/item get [ID] [amount]");
                }
            }
            case "recipe" -> {
                int id;
                try {
                    id = Integer.parseInt(args2);
                    if (ItemMain.INSTANCE.getNeutralItem(id) == null) {
                        player.sendMessage("[ID-" + args2 + "]のアイテムは存在しません");
                        return;
                    }
                    player.openInventory(RecipeEditorGUI.getGui(id));
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().info("/item recipe [ID]");
                }
            }
            default -> player.sendMessage("/item [get | set | recipe | setMM] [id]");
        }
    }
}
