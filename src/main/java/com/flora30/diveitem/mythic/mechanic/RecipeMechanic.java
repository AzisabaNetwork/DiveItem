package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveitem.craft.CraftMain;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Player;

public class RecipeMechanic extends SkillMechanic implements ITargetedEntitySkill {
    final int recipeId;
    final int exp;

    public RecipeMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        recipeId = mlc.getInteger("id",0);
        exp = mlc.getInteger(new String[]{"exp"},0);
    }


    @Override
    public boolean castAtEntity(SkillMetadata skillData, AbstractEntity t) {

        if (!t.isValid() || t.isDead() || t.getHealth() <= 0.0 || !t.isLiving())
            return false;
        //プレイヤー以外からを除外
        if (!skillData.getCaster().getEntity().isPlayer()) {
            return false;
        }

        Player player = (Player)skillData.getCaster().getEntity().getBukkitEntity();
        CraftMain.onUseRecipeBook(player, recipeId, exp);

        return true;
    }
}
