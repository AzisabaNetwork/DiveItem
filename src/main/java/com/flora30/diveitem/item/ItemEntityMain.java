package com.flora30.diveitem.item;

import com.flora30.divelib.ItemEntityData;
import com.flora30.divelib.ItemEntityObject;
import com.flora30.divelib.ItemMain;
import com.flora30.divelib.MobEntityData;
import com.flora30.divelib.data.Rarity;
import com.flora30.divelib.event.PutItemEntityEvent;
import com.flora30.diveitem.DiveItem;
import com.flora30.divelib.data.item.ItemData;
import com.flora30.divelib.data.item.ItemDataObject;
import com.flora30.divelib.util.PlayerItem;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ItemEntityMain {

    // DiveLibのクラスを引用
    private static final Map<Item,ItemEntityData> itemEntityMap = ItemEntityObject.INSTANCE.getItemEntityMap();
    private static final Map<UUID,MobEntityData> mobMap = ItemEntityObject.INSTANCE.getMobMap();
    private static final Set<Item> freeItems = ItemEntityObject.INSTANCE.getFreeItemSet();
    private static final Map<Rarity,Team> colorTeamMap = ItemEntityObject.INSTANCE.getColorTeamMap();


    ///////////////////////////
    ///////////////////////////
    ///////////////////////////

    public static void onDrop(PlayerDropItemEvent event){
        if (event.isCancelled()){
            return;
        }
        // コンパスは外す（MenuItem対策）
        if(event.getItemDrop().getItemStack().getType().equals(Material.COMPASS)) {
            return;
        }
        ItemEntityObject.INSTANCE.putItem(event.getItemDrop(),event.getPlayer());
    }

    public static void onPickupItem(EntityPickupItemEvent event){
        if (!(event.getEntity() instanceof Player player)){
            return;
        }

        if (!canPickupItem(event.getItem(),player)){
            event.setCancelled(true);
        }
    }

    public static void onAttackMob(EntityDamageByEntityEvent event){
        if ((event.getDamager() instanceof Player player)){
            // pvpは停止
            if (event.getEntity() instanceof Player) {
                event.setCancelled(true);
                return;
            }
            // 攻撃力の無いアイテムは停止（遺物は？）
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemData data = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(item));
            if (data == null || data.getDamage() <= 0) {
                event.setCancelled(true);
                return;
            }

            putMob(event.getEntity().getUniqueId(),player);
        }
    }

    public static void onMerge(ItemMergeEvent e) {
        Map<Item, ItemEntityData> map = ItemEntityObject.INSTANCE.getItemEntityMap();

        // 演出・判定を消去
        if (ItemEntityObject.INSTANCE.getItemEntityMap().containsKey(e.getEntity())) {
            ItemEntityData data = map.get(e.getEntity());

            // 合流元・先のIDが違ったら回避
            ItemEntityData targetData = map.get(e.getTarget());
            if (targetData != null && targetData.getId() != data.getId()) {
                e.setCancelled(true);
                return;
            }

            Entity as = Bukkit.getEntity(data.getArmorStandID());
            if (as != null) {
                as.remove();
            }
            map.remove(e.getEntity());
        }

        ItemEntityObject.INSTANCE.getFreeItemSet().remove(e.getEntity());
    }

    public static void onDespawn(ItemDespawnEvent e) {
        // 演出・判定を消去

        if (ItemEntityObject.INSTANCE.getItemEntityMap().containsKey(e.getEntity())) {
            e.setCancelled(true);
            return;
        }

        ItemEntityObject.INSTANCE.getFreeItemSet().remove(e.getEntity());
    }

    ///////////////////////////
    ///////////////////////////
    ///////////////////////////

    public static boolean canPickupItem(Item item, Player player){
        if (ItemEntityObject.INSTANCE.getFreeItemSet().contains(item)) {
            ItemEntityObject.INSTANCE.getFreeItemSet().remove(item);
            return true;
        }

        ItemEntityData data = ItemEntityObject.INSTANCE.getItemEntityMap().get(item);
        if (data == null){
            return true;
        }

        if (data.getId().equals(player.getUniqueId())){
            Objects.requireNonNull(Bukkit.getEntity(data.getArmorStandID())).remove();
            ItemEntityObject.INSTANCE.getItemEntityMap().remove(item);
            return true;
        }
        else{
            return false;
        }
    }

    public static void putMob(UUID uuid, Player player){
        Map<UUID,MobEntityData> mobMap = ItemEntityObject.INSTANCE.getMobMap();

        if (mobMap.containsKey(uuid)){
            if (player.getUniqueId().equals(uuid)){
                mobMap.get(uuid).setRemain(ItemEntityObject.INSTANCE.getReleaseTick());
            }
        }
        else{
            MobEntityData data = new MobEntityData(ItemEntityObject.INSTANCE.getReleaseTick());
            data.getPlayerIdSet().add(player.getUniqueId());
            mobMap.put(uuid,data);
        }
    }

    /**
     * アイテムに発光色を追加
     */
    public static void onPutItem(PutItemEntityEvent event) {
        int itemId = ItemMain.INSTANCE.getItemId(event.getItem().getItemStack());
        ItemData itemData = ItemDataObject.INSTANCE.getItemDataMap().get(itemId);
        if (itemData == null) return;

        // 発光色を追加
        Rarity rarity = itemData.getRarity();
        ItemDataObject.INSTANCE.getColorTeamMap().get(rarity).addEntry(event.getItem().getUniqueId().toString());
        event.getItem().setGlowing(true);
    }

    public static void onTick(){

        // 一時保存の値を減少する
        for (ItemEntityData data : itemEntityMap.values()) {
            data.setRemain(data.getRemain() - 1);
        }
        for (MobEntityData data : mobMap.values()) {
            data.setRemain(data.getRemain()-1);
        }


        // 演出（アーマースタンドの削除もここ）
        for (Item item : itemEntityMap.keySet()) {
            ItemEntityData data = itemEntityMap.get(item);
            Entity as = Bukkit.getEntity(data.getArmorStandID());
            if (as == null) {
                freeItems.add(item);
                break;
            }

            // 演出
            ItemData itemData = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(item.getItemStack()));
            if (itemData != null) {
                item.getWorld().spawnParticle(Particle.REDSTONE, item.getLocation().add(0, 0.3, 0), 2, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(getColor(itemData.getRarity()), (float) Math.random() * 0.6F + 0.4F));
            }

            // アイテムのプレイヤー帰属残り時間がある（通常時）
            if (data.getRemain() > 0) {
                as.teleport(item.getLocation().add(0,0.3,0));
                //Bukkit.getLogger().info("armorStandLoc = "+as.getLocation().getX()+","+as.getLocation().getY()+","+as.getLocation().getZ());
            }
            else {
                // 残り時間がない -> アーマースタンドが削除され、freeに追加
                as.remove();
                freeItems.add(item);
            }
        }

        // 何かの原因でアマスタが無くなっているものを所有物から外す
        itemEntityMap.entrySet().removeIf(i -> Bukkit.getEntity(i.getValue().getArmorStandID()) == null);

        for (Item item : freeItems) {
            // 演出
            ItemData data = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(item.getItemStack()));
            if (data != null) {
                item.getWorld().spawnParticle(Particle.REDSTONE,item.getLocation().add(0,0.3,0),2,0.2,0.2,0.2,0,new Particle.DustOptions(getColor(data.getRarity()),(float) Math.random()*0.6F+0.4F));
            }
        }

        // remain <= 0になったものを削除する
        itemEntityMap.entrySet().removeIf(entry -> entry.getValue().getRemain() <= 0);

        mobMap.entrySet().removeIf(entry -> entry.getValue().getRemain() <= 0);
    }

    static final float[] speeds = {0.4F,0.6F,1F,1.5F,2F};

    /**
     * パケットでのアイテムドロップ演出（プレイヤー固有）
     */
    public static void playDropEffect(Player player, ItemStack item, Location location) {
        net.minecraft.world.entity.Entity entity = spawnPacketItem(player, item,location);
        Rarity rarity = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(((Item)entity.getBukkitEntity()).getItemStack())).getRarity();
        moveToPlayer(player,entity,rarity, 1);
    }

    public static void moveToPlayer(Player player, net.minecraft.world.entity.Entity entity, Rarity rarity, int count) {

        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl packetSender = serverPlayer.connection;
        Vec3 entityPos = entity.position();
        Vec3 playerPos = serverPlayer.position().add(0,0.7,0);

        // プレイヤーが取れる距離じゃない場合
        if (entityPos.distanceTo(playerPos) > 1) {
            // 移動
            float speed = speeds[Math.min(count / 10, speeds.length - 1)];
            Vec3 vector = entityPos.vectorTo(playerPos).normalize().scale(speed);
            entity.move(MoverType.SELF,vector);

            // 動きを与えるパケット
            ClientboundSetEntityMotionPacket motionPacket = new ClientboundSetEntityMotionPacket(entity.getId(),vector);
            packetSender.send(motionPacket);

            // 常時演出
            player.spawnParticle(Particle.REDSTONE,entityPos.x,entityPos.y+0.3,entityPos.z,2,0.2,0.2,0.2,0,new Particle.DustOptions(getColor(rarity),(float) Math.random()*0.6F+0.4F));
            //ClientboundTeleportEntityPacket teleportPacket = new ClientboundTeleportEntityPacket(entity);
            //packetSender.send(teleportPacket);
            DiveItem.plugin.delayedTask(1,() -> moveToPlayer(player,entity,rarity, count+1));
        }
        else {
            // 取得時
            player.playSound(player.getLocation(),Sound.ENTITY_ITEM_PICKUP,1,1);
            PlayerItem.INSTANCE.giveItem(player,((Item)entity.getBukkitEntity()).getItemStack());

            // エンティティを削除するパケット
            ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(entity.getId());
            packetSender.send(removePacket);
            colorTeamMap.get(rarity).removeEntry(entity.getUUID().toString());
            entity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }

    public static net.minecraft.world.entity.Entity spawnPacketItem(Player player, ItemStack item, Location loc) {
        // レアリティ
        Rarity rarity = ItemDataObject.INSTANCE.getItemDataMap().get(ItemMain.INSTANCE.getItemId(item)).getRarity();
        //Bukkit.getLogger().info("mmDrop: rarity = "+rarity.toString());

        ServerLevel serverWorld = ((CraftWorld)player.getWorld()).getHandle();
        // ServerWorld、座標xyz、ItemStack（＋動きxyz）


        ItemEntity entity = new ItemEntity(serverWorld, loc.getX(),loc.getY()+1, loc.getZ(), CraftItemStack.asNMSCopy(item));
        entity.makeFakeItem();
        entity.setUnlimitedLifetime();
        entity.setNoGravity(true);
        entity.setGlowingTag(true);
        colorTeamMap.get(rarity).addEntry(entity.getUUID().toString());
        //Bukkit.getLogger().info("color = "+colorTeamMap.get(rarity).getColor().name());
        // エンティティを追加するパケット（２つ必要）
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(entity);
        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entity.getId(),entity.getEntityData(),true);

        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl packetSender = serverPlayer.connection;
        packetSender.send(packet);
        packetSender.send(dataPacket);

        // アイテムドロップ時の音
        player.playSound(loc,Sound.ENTITY_ENDER_EYE_DEATH,1,1);

        return entity;
    }

    // サーバー停止時にアマスタの紐づけ消去
    public static void removeArmorstands(){
        for (ItemEntityData data : itemEntityMap.values()) {
            Entity armorstand = Bukkit.getEntity(data.getArmorStandID());
            if (armorstand != null) {
                armorstand.remove();
            }
        }
        Bukkit.getLogger().info("[DiveItem]名前アーマースタンドを消去しました");
    }

    public static Color getColor(Rarity rarity) {
        return switch (rarity) {
            case Unusual -> Color.fromRGB(0,170,0); // DARK_GREEN
            case Rare -> Color.fromRGB(0,0,170); // DARK_BLUE
            case Epic -> Color.fromRGB(170,0,170); // DARK_PURPLE
            case Legendary -> Color.fromRGB(255,170,0); // GOLD
            case Blessed -> Color.fromRGB(170,0,0); // DARK_RED
            default -> Color.fromRGB(255,255,255); // WHITE
        };
    }
    public static ChatColor getChatColor(Rarity rarity) {
        return switch (rarity) {
            case Unusual -> ChatColor.DARK_GREEN;
            case Rare -> ChatColor.DARK_BLUE;
            case Epic -> ChatColor.DARK_PURPLE;
            case Legendary -> ChatColor.GOLD;
            case Blessed -> ChatColor.DARK_RED;
            default -> ChatColor.WHITE;
        };
    }
}
