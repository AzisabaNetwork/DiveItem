package com.flora30.diveitem.mythic;

import com.flora30.diveitem.mythic.mechanic.*;
import com.flora30.diveitem.mythic.condition.*;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicConditionLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicListener implements Listener {

    @EventHandler
    public void onLoadMechanic(MythicMechanicLoadEvent e){
        //Bukkit.getLogger().info("MythicMechanicLoadEvent called for mechanic " + e.getMechanicName());
        switch(e.getMechanicName()){
            case "ARTIFACTDM" -> {
                SkillMechanic skill = new ArtifactDamageMechanic(e.getContainer().getConfigLine(), e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]arifactDamageを登録");
            }
            case "USESTAMINA" -> {
                SkillMechanic skill = new UseStaminaMechanic(e.getContainer().getConfigLine(),e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]useStaminaを登録");
            }
            case "WEAPONDM" -> {
                SkillMechanic skill = new WeaponDamageMechanic(e.getContainer().getConfigLine(),e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]weaponDamageを登録");
            }
            case "SETCD" -> {
                SkillMechanic skill = new SetCdMechanic(e.getContainer().getConfigLine(),e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]setCdを登録");
            }
            case "DISPLAYCD" -> {
                SkillMechanic skill = new DisplayCdMechanic(e.getContainer().getConfigLine(),e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]displayCdを登録");
            }
            case "DROPDIVEITEM" -> {
                SkillMechanic skill = new DropDiveItemMechanic(e.getContainer().getConfigLine(),e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]dropDiveItemを登録");
            }
            case "USEROPE" -> {
                SkillMechanic skill = new UseRopeMechanic(e.getContainer().getConfigLine(),e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]useRopeを登録");
            }
            case "RECIPE" -> {
                SkillMechanic skill = new RecipeMechanic(e.getContainer().getConfigLine(), e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]recipeを登録");
            }
            case "OWNERWEAPONDM" -> {
                SkillMechanic skill = new OwnerWdMechanic(e.getContainer().getConfigLine(), e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]ownerWeaponDMを登録");
            }
            case "CORPSELOOT" -> {
                SkillMechanic skill = new CorpseLootMechanic(e.getContainer().getConfigLine(), e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]corpseLootを登録");
            }
        }
    }

    @EventHandler
    public void onLoadCondition(MythicConditionLoadEvent e){
        //Bukkit.getLogger().info("MythicConditionLoadEvent called for condition " + e.getConditionName());
        switch(e.getConditionName()){
            case "point":
                SkillCondition skill = new PointCondition(e.getConfig());
                e.register(skill);
                Bukkit.getLogger().info("[DiveItem-Mythic]point(Condition)を登録");
                break;
            case "stamina":
                SkillCondition condition2 = new StaminaCondition(e.getConfig());
                e.register(condition2);
                Bukkit.getLogger().info("[DiveItem-Mythic]stamina(Condition)を登録");
                break;
            case "diveCd":
                SkillCondition condition3 = new DiveCdCondition(e.getConfig());
                e.register(condition3);
                Bukkit.getLogger().info("[DiveItem-Mythic]diveCd(Condtiion)を登録");
            default:
        }
    }
}
