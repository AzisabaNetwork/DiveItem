package com.flora30.diveitem.loot;

import java.util.ArrayList;
import java.util.List;

public class LootGoods {
    //アイテムスタック+確率
    private final List<LootGood> itemList = new ArrayList<>();

    public void addItem(int id, int amount){
        LootGood good = new LootGood();
        good.setAmount(amount);
        good.setItemID(id);
        itemList.add(good);
    }

    public LootGood getGood(int slot){
        return itemList.get(slot);
    }

    public void remove(int slot){
        itemList.remove(slot);
    }

    public List<LootGood> getItemList() {
        return itemList;
    }
}
