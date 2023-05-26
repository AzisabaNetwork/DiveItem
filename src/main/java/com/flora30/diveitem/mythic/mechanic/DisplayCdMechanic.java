package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveapin.ItemEntityObject;
import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

public class DisplayCdMechanic extends SkillMechanic implements INoTargetSkill {
    private final String name;

    public DisplayCdMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        name = mlc.getString("name",null);
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        if (!skillMetadata.getCaster().getEntity().isPlayer()){
            return false;
        }
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(skillMetadata.getCaster().getEntity().getUniqueId());
        data.setDisplayCoolDownName(name);
        return true;
    }
}
