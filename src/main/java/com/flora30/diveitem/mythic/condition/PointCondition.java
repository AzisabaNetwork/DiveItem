package com.flora30.diveitem.mythic.condition;

import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
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
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(entity.getUniqueId());

        return switch (skillPointType) {
            case 1 -> level <= data.getLevelData().getPointLuc();
            case 2 -> level <= data.getLevelData().getPointInt();
            case 3 -> level <= data.getLevelData().getPointVit();
            case 4 -> level <= data.getLevelData().getPointAtk();
            default -> false;
        };
    }
}
