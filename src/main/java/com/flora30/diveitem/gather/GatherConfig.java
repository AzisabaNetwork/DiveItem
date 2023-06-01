package com.flora30.diveitem.gather;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.flora30.divelib.util.Config;
import com.flora30.divelib.data.GatherData;
import com.flora30.divelib.data.LayerObject;
import com.flora30.divelib.data.item.ItemDataObject;
import com.flora30.divelib.data.item.ToolData;
import com.flora30.divelib.data.item.ToolType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GatherConfig extends Config {

    private static File[] areaFiles = new File[100];

    public GatherConfig(){
        areaFiles = LayerObject.INSTANCE.getLayerFile();
//        folderCheck(RegionAPI.region.getDataFolder().getAbsolutePath() + "/area");
//        areaFiles = new File(RegionAPI.region.getDataFolder().getAbsolutePath() + "/area").listFiles();
    }

    /**
     * @return 設定後のItemStackが返ってくるよ！
     */
    public static ItemStack setMineBlock(int itemId, ItemStack item) {
        // 必要な ItemData を取得
        if(ItemDataObject.INSTANCE.getItemDataMap().get(itemId) == null) return item;
        ToolData data = ItemDataObject.INSTANCE.getItemDataMap().get(itemId).getToolData();
        if (data == null) return item;
        if(data.getToolType() == ToolType.None) return item;

        // Material を リストに入れる
        List<String> materialList = new ArrayList<>();
        for(Material material : data.getBreakableMaterialSet()){
            //Bukkit.getLogger().info("Material.send = minecraft:" + material.toString().toLowerCase());
            materialList.add("minecraft:" + material.toString().toLowerCase());
        }
        // 上で作ったものを、壊せるブロックとして設定
        NbtList<String> nbtList = NbtFactory.ofList("CanDestroy",materialList);

        NbtWrapper<?> wrapper = NbtFactory.fromItemTag(item);
        NbtCompound compound = NbtFactory.asCompound(wrapper);
        compound.put(nbtList);

        NbtFactory.setItemTag(item,compound);
        Bukkit.getLogger().info("[DiveCore-GatherLS] アイテム " + itemId + " に破壊可能ブロックを設定しました");

        return item;
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
                GatherData gatherData = new GatherData();

                // normalDrop の中は ToolType : ItemID
                for(String toolTypeStr : normalDropSection.getKeys(false)) {
                    try{
                        ToolType type = ToolType.valueOf(toolTypeStr);
                        int dropId = loadOrDefault("Gather", normalDropSection, toolTypeStr, -1);

                        if(dropId == -1) { continue; }

                        gatherData.getNormalDropMap().put(type,dropId);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-Gather]階層「"+key+"」の採集通常ドロップ「"+toolTypeStr+"」の取得に失敗しました");
                    }
                }

                // relicRate の中は ItemId : 確率（小数）
                for(String relicIdStr : relicRateSection.getKeys(false)) {
                    try{
                        int relicId = Integer.parseInt(relicIdStr);
                        float rate = (float) loadOrDefault("Gather", relicRateSection, relicIdStr, 0.0);

                        gatherData.getRelicRateMap().put(relicId, rate);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-Gather]階層「" + key + "」の採集遺物「" + relicIdStr + "」の取得に失敗しました");
                    }
                }

                // monster の中は MobName : 確率（小数）
                for(String monsterId : monsterSection.getKeys(false)) {
                    try{
                        float rate = (float) loadOrDefault("Gather", monsterSection, monsterId, 0.0);

                        gatherData.getMonsterMap().put(monsterId, rate);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-monster]階層「" + key + "」の原生生物「" + monsterId + "」の取得に失敗しました");
                    }
                }

                // fishMonster の中も monster と同様 MobName : 確率（小数）
                for(String monsterId : fishMonsterSection.getKeys(false)) {
                    try{
                        float rate = (float) loadOrDefault("Gather", fishMonsterSection, monsterId, 0.0);

                        gatherData.getFishMonsterMap().put(monsterId, rate);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[DiveCore-monster]階層「" + key + "」の釣り生物「" + monsterId + "」の取得に失敗しました");
                    }
                }


                LayerObject.INSTANCE.getGatherMap().put(key,gatherData);
                Bukkit.getLogger().info("[DiveItem-Gather]階層「"+key+"」の採掘データを設定しました");
            }
        }
    }

    @Override
    public void save() {

    }
}
