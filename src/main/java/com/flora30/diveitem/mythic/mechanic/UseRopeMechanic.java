package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveitem.rope.RopeMain;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Player;

public class UseRopeMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final int id;

    public UseRopeMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        id = mlc.getInteger(new String[]{"id","itemId"},0);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillData, AbstractEntity t) {
        if (!t.isValid() || t.isDead() || t.getHealth() <= 0.0 || !t.isLiving())
            return false;
        //プレイヤー以外からを除外
        if (!skillData.getCaster().getEntity().isPlayer()) {
            return false;
        }

        RopeMain.use((Player)skillData.getCaster().getEntity().getBukkitEntity(), id);
        return true;
    }
}
