package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.data.Point;
import com.flora30.diveapi.plugins.CoreAPI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class ArtifactDamageMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final double preDamage;

    public ArtifactDamageMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        preDamage = mlc.getDouble(new String[]{"amount","a"},0);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity t) {
        if (!t.isValid() || t.isDead() || t.getHealth() <= 0.0 || !t.isLiving())
            return false;
        //プレイヤー以外から｜PVPを除外
        if(!data.getCaster().getEntity().isPlayer() || t.isPlayer()){
            return false;
        }
        /*
        //ダメージを与えられない場合を除外
        if(((LivingEntity)t.getBukkitEntity()).getNoDamageTicks() >= 0){
            return false;
        }
         */

        PlayerData pData = CoreAPI.getPlayerData(data.getCaster().getEntity().getUniqueId());
        double damage = applyPointRate(pData,preDamage);
        //ダメージが0である場合を除外
        if(damage == 0){
            return false;
        }

        //ダメージを与える
        Bukkit.getLogger().info("pre: "+preDamage+" | applied: "+damage);
        ((LivingEntity)t.getBukkitEntity()).setNoDamageTicks(0);
        ((LivingEntity)t.getBukkitEntity()).damage(damage,data.getCaster().getEntity().getBukkitEntity());

        return true;
    }

    private double applyPointRate(PlayerData data, double damage){
        double rate = Point.convertArtifact(data.levelData.pointAtk);
        return damage * rate;
    }
}
