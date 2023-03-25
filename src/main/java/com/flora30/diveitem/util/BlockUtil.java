package com.flora30.diveitem.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockUtil {

    //無条件に通れるブロック
    public static boolean isIgnoreBlockType(Block block) {
        return isIgnoreBlockType(block.getType());
    }

    public static boolean isIgnoreBlockType(Material material) {
        String type =material.toString();
        //サンゴ
        if(type.endsWith("CORAL")){
            return true;
        }
        //サンゴその２
        if(type.endsWith("FAN")){
            return true;
        }
        //看板
        if(type.endsWith("SIGN")){
            return true;
        }
        //旗
        if(type.endsWith("BANNER")){
            return true;
        }
        //感圧版
        if(type.endsWith("PLATE")){
            return true;
        }
        //ボタン
        if(type.endsWith("BUTTON")){
            return true;
        }
        //松明
        if(type.endsWith("TORCH")){
            return true;
        }
        //レール
        if(type.endsWith("RAIL")){
            return true;
        }
        switch (material) {
            //空気系
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                //草系
            case GRASS:
            case FERN:
            case DEAD_BUSH:
            case TALL_GRASS:
            case LARGE_FERN:
            case VINE:
                //作物系
            case WHEAT:
            case BEETROOTS:
            case CARROTS:
            case POTATOES:
            case SUGAR_CANE:
            case SWEET_BERRY_BUSH:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case NETHER_WART:
                //苗木系
            case SPRUCE_SAPLING:
            case ACACIA_SAPLING:
            case BAMBOO_SAPLING:
            case BIRCH_SAPLING:
            case DARK_OAK_SAPLING:
            case JUNGLE_SAPLING:
            case OAK_SAPLING:
                //花系
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case WITHER_ROSE:
            case SUNFLOWER:
            case ROSE_BUSH:
            case LILAC:
            case PEONY:
                //液体系
            case WATER:
            case LAVA:
                //水草系
            case SEAGRASS:
            case TALL_SEAGRASS:
            case KELP_PLANT:
            case KELP:
                //その他
            case COBWEB:
            case SCAFFOLDING:
            case PAINTING:
            case ITEM_FRAME:
            case ARMOR_STAND:
            case REDSTONE:
            case REDSTONE_WIRE:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case STRING:
                return true;
        }
        return false;
    }
}
