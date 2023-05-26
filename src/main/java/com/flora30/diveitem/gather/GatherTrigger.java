package com.flora30.diveitem.gather;

import com.flora30.diveapin.ItemMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.diveitem.mythic.DMythicUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class GatherTrigger {
    public static void onBreakBlock(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        int itemId = ItemMain.INSTANCE.getItemId(player.getInventory().getItemInMainHand());
        GatherMain.gather(player, location, itemId);
    }

    public static void onGatherSendTick(Player player){
        GatherMain.checkBedrock(player);
    }


    // 演出に使うMobの名前
    private static final String biteMobName = "FishingEffectOne";

    public static void onFish(PlayerFishEvent event) {
        //Bukkit.getLogger().info("Fishing listened");
        //Bukkit.getLogger().info("State : " + event.getState().name());
        //Bukkit.getLogger().info("HookState : " + event.getHook().getState().name());
        //Bukkit.getLogger().info("CaughtEntity : " + event.getCaught());
        if(event.getCaught() != null){
            //Bukkit.getLogger().info("CaughtEntityType : " + event.getCaught().getType());
        }

        switch (event.getState()) {
            // 釣りを開始したとき
            case FISHING -> {
                event.getHook().setMinWaitTime(20);
                event.getHook().setMaxWaitTime(50);
                //Bukkit.getLogger().info("MaxWait : " + event.getHook().getMaxWaitTime());
                //Bukkit.getLogger().info("MinWait : " + event.getHook().getMinWaitTime());
            }
            // 獲物がかかったとき
            case BITE -> {
                // 演出を行う
                DMythicUtil.spawnMob(biteMobName, event.getHook().getLocation());
                event.getPlayer().playSound(event.getHook().getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH,1,1);
            }
            // 釣ったとき
            case CAUGHT_FISH -> {
                ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
                ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();

                int itemId;

                // オフハンド対応・どちらにも釣り竿が無い場合はキャンセル
                if (mainHandItem.getType() == Material.FISHING_ROD) {
                    itemId = ItemMain.INSTANCE.getItemId(mainHandItem);
                }
                else if (offHandItem.getType() == Material.FISHING_ROD) {
                    itemId = ItemMain.INSTANCE.getItemId(offHandItem);
                }
                else {
                    event.setCancelled(true);
                    return;
                }

                GatherMain.gather(event.getPlayer(), event, itemId);
            }
        }

    }
}
