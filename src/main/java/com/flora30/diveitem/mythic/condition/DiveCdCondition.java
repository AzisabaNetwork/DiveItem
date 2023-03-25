package com.flora30.diveitem.mythic.condition;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

public class DiveCdCondition extends SkillCondition implements IEntityCondition {
    private final String name;
    private final int amount;
    private final String type;

    public DiveCdCondition(MythicLineConfig mlc) {
        super(mlc.getLine());
        name = mlc.getString("name",null);
        amount = mlc.getInteger("amount",0);
        type = mlc.getString("type",null);
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if(!entity.isPlayer()){
            return false;
        }
        PlayerData data = CoreAPI.getPlayerData(entity.getUniqueId());
        if (data == null){
            return false;
        }

        if (data.coolDownMap.get(name) == null){
            return false;
        }

        if (type == null){
            return data.coolDownMap.get(name) == this.amount;
        }

        switch (type){
            case "up":
                return data.coolDownMap.get(name) > this.amount;
            case "down":
                return data.coolDownMap.get(name) < this.amount;
            default:
                return data.coolDownMap.get(name) == this.amount;
        }
    }
}
