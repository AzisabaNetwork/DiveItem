package com.flora30.diveitem.gather;

import com.flora30.diveapi.data.item.ToolData;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveapi.tools.Config;
import com.flora30.diveapi.tools.ToolType;
import com.flora30.diveitem.item.data.ItemDataMain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class GatherConfig extends Config {

    private static File[] areaFiles = new File[100];

    public GatherConfig(){
        folderCheck(RegionAPI.region.getDataFolder().getAbsolutePath() + "/area");
        areaFiles = new File(RegionAPI.region.getDataFolder().getAbsolutePath() + "/area").listFiles();
    }

    /**
     * @return 設定後のItemStackが返ってくるよ！
     */
    public static ItemStack setMineBlock(int itemId, ItemStack item) {
        // 必要な ItemData を取得
        if(ItemDataMain.getItemData(itemId) == null) return item;
        ToolData data = ItemDataMain.getItemData(itemId).gatherData;
        if(data.toolType == ToolType.None) return item;
        // 必要な nms版のItemStack を取得
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);

        // Material を StringTag にしてリストに入れる
        // 後で tag に設定するための準備
        ListTag listTag = new ListTag();
        for(Material material : data.breakAbleMaterialSet){
            //Bukkit.getLogger().info("Material.send = minecraft:" + material.toString().toLowerCase());
            listTag.add(StringTag.valueOf("minecraft:" + material.toString().toLowerCase()));
        }

        CompoundTag tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new CompoundTag();
        assert tag != null;

        // 上で作ったものを、壊せるブロックとして設定
        tag.put("CanDestroy", listTag);
        nmsItemStack.setTag(tag);
        Bukkit.getLogger().info("[DiveCore-GatherLS] アイテム " + itemId + " に破壊可能ブロックを設定しました");

        // 普通のItemStackにして返却
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public void load() {
        //areaフォルダでループ
        for(File file2 : areaFiles) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file2);

            //フォルダ内の1番目でループ(key=layerID)
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }

                // gather がないとき
                if (!(section.isConfigurationSection("gather"))) {
                    Bukkit.getLogger().info("[DiveCore-Gather]階層「"+key+"」のgather記述がありません");
                    continue;
                }

                ConfigurationSection normalDropSection = section.getConfigurationSection("gather.normalDrop");
                ConfigurationSection relicRateSection = section.getConfigurationSection("gather.relicRate");
                ConfigurationSection monsterSection = section.getConfigurationSection("gather.monster");
                ConfigurationSection fishMonsterSection = section.getConfigurationSection("gather.fishMonster");

                // いずれかがないとき
                if (normalDropSection == null || relicRateSection == null || monsterSection == null || fishMonsterSection == null) {
                    Bukkit.getLogger().info("[DiveCore-Gather]階層「"+key+"」のgather記述が不足しています");
                    continue;
                }

                // 採掘の階層側データを作成
                GatherLayerData gatherLayerData = new GatherLayerData();

                // normalDrop の中は ToolType : ItemID
                for(String toolTypeStr : normalDropSection.getKeys(false)) {
                    try{
                        ToolType type = ToolType.valueOf(toolTypeStr);
                        int dropId = loadOrDefault("Gather", normalDropSection, toolTypeStr, -1);

                        if(dropId == -1) { continue; }

                        gatherLayerData.setNormalDrop(type, dropId);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-Gather]階層「"+key+"」の採集通常ドロップ「"+toolTypeStr+"」の取得に失敗しました");
                    }
                }

                // relicRate の中は ItemId : 確率（小数）
                for(String relicIdStr : relicRateSection.getKeys(false)) {
                    try{
                        int relicId = Integer.parseInt(relicIdStr);
                        float rate = (float) loadOrDefault("Gather", relicRateSection, relicIdStr, 0.0);

                        gatherLayerData.setRelicDrop(relicId, rate);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-Gather]階層「" + key + "」の採集遺物「" + relicIdStr + "」の取得に失敗しました");
                    }
                }

                // monster の中は MobName : 確率（小数）
                for(String monsterId : monsterSection.getKeys(false)) {
                    try{
                        float rate = (float) loadOrDefault("Gather", monsterSection, monsterId, 0.0);

                        gatherLayerData.setMonster(monsterId, rate);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-monster]階層「" + key + "」の原生生物「" + monsterId + "」の取得に失敗しました");
                    }
                }

                // fishMonster の中も monster と同様 MobName : 確率（小数）
                for(String monsterId : fishMonsterSection.getKeys(false)) {
                    try{
                        float rate = (float) loadOrDefault("Gather", fishMonsterSection, monsterId, 0.0);

                        gatherLayerData.setFishMonster(monsterId, rate);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-monster]階層「" + key + "」の釣り生物「" + monsterId + "」の取得に失敗しました");
                    }
                }


                GatherMain.gatherLayerMap.put(key,gatherLayerData);
                Bukkit.getLogger().info("[DiveItem-Gather]階層「"+key+"」の採掘データを設定しました");
            }
        }
    }

    @Override
    public void save() {

    }
}
