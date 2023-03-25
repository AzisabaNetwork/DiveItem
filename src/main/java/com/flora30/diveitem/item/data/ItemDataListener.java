package com.flora30.diveitem.item.data;

import com.flora30.diveapi.data.ItemData;
import com.flora30.diveapi.data.Story;
import com.flora30.diveapi.data.item.Rarity;
import com.flora30.diveapi.event.GetItemEvent;
import com.flora30.diveapi.event.SaveItemEvent;
import com.flora30.diveapi.plugins.QuestAPI;
import com.flora30.diveapi.tools.ItemType;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.util.PlayerItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ItemDataListener {
    public static final String artifactValueKey = "artifactValue";

    // 固有の値を保存する
    public static void onSaveItem(SaveItemEvent e) {
        ItemData data = ItemDataMain.getItemData(ItemStackMain.getItemID(e.item));
        // 遺物価値
        if (data != null && data.artifactData != null) {
            e.additionalValue = String.valueOf(PlayerItem.getInt(e.item,artifactValueKey));
        }
    }

    // 固有の値がある場合は、e.additionalValueにある
    public static void onGetItem(GetItemEvent e){
        int id = e.getId();
        ItemData itemData = ItemDataMain.getItemData(id);
        if (itemData == null){
            Bukkit.getLogger().info("itemDataがありません - "+e.getId());
            return;
        }

        //データ紐づけ
        e.setString("id",id);
        // 遺物価値
        if (itemData.artifactData != null) {
            // 値がない場合は自動生成：80 ％ ～ 120 ％
            if (e.additionalValue == null) {
                int value = (int) (((double)itemData.artifactData.value) * (0.8 + Math.random() * 0.4));
                e.additionalValue = String.valueOf(value);
            }

            try{
                e.setString(artifactValueKey, Integer.parseInt(e.additionalValue));
            } catch (NumberFormatException error) {
                Bukkit.getLogger().info("遺物"+e.getItemMeta().getDisplayName()+"の価値「"+e.additionalValue+"」は不正です");
                e.setCancelled(true);
                return;
            }
        }
        // 耐久値
        else if(itemData.type == ItemType.Armor || itemData.gatherData != null) {
            Damageable damageable = (Damageable) e.getItemMeta();
            try{
                damageable.setDamage(Integer.parseInt(e.additionalValue));
                e.setItemMeta(damageable);
            } catch (NumberFormatException error) {
                //Bukkit.getLogger().info(e.getItemMeta().getDisplayName()+"に耐久値がありません：変更なし");
            }
        }

        // lore作成
        ItemMeta meta = e.getItemMeta();
        meta.setLore(createLore(itemData, e.additionalValue));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        e.setItemMeta(meta);
    }

    /**
     * Loreを作成する
     */
    private static List<String> createLore(ItemData data, String value1) {
        List<String> lore = new ArrayList<>();
        if (data == null){
            return lore;
        }

        //種類
        if (convertDisplayType(data.type) != null){
            lore.add("");
            lore.add(ChatColor.GOLD +"種類 ‣ " + ChatColor.WHITE + convertDisplayType(data.type));
        }

        // 遺物価値
        if (data.artifactData != null) {
            lore.add("");
            lore.add(ChatColor.GOLD + "遺物価値 ‣ " + ChatColor.WHITE + value1);
        }

        //攻撃力
        if (data.damage > 0) {
            lore.add("");
            lore.add(ChatColor.GOLD+"攻撃力 ‣ "+ChatColor.WHITE + data.damage);
        }

        //レベル
        if (data.level > 1) {
            lore.add("");
            lore.add(ChatColor.GOLD+"必要レベル ‣ "+ChatColor.WHITE + data.level);
        }

        //売却値
        if (data.money > 0) {
            lore.add("");
            lore.add(ChatColor.GOLD +"売却値 ‣ " + ChatColor.WHITE+String.format("%5.1f",data.money));
        }

        ///////////////////
        lore.add("");
        ///////////////////
        //所持数制限
        if (data.maxStack < 64){
            lore.add(ChatColor.GOLD +"最大スタック ‣ " + ChatColor.WHITE + data.maxStack);
        }
        //エリア名
        if (data.area != null){
            Story story = QuestAPI.getStory(data.area);
            if (story != null && story.displayName != null){
                lore.add( ChatColor.GOLD +"入手階層 ‣ " + ChatColor.WHITE + QuestAPI.getStory(data.area).displayName);
            }
        }
        //食事
        checkAdd(lore, ChatColor.GOLD +"満腹度 ‣ " + ChatColor.WHITE + data.food, data.food);

        // 採集
        switch (data.gatherData.toolType) {
            case Mining,Logging, Fishing -> {
                lore.add("");
                lore.add(ChatColor.GOLD + "採集可能な深度 ‣ " + ChatColor.WHITE + data.gatherData.maxDepth);
                lore.add("");
                //checkAdd(lore, ChatColor.GOLD + "破壊可能ブロック ‣ " + ChatColor.WHITE + convertMaterialsToString(data.gatherData.breakAbleMaterialSet), convertMaterialsToString(data.gatherData.breakAbleMaterialSet));
                lore.add(ChatColor.GOLD + "アイテム入手確率 ‣ " + ChatColor.WHITE + (int) data.gatherData.dropRate * 100 + "％");
            }
        }

        // ロープ
        if (data.ropeData != null) {
            lore.add(ChatColor.GOLD + "距離 ‣ " + ChatColor.WHITE + data.ropeData.length);
        }

        ///////////////////
        lore.add("");
        ///////////////////
        List<String> text = data.text;
        if (text != null){
            lore.addAll(text);
        }

        checkAdd(lore,getRarityString(data.rarity),getRarityString(data.rarity));

        return lore;
    }


    private static String convertMaterialsToString(Set<Material> set){
        StringBuilder sb = new StringBuilder();
        for (Material material : set){
            sb.append(material.toString());
        }
        return sb.toString();
    }

    private static String convertDisplayType(ItemType type){
        try{
            switch (type){
                case Food:
                    return "食べ物";
                case Armor:
                    return "防具";
                case Other:
                    return "その他";
                case Supply:
                    return "店売り品";
                case Artifact:
                    return "遺物";
                case Material:
                    return "素材";
            }
        } catch (IllegalArgumentException e){
            Bukkit.getLogger().info("[DiveCore-Item]種類が不正です - "+type);
            return null;
        }
        return  null;
    }

    private static void checkAdd(List<String> list, String str, String check){
        if (Objects.nonNull(check)){
            list.add(str);
        }
    }

    private static void checkAdd(List<String> list, String str, int check){
        if (check > 0){
            list.add(str);
        }
    }

    private static void checkAdd(List<String> list, String str, double check){
        if(check > 0){
            list.add(str);
        }
    }

    private static String getRarityString(Rarity type){
        return switch (type) {
            case Unusual -> ChatColor.DARK_GREEN + "Unusual";
            case Rare -> ChatColor.DARK_BLUE + "Rare";
            case Epic -> ChatColor.DARK_PURPLE + "Epic";
            case Legendary -> ChatColor.GOLD + "Legendary";
            case Blessed -> ChatColor.DARK_RED + "Blessed";
            default -> ChatColor.WHITE + "Normal";
        };
    }
}
