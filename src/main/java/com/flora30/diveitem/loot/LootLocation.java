package com.flora30.diveitem.loot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;

public class LootLocation {
    String world;
    int blockX;
    int blockY;
    int blockZ;
    BlockFace face;
    private boolean isPrepared = false;

    public boolean isPrepared() {
        return isPrepared;
    }

    public void setPrepared(boolean prepared) {
        isPrepared = prepared;
    }

    public BlockFace getFace() {
        return face;
    }

    public void setFace(BlockFace face) {
        this.face = face;
    }

    public String getWorld() {
        return world;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public Location getLocation(){
        World world = Bukkit.getWorld(getWorld());
        if (world == null){
            Bukkit.getLogger().info("[DiveCore-Loot]生成 - ワールドの取得に失敗しました");
            return null;
        }
        return world.getBlockAt(blockX,blockY,blockZ).getLocation();
    }

    public boolean check(Location location){
        if (location.getWorld() == null || !location.getWorld().getName().equals(world)){
            return false;
        }
        if (location.getBlockX() != blockX){
            return false;
        }
        if (location.getBlockY() != blockY){
            return false;
        }
        return location.getBlockZ() == blockZ;
    }

    public void setBlockX(int blockX) {
        this.blockX = blockX;
    }

    public void setBlockY(int blockY) {
        this.blockY = blockY;
    }

    public void setBlockZ(int blockZ) {
        this.blockZ = blockZ;
    }

    public void setWorld(String world) {
        this.world = world;
    }
}
