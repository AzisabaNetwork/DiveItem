package com.flora30.diveitem.craft;

import com.flora30.diveapi.tools.GuiItem;
import com.flora30.diveapi.tools.GuiItemType;
import com.flora30.diveitem.item.ItemStackMain;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Recipe {
    // 完成品の数
    public final int amount;
    // 素材アイテム（位置、itemId）
    public final int[] materials;
    // ぼやけた地点（位置、隠すブロック3種）
    public final HideType[] hides;
    // 隠すアイテムのItemStack（先に作成しておく）
    public final Map<HideType, ItemStack> hideItemMap = new HashMap<>();
    // ぼやけた地点が無い場合true
    public final boolean isAutoComplete;

    // 必要な素材数まとめ
    public final List<ItemAmount> itemAmounts = new ArrayList<>();
    public record ItemAmount(int itemId, int amount){}

    // ぼやけた地点で、素材の前につく言葉
    String[] prefixes = {"たぶん","きっと","おそらく","もしかして"};

    /**
     * Materialに応じたItemStackを作成するので、ItemStackMainが動くことが必須
     */
    public Recipe(int[] materials, HideType[] hides, int amount) {
        this.materials = materials;
        this.hides = hides;
        this.amount = amount;

        // hideに応じたItemStackを作成
        for (HideType hide : HideType.values()) {
            // 現在のhMatで隠される素材一覧を取得
            List<Integer> hiddenList = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                if (hides[i] == hide && materials[i] != 0) {
                    hiddenList.add(materials[i]);
                }
            }

            // ItemStackを作成
            ItemStack item = null;
            switch (hide) {
                case White -> item = GuiItem.getItem(GuiItemType.WoolWhite);
                case Gray -> item = GuiItem.getItem(GuiItemType.WoolGray);
                case Black -> item = GuiItem.getItem(GuiItemType.WoolBlack);
            }
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GOLD + "ぼやけた地点");
            List<String> lore = new ArrayList<>();
            for (int hiddenId : hiddenList) {
                ItemStack hiddenItem = ItemStackMain.getItem(hiddenId);
                if (hiddenItem == null || hiddenItem.getItemMeta() == null) continue;
                lore.add("");
                lore.add(ChatColor.WHITE + prefixes[new Random().nextInt(prefixes.length)] + " ‣ " + hiddenItem.getItemMeta().getDisplayName());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            hideItemMap.put(hide,item);
        }

        // 必要な素材数まとめ
        List<Integer> finished = new ArrayList<>();
        for (int id : materials) {
            // 素材が無い場所 || 既にカウント済み
            if (id == 0 || finished.contains(id)) continue;

            // 素材数をカウントする
            int count = (int) Arrays.stream(materials).filter(i -> i == id).count();
            itemAmounts.add(new ItemAmount(id,count));
            finished.add(id);
        }

        // isAutoCompleteを設定
        for (HideType h : hides) {
            if (h != null) {
                isAutoComplete = false;
                return;
            }
        }
        isAutoComplete = true;
    }

    /**
     * アイテムを作ったことがある段階（素材の表示）
     */
    public ItemStack[] getCompletedMaterials() {
        ItemStack[] items = new ItemStack[12];

        for (int i = 0; i < 12; i++) {
            // アイテムIDを取得
            int id = materials[i];
            if (id == 0) continue;

            // アイテムを取得
            ItemStack item = ItemStackMain.getItem(id);
            if (item == null) continue;
            item.setAmount(1);
            items[i] = item;
        }

        return items;
    }

    /**
     * レシピを取得した段階（素材の表示）
     */
    public ItemStack[] getFoundMaterials() {
        ItemStack[] items = new ItemStack[12];

        for (int i = 0; i < 12; i++) {
            // 隠されていた場合：隠すアイテムを取得
            if (hides[i] != null) {
                items[i] = hideItemMap.get(hides[i]).clone();
                continue;
            }

            // アイテムIDを取得
            int id = materials[i];
            if (id == 0) continue;

            // アイテムを取得
            ItemStack item = ItemStackMain.getItem(id);
            if (item == null) continue;
            item.setAmount(1);
            items[i] = item;
        }

        return items;
    }

}
