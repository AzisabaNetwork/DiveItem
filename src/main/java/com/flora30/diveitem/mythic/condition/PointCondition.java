package com.flora30.diveitem.mythic.condition;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

public class PointCondition extends SkillCondition implements IEntityCondition {
    private final int level;
    private final int skillPointType;

    public PointCondition(MythicLineConfig mlc) {
        super(mlc.getLine());
        level = mlc.getInteger("level",0);
        skillPointType = mlc.getInteger("type",0);
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if(!entity.isPlayer()){
            return false;
        }
        PlayerData data = CoreAPI.getPlayerData(entity.asPlayer().getUniqueId());

        switch (skillPointType){
            case 1:
                return level <= data.levelData.pointLuc;
            case 2:
                return level <= data.levelData.pointInt;
            case 3:
                return level <= data.levelData.pointVit;
            case 4:
                return level <= data.levelData.pointAtk;
        }
        return false;
    }
}
