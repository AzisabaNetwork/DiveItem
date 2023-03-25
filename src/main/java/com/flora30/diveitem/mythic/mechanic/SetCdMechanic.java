package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

import java.util.Map;

public class SetCdMechanic extends SkillMechanic implements INoTargetSkill {
    private final int amount;
    private final String name;

    public SetCdMechanic(String skill, MythicLineConfig mlc){
        super(skill, mlc);
        name = mlc.getString("name",null);
        amount = mlc.getInteger("amount",0);
    }

    @Override
    public boolean cast(SkillMetadata skillData) {
        //プレイヤー以外からを除外
        if(!skillData.getCaster().getEntity().isPlayer()){
            return false;
        }

        PlayerData data = CoreAPI.getPlayerData(skillData.getCaster().getEntity().getUniqueId());
        if (data == null){
            return false;
        }

        Map<String,Integer> coolDownMap = data.coolDownMap;
        coolDownMap.put(name,amount);
        return true;
    }
}
