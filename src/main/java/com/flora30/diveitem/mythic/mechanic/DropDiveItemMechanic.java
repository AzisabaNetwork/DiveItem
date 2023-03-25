package com.flora30.diveitem.mythic.mechanic;

import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.item.ItemEntityMain;
import com.flora30.diveitem.item.ItemStackMain;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DropDiveItemMechanic extends SkillMechanic implements ITargetedEntitySkill {
    final int id;
    final int amount;

    public DropDiveItemMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        id = mlc.getInteger("id",0);
        amount = mlc.getInteger(new String[]{"amount"},0);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        ItemStack itemStack = ItemStackMain.getItemAsync(id);
        //itemStack作成
        itemStack.setAmount(amount);
        //mobId判定
        UUID mobId = abstractEntity.getUniqueId();

        if (ItemEntityMain.mobMap.containsKey(mobId)){
            // mobを攻撃したプレイヤーSet
            for (UUID playerId : ItemEntityMain.mobMap.get(mobId).playerIdSet) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null) continue;

                //item召喚
                Bukkit.getScheduler().runTask(DiveItem.plugin, ()->ItemEntityMain.playDropEffect(player,itemStack,abstractEntity.getBukkitEntity().getLocation()));
            }
        }
        return true;
    }
}
