package com.flora30.diveitem.loot;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Loot {
    public final List<LootLocation> locationList = new ArrayList<>();
    //各レベルあたりの報酬リスト
    private final List<LootGoods> lootGoodsList = new ArrayList<>(10);

    public Loot(){
        lootGoodsList.add(null);
        lootGoodsList.add(null);
        lootGoodsList.add(null);
    }

    public void addLocation(LootLocation location){
        locationList.add(location);
    }

    public void removeLocation(LootLocation location){
        locationList.remove(location);
    }

    public LootLocation getLootLoc(int id){
        try{
            return locationList.get(id);
        } catch (IndexOutOfBoundsException e){
            Bukkit.getLogger().info("[DiveCore-Loot]座標id["+id+"]が存在数["+locationList.size()+"]を越えました");
            return null;
        }
    }

    public int getLocationAmount(){
        return locationList.size();
    }

    public int getID(Location location){
        //無い場合は-1
        for(int i = 0; i < locationList.size(); i++){
            LootLocation lootLoc = locationList.get(i);
            if (lootLoc.check(location)){
                return i;
            }
        }
        return -1;
    }

    public LootGoods getLootGood(int lv) {
        return lootGoodsList.get(lv-1);
    }

    public void setLootGoods(int lv, LootGoods lootGoods) {
        lootGoodsList.set(lv-1,lootGoods);
    }

    public int getMaxLevel(){
        return lootGoodsList.size();
    }
}
