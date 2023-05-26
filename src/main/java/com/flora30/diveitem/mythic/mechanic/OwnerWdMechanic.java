package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveitem.DiveItem;
import com.flora30.divenew.data.PointObject;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class OwnerWdMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final double preDamage;

    public OwnerWdMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        preDamage = mlc.getDouble(new String[]{"amount","a"},0);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity t) {

        if (!t.isValid() || t.isDead() || t.getHealth() <= 0.0 || !t.isLiving())
            return false;
        //PVPを除外
        if(t.isPlayer()){
            return false;
        }

        UUID ownerUUID = MythicMobs.inst().getAPIHelper().getMythicMobInstance(data.getCaster().getEntity().getBukkitEntity()).getOwner().get();
        if (ownerUUID == null) {
            Bukkit.getLogger().info("[DiveItem-Mechanic] owner null");
            return false;
        }

        PlayerData pData = PlayerDataObject.INSTANCE.getPlayerDataMap().get(ownerUUID);
        double damage = applyPointRate(pData,preDamage);
        //ダメージが0である場合を除外
        if(damage == 0){
            return false;
        }

        //ダメージを与える
        Bukkit.getLogger().info("pre: "+preDamage+" | applied: "+damage);
        ((LivingEntity)t.getBukkitEntity()).setNoDamageTicks(0);

        // 原因不明だけど、こちらだけsyncする必要がある
        DiveItem.plugin.syncTask(() -> {
            ((LivingEntity)t.getBukkitEntity()).damage(damage,Bukkit.getPlayer(ownerUUID));
        });

        return true;
    }

    private double applyPointRate(PlayerData data, double damage){
        double rate = PointObject.INSTANCE.getAttackRate(data.getLevelData().getPointAtk());
        return damage * rate;
    }
}
