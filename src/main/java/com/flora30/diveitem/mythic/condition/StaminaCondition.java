package com.flora30.diveitem.mythic.condition;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

public class StaminaCondition extends SkillCondition implements IEntityCondition {
    private final int stamina;

    public StaminaCondition(MythicLineConfig mlc) {
        super(mlc.getLine());
        stamina = mlc.getInteger("amount",0);
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if(!entity.isPlayer()){
            return false;
        }
        PlayerData data = CoreAPI.getPlayerData(entity.asPlayer().getUniqueId());

        return data.currentST >= stamina;
    }
}
