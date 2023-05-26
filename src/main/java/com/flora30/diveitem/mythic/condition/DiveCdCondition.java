package com.flora30.diveitem.mythic.condition;

import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
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
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(entity.getUniqueId());
        if (data == null){
            return false;
        }

        if (data.getCoolDownMap().get(name) == null){
            return false;
        }

        if (type == null){
            return data.getCoolDownMap().get(name) == this.amount;
        }

        return switch (type) {
            case "up" -> data.getCoolDownMap().get(name) > this.amount;
            case "down" -> data.getCoolDownMap().get(name) < this.amount;
            default -> data.getCoolDownMap().get(name) == this.amount;
        };
    }
}
