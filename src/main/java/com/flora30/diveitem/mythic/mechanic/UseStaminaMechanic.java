package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.event.HelpEvent;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.tools.HelpType;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UseStaminaMechanic  extends SkillMechanic implements ITargetedEntitySkill {
    private final int amount;

    public UseStaminaMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        amount = mlc.getInteger(new String[]{"amount","a"},0);
    }


    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity t) {
        if (!t.isValid() || t.isDead() || t.getHealth() <= 0.0 || !t.isLiving())
            return false;
        //プレイヤー以外からを除外
        if (!data.getCaster().getEntity().isPlayer()) {
            return false;
        }

        Player player = (Player) data.getCaster().getEntity().getBukkitEntity();
        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.UseStamina));
        PlayerData pData = CoreAPI.getPlayerData(player.getUniqueId());
        pData.currentST -= amount;
        return true;
    }
}
