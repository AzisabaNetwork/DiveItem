package com.flora30.diveitem.gather;


import com.flora30.diveapi.tools.ToolType;

import java.util.HashMap;
import java.util.Map;

/**
 * 各層の採集についてのデータ
 */
public class GatherLayerData {
    private final Map<ToolType, Integer> normalDropMap = new HashMap<>();
    private final Map<Integer,Float> relicRateMap = new HashMap<>();
    private final Map<String, Float> monsterMap = new HashMap<>();
    private final Map<String, Float> fishMonsterMap = new HashMap<>();

    //////////////////////////
    public int getNormalDrop(ToolType type) {
        return normalDropMap.get(type);
    }

    public Map<Integer, Float> getRelicRateMap() {
        return relicRateMap;
    }

    public Map<String, Float> getMonsterMap() {
        return monsterMap;
    }

    public Map<String, Float> getFishMonsterMap() {
        return fishMonsterMap;
    }

    public String getRandomMob() {
        float n = 0.0F;
        for(String mobId : monsterMap.keySet()){
            n += monsterMap.get(mobId);
            if(Math.random() <= n){
                return mobId;
            }
        }

        return null; // いない
    }
    public String getRandomFishMob() {
        float n = 0.0F;
        for(String mobId : fishMonsterMap.keySet()){
            n += fishMonsterMap.get(mobId);
            if(Math.random() <= n){
                return mobId;
            }
        }

        return null; // いない
    }

    //////////////////////////
    public void setNormalDrop(ToolType type, int itemId){
        normalDropMap.put(type, itemId);
    }

    public void setRelicDrop(int id, float rate) {
        relicRateMap.put(id, rate);
    }

    public void setMonster(String id, float rate) {
        monsterMap.put(id, rate);
    }

    public void setFishMonster(String id, float rate) {
        fishMonsterMap.put(id, rate);
    }
}