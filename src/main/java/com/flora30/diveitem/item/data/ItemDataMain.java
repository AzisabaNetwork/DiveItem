package com.flora30.diveitem.item.data;

import com.flora30.diveapi.data.ItemData;
import com.flora30.diveapi.data.item.Rarity;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

import static com.flora30.diveapi.data.item.Rarity.*;

public class ItemDataMain {

    // ID | ItemData
    private static final Map<Integer, ItemData> itemDataMap = new HashMap<>();

    public static ItemData getItemData(int id) {
        return itemDataMap.get(id);
    }

    public static void setItemData(int id, ItemData data) {
        itemDataMap.put(id, data);
    }

}
