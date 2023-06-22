package com.flora30.diveitem.loot;

import com.flora30.divelib.data.Rarity;
import com.flora30.divelib.data.gimmick.action.ChestType;
import com.flora30.divelib.util.Config;
import com.flora30.diveitem.DiveItem;
import com.flora30.divelib.data.item.ItemDataObject;
import com.flora30.divelib.data.loot.LootLevel;
import com.flora30.divelib.data.loot.LootLocation;
import com.flora30.divelib.data.loot.LootObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LootConfig extends Config {
    private static File file;
    private static File[] lootFiles = new File[100];
    public LootConfig(){
        file = new File(DiveItem.plugin.getDataFolder(),File.separator+"loot.yml");

        folderCheck(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/loot");
        lootFiles = new File(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/loot").listFiles();
    }

    @Override
    public void load(){
        Bukkit.getLogger().info("[DiveItem-Loot]ルートチェストの読み込みを開始します...");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        //loot.ymlの読み込み
        int pDistance = loadOrDefault("loot",config,"particleDistance",20);
        int pCount = loadOrDefault("loot",config,"particleCount",10);
        double pRange = loadOrDefault("loot",config,"particleRange",1.0);
        boolean fillAir = config.getBoolean("fillAir",false);

        LootObject.INSTANCE.setParticleDistance(pDistance);
        LootObject.INSTANCE.setParticleCount(pCount);
        LootObject.INSTANCE.setParticleRange(pRange);
        LootObject.INSTANCE.setFillAir(fillAir);

        if(config.getConfigurationSection("lootlevel") == null){
            config.createSection("lootlevel");
        }
        LootObject.INSTANCE.getLootLevelList().add(new LootLevel());
        Bukkit.getLogger().info("LootLevel size -> "+LootObject.INSTANCE.getLootLevelList().size());
        for(String key : Objects.requireNonNull(config.getConfigurationSection("lootlevel")).getKeys(false)){
            String titlePlus = loadOrDefault("loot",config,"lootlevel."+key+".titlePlus","Lv.0");
            int chestSlot = loadOrDefault("loot", config,"lootlevel."+key+".chestSlot",9);
            double percent = loadOrDefault("loot", config,"lootlevel."+key+".percent",0.1);
            Particle pType = Particle.valueOf(config.getString("lootlevel."+key+".particleType","END_ROD"));
            Material material = Material.valueOf(config.getString("lootlevel."+key+".material","CHEST"));

            LootLevel lootLevel = new LootLevel(
                    titlePlus,
                    chestSlot,
                    percent,
                    pType
            );
            LootObject.INSTANCE.getLootLevelList().add(lootLevel);
            Bukkit.getLogger().info("LootLevel size -> "+LootObject.INSTANCE.getLootLevelList().size());
        }

        //失敗時アイテム
        int fAmount = loadOrDefault("loot",config,"failed.amount",1);
        int fID = loadOrDefault("loot",config,"failed.itemID",1);
        LootObject.ItemAmount failed = new LootObject.ItemAmount(fID,fAmount);
        LootObject.INSTANCE.setFailedLoot(failed);

        // レアリティに応じたドロップ率
        for (Rarity rarity: Rarity.values()){
            ItemDataObject.INSTANCE.getDropRateMap().put(rarity,config.getDouble("rarity."+rarity.toString().toLowerCase(),1));
        }

        // ChestTypeごとの表示ブロック
        for (String typeStr : config.getConfigurationSection("chestType").getKeys(false)){
            try {
                ChestType type = ChestType.valueOf(typeStr);
                Material material = Material.valueOf(config.getString("chestType."+typeStr));
                LootObject.INSTANCE.getDisplayList().put(type,material);
            } catch (IllegalArgumentException ignored){}
        }


        Bukkit.getLogger().info("[DiveItem-Loot]ルートチェスト全体の設定を読み込みました");


        //lootフォルダ内のファイルを検索
        for(File separated : lootFiles) {
            FileConfiguration file2 = YamlConfiguration.loadConfiguration(separated);
            for (String lootID : file2.getKeys(false)) {
                /* layerの読み込みが遅いので判定不可
                if (!RegionAPI.isLayerID(layerID)) {
                    Bukkit.getLogger().info("[DiveItem-Loot]エリア「" + layerID + "」は存在しません");
                    continue;
                }
                 */
                //報酬の読み込み
                List<String> list = file2.getStringList(lootID);

                //報酬リストの新規作成
                ArrayList<LootObject.ItemAmount> itemList = new ArrayList<>();

                for (String str : list) {
                    String[] split = str.split(",");
                    int lootItemID, lootAmount;
                    try {
                        lootItemID = Integer.parseInt(split[0]);
                        lootAmount = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().info("[DiveItem-Loot]「" + lootID + "」の報酬「ID-" + split[0] + "」の読み込みに失敗しました");
                        continue;
                    }

                    //報酬の新規作成
                    itemList.add(new LootObject.ItemAmount(lootItemID, lootAmount));
                }

                //報酬リストとして設定
                LootObject.INSTANCE.getLootItemMap().put(lootID,itemList);
                Bukkit.getLogger().info("[DiveItem-Loot]「" + lootID + "」のルートチェストを読み込みました");
            }
        }
        Bukkit.getLogger().info("[DiveItem-Loot]ルートチェストの読み込みが完了しました");
    }


    private LootLocation readLootLoc(String line){
        String[] split = line.split(",");
        int x,y,z;
        BlockFace face;
        try{
            x=Integer.parseInt(split[1]);
            y=Integer.parseInt(split[2]);
            z=Integer.parseInt(split[3]);
            face=BlockFace.valueOf(split[4]);
        }catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e){
            Bukkit.getLogger().info("[DiveItem-Loot]line["+line+"]の読み取りに失敗しました");
            return null;
        }

        return new LootLocation(
                split[0],
                x,
                y,
                z,
                face,
                false
        );
    }


    public void save(String lootID){
        //既存にあればそこに保存
        for(File separated : lootFiles) {
            FileConfiguration file2 = YamlConfiguration.loadConfiguration(separated);
            if (lootID.equals(file2.getKeys(false).toString())) {
                //一致したとき
                save(lootID,YamlConfiguration.loadConfiguration(separated), separated);
                Bukkit.getLogger().info("[DiveItem-Loot]「" + lootID + "」を保存しました");
                return;
            }
        }
        //無ければファイルを新規作成（layerIDが名前のyml）
        File file = new File(getMyFolder().getAbsolutePath() + File.separator + "loot","loot_"+lootID+".yml");
        save(lootID,YamlConfiguration.loadConfiguration(file), file);
        Bukkit.getLogger().info("[DiveItem-Loot]「" + lootID + "」を新規保存しました");
    }
    
    private File getMyFolder(){
        return DiveItem.plugin.getDataFolder();
    }

    @Override
    public void save() {

    }

    private void save(String lootID, FileConfiguration file, File saveTo){
        List<LootObject.ItemAmount> lootList = LootObject.INSTANCE.getLootItemMap().get(lootID);
        if (!file.isConfigurationSection(lootID)) {
            file.createSection(lootID);
        }
        ConfigurationSection sec1 = file.getConfigurationSection(lootID);
        assert sec1 != null;

        //報酬リストを作成
        List<String> list = new ArrayList<>();
        for(LootObject.ItemAmount ia : lootList) {
            String goodString = ia.getItemId() + "," + ia.getAmount();
            list.add(goodString);
        }
        checkAndWrite(sec1, "loot."+lootID, list);

        try{
            file.save(saveTo);
        } catch (IOException e){
            Bukkit.getLogger().info("[DiveItem-Loot]「"+lootID+"」のルートチェストの保存に失敗しました");
            e.printStackTrace();
            return;
        }

        Bukkit.getLogger().info("[DiveItem-Loot]「"+lootID+"」のルートチェストを保存しました");
    }

    private String composeLootLoc(LootLocation loc){
        return loc.getWorld()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+loc.getFace();
    }
}
