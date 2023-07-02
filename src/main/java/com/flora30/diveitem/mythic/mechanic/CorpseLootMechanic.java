package com.flora30.diveitem.mythic.mechanic;

import com.flora30.divelib.data.gimmick.action.ChestType;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.gui.CorpseLootGUI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CorpseLootMechanic extends SkillMechanic implements ITargetedEntitySkill {


    private final String lootID;

    public CorpseLootMechanic(String skill, MythicLineConfig mlc){
        super(skill, mlc);
        lootID = mlc.getString(new String[]{"lootid","id"},"");
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity t) {
        if (!t.isValid() || t.isDead() || t.getHealth() <= 0.0 || !t.isLiving())
            return false;
        //プレイヤー対象のみ
        if(!(t.isPlayer())){
            return false;
        }

        // 死体GUIを表示
        CorpseLootGUI.INSTANCE.open(((Player) t.getBukkitEntity()).getPlayer(),lootID);
        return true;
    }
}
