package com.flora30.diveitem.item.data;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.data.Rarity;
import com.flora30.diveapin.event.GetItemEvent;
import com.flora30.diveapin.event.SaveItemEvent;
import com.flora30.diveapin.util.PlayerItem;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divenew.data.item.ItemData;
import com.flora30.divenew.data.item.ItemDataObject;
import com.flora30.divenew.data.item.ItemType;
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
        ItemData data = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(e.getItem()));
        // 遺物価値
        if (data != null && data.getArtifactData() != null) {
            e.setAdditionalValue(String.valueOf(PlayerItem.INSTANCE.getInt(e.getItem(),artifactValueKey)));
        }
    }

    // 固有の値がある場合は、e.additionalValueにある
    public static void onGetItem(GetItemEvent e){
        int id = e.getId();
        ItemData itemData = ItemDataObject.INSTANCE.getItemDataMap().get(id);
        if (itemData == null){
            Bukkit.getLogger().info("itemDataがありません - "+e.getId());
            return;
        }

        //データ紐づけ
        e.setString("id",id);
        // 遺物価値
        if (itemData.getArtifactData() != null) {
            // 値がない場合は自動生成：80 ％ ～ 120 ％
            if (e.getAdditionalValue() == null) {
                int value = (int) (((double)itemData.getArtifactData().getValue()) * (0.8 + Math.random() * 0.4));
                e.setAdditionalValue(String.valueOf(value));
            }

            try{
                e.setString(artifactValueKey, Integer.parseInt(e.getAdditionalValue()));
            } catch (NumberFormatException error) {
                Bukkit.getLogger().info("遺物"+e.getItem().getItemMeta().getDisplayName()+"の価値「"+e.getAdditionalValue()+"」は不正です");
                e.setCancelled(true);
                return;
            }
        }
        // 耐久値
        else if(itemData.getType() == ItemType.Armor || itemData.getToolData() != null) {
            Damageable damageable = (Damageable) e.getItem().getItemMeta();
            try{
                damageable.setDamage(Integer.parseInt(e.getAdditionalValue()));
                e.getItem().setItemMeta(damageable);
            } catch (NumberFormatException error) {
                //Bukkit.getLogger().info(e.getItemMeta().getDisplayName()+"に耐久値がありません：変更なし");
            }
        }

        // lore作成
        ItemMeta meta = e.getItem().getItemMeta();
        meta.setLore(createLore(itemData, e.getAdditionalValue()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        e.getItem().setItemMeta(meta);
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
        if (convertDisplayType(data.getType()) != null){
            lore.add("");
            lore.add(ChatColor.GOLD +"種類 ‣ " + ChatColor.WHITE + convertDisplayType(data.getType()));
        }

        // 遺物価値
        if (data.getArtifactData() != null) {
            lore.add("");
            lore.add(ChatColor.GOLD + "遺物価値 ‣ " + ChatColor.WHITE + value1);
        }

        //攻撃力
        if (data.getDamage() > 0) {
            lore.add("");
            lore.add(ChatColor.GOLD+"攻撃力 ‣ "+ChatColor.WHITE + data.getDamage());
        }

        //レベル
        if (data.getLevel() > 1) {
            lore.add("");
            lore.add(ChatColor.GOLD+"必要レベル ‣ "+ChatColor.WHITE + data.getLevel());
        }

        //売却値
        if (data.getMoney() > 0) {
            lore.add("");
            lore.add(ChatColor.GOLD +"売却値 ‣ " + ChatColor.WHITE+String.format("%5.1f",data.getMoney()));
        }

        ///////////////////
        lore.add("");
        ///////////////////
        //エリア名
        Story story = QuestAPI.getStory(data.getArea());
        if (story != null && story.displayName != null) {
            lore.add(ChatColor.GOLD + "入手階層 ‣ " + ChatColor.WHITE + QuestAPI.getStory(data.getArea()).displayName);
        }

        //食事
        checkAdd(lore, ChatColor.GOLD +"満腹度 ‣ " + ChatColor.WHITE + data.getFood(), data.getFood());

        // 採集
        if (data.getToolData() != null) {
            switch (data.getToolData().getToolType()) {
                case Mining,Logging, Fishing -> {
                    lore.add("");
                    lore.add(ChatColor.GOLD + "採集可能な深度 ‣ " + ChatColor.WHITE + data.getToolData().getMaxDepth());
                    lore.add("");
                    //checkAdd(lore, ChatColor.GOLD + "破壊可能ブロック ‣ " + ChatColor.WHITE + convertMaterialsToString(data.gatherData.breakAbleMaterialSet), convertMaterialsToString(data.gatherData.breakAbleMaterialSet));
                    lore.add(ChatColor.GOLD + "アイテム入手確率 ‣ " + ChatColor.WHITE + (int) data.getToolData().getDropRate() * 100 + "％");
                }
            }
        }

        // ロープ
        if (data.getRopeData() != null) {
            lore.add(ChatColor.GOLD + "距離 ‣ " + ChatColor.WHITE + data.getRopeData().getLength());
        }

        ///////////////////
        lore.add("");
        ///////////////////
        List<String> text = data.getText();
        lore.addAll(text);

        checkAdd(lore,getRarityString(data.getRarity()),getRarityString(data.getRarity()));

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
