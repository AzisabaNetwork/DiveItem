package com.flora30.diveitem.item;

import com.flora30.diveapi.data.ItemData;
import com.flora30.diveapi.data.item.Rarity;
import com.flora30.diveapi.tools.PlayerItem;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.item.data.ItemDataMain;
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
    //一時保存（アイテムエンティティ | プレイヤーUUID）
    public static final Map<Item, ItemEntityData> itemEntityMap = new HashMap<>();
    //一時保存（エンティティ | 攻撃したプレイヤーリスト）
    public static final Map<UUID, MobEntityData> mobMap = new HashMap<>();
    // 色の演出用
    public static final Set<Item> freeItems = new HashSet<>();

    //レア度 | 発光用のTeam
    public static final Map<Rarity, Team> colorTeamMap = new HashMap<>();


    public static int releaseTick = 200;

    ///////////////////////////
    ///////////////////////////
    ///////////////////////////

    public static Entity SpawnItem(ItemStack item, Location location, Player player){
        if(location.getWorld() == null) return null;
        Item itemEntity = location.getWorld().dropItem(location, item);
        putItem(itemEntity, player);
        return itemEntity;
    }

    public static void onDrop(PlayerDropItemEvent event){
        if (event.isCancelled()){
            return;
        }
        // コンパスは外す（MenuItem対策）
        if(event.getItemDrop().getItemStack().getType().equals(Material.COMPASS)) {
            return;
        }
        putItem(event.getItemDrop(),event.getPlayer());
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
            ItemData data = ItemDataMain.getItemData(ItemStackMain.getItemID(item));
            if (data == null || data.damage <= 0) {
                event.setCancelled(true);
                return;
            }

            putMob(event.getEntity().getUniqueId(),player);
        }
    }

    public static void onMerge(ItemMergeEvent e) {

        // 演出・判定を消去
        if (itemEntityMap.containsKey(e.getEntity())) {
            ItemEntityData data = itemEntityMap.get(e.getEntity());

            // 合流元・先のIDが違ったら回避
            ItemEntityData targetData = itemEntityMap.get(e.getTarget());
            if (targetData != null && targetData.getId() != data.getId()) {
                e.setCancelled(true);
                return;
            }

            Entity as = Bukkit.getEntity(data.armorStandId);
            if (as != null) {
                as.remove();
            }
            itemEntityMap.remove(e.getEntity());
        }

        freeItems.remove(e.getEntity());
    }

    public static void onDespawn(ItemDespawnEvent e) {
        // 演出・判定を消去

        if (itemEntityMap.containsKey(e.getEntity())) {
            e.setCancelled(true);
            return;
        }

        freeItems.remove(e.getEntity());
    }

    ///////////////////////////
    ///////////////////////////
    ///////////////////////////

    public static boolean canPickupItem(Item item, Player player){
        if (freeItems.contains(item)) {
            freeItems.remove(item);
            return true;
        }

        ItemEntityData data = itemEntityMap.get(item);
        if (data == null){
            return true;
        }

        if (data.getId().equals(player.getUniqueId())){
            Objects.requireNonNull(Bukkit.getEntity(data.armorStandId)).remove();
            itemEntityMap.remove(item);
            return true;
        }
        else{
            return false;
        }
    }

    public static void putMob(UUID uuid, Player player){
        if (mobMap.containsKey(uuid)){
            if (player.getUniqueId().equals(uuid)){
                mobMap.get(uuid).remain = releaseTick;
            }
        }
        else{
            MobEntityData data = new MobEntityData();
            data.remain = releaseTick;
            data.playerIdSet.add(player.getUniqueId());
            mobMap.put(uuid,data);
        }
    }

    public static void putItem(Item item, Player player){
        int itemId = ItemStackMain.getItemID(item.getItemStack());
        ItemData itemData = ItemDataMain.getItemData(itemId);
        if (itemData == null) return;

        ItemEntityData data = new ItemEntityData();
        data.setRemain(releaseTick);
        data.setId(player.getUniqueId());

        // 名前表示用のアーマースタンド
        ArmorStand armorStand = (ArmorStand) item.getWorld().spawnEntity(item.getLocation().add(0,-1.5,0),EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(player.getDisplayName());
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        data.armorStandId = armorStand.getUniqueId();

        // 発光色を追加
        Rarity rarity = itemData.rarity;
        colorTeamMap.get(rarity).addEntry(item.getUniqueId().toString());
        item.setGlowing(true);

        itemEntityMap.put(item,data);
    }

    public static void onTick(){

        // 一時保存の値を減少する
        for (ItemEntityData data : itemEntityMap.values()) {
            data.setRemain(data.getRemain() - 1);
        }
        for (MobEntityData data : mobMap.values()) {
            data.remain--;
        }


        // 演出（アーマースタンドの削除もここ）
        for (Item item : itemEntityMap.keySet()) {
            ItemEntityData data = itemEntityMap.get(item);
            Entity as = Bukkit.getEntity(data.armorStandId);
            if (as == null) {
                freeItems.add(item);
                break;
            }

            // 演出
            ItemData itemData = ItemDataMain.getItemData(ItemStackMain.getItemID(item.getItemStack()));
            if (itemData != null) {
                item.getWorld().spawnParticle(Particle.REDSTONE, item.getLocation().add(0, 0.3, 0), 2, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(getColor(itemData.rarity), (float) Math.random() * 0.6F + 0.4F));
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
        itemEntityMap.entrySet().removeIf(i -> Bukkit.getEntity(i.getValue().armorStandId) == null);

        for (Item item : freeItems) {
            // 演出
            ItemData data = ItemDataMain.getItemData(ItemStackMain.getItemID(item.getItemStack()));
            if (data != null) {
                item.getWorld().spawnParticle(Particle.REDSTONE,item.getLocation().add(0,0.3,0),2,0.2,0.2,0.2,0,new Particle.DustOptions(getColor(data.rarity),(float) Math.random()*0.6F+0.4F));
            }
        }

        // remain <= 0になったものを削除する
        itemEntityMap.entrySet().removeIf(entry -> entry.getValue().getRemain() <= 0);

        mobMap.entrySet().removeIf(entry -> entry.getValue().remain <= 0);
    }

    static final float[] speeds = {0.4F,0.6F,1F,1.5F,2F};

    /**
     * パケットでのアイテムドロップ演出（プレイヤー固有）
     */
    public static void playDropEffect(Player player, ItemStack item, Location location) {
        net.minecraft.world.entity.Entity entity = spawnPacketItem(player, item,location);
        Rarity rarity = ItemDataMain.getItemData(ItemStackMain.getItemID(((Item)entity.getBukkitEntity()).getItemStack())).rarity;
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
            PlayerItem.giveItem(player,((Item)entity.getBukkitEntity()).getItemStack());

            // エンティティを削除するパケット
            ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(entity.getId());
            packetSender.send(removePacket);
            colorTeamMap.get(rarity).removeEntry(entity.getUUID().toString());
            entity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }

    public static net.minecraft.world.entity.Entity spawnPacketItem(Player player, ItemStack item, Location loc) {
        // レアリティ
        Rarity rarity = ItemDataMain.getItemData(ItemStackMain.getItemID(item)).rarity;
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
            Entity armorstand = Bukkit.getEntity(data.armorStandId);
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
