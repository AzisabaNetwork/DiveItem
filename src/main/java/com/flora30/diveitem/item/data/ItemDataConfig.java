package com.flora30.diveitem.item.data;

import com.flora30.divelib.data.Rarity;
import com.flora30.divelib.util.Config;
import com.flora30.diveitem.DiveItem;
import com.flora30.divelib.data.item.*;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.io.MythicConfig;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDataConfig extends Config {
    private static File[] files = new File[100];
    public ItemDataConfig(){
        folderCheck(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/itemdata");
        files = new File(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/itemdata").listFiles();
    }

    @Override
    public void load() {
        //アイテム数記録用
        int count = 0;

        Bukkit.getLogger().info("[DiveItem-Data]アイテム情報の読み込みを開始します...");

        //MythicMobs本体からロード
        for(MythicItem item : MythicMobs.inst().getItemManager().getItems()) {
            if(!item.getConfig().isConfigurationSection("Dive")) continue;
            MythicConfig conf = item.getConfig().getNestedConfig("Dive");


            //読み込み
            int id = conf.getInteger("Id");
            String area = conf.getString("Area");
            int damage = conf.getInteger("Damage",0);
            double sell = conf.getDouble("Sell",0.0);
            int level = conf.getInteger("Level",1);
            int food = conf.getInteger("Food",0);
            int exp = conf.getInteger("Exp",0);

            String strType = conf.getString("Type");
            ItemType type;
            try{
                type = ItemType.valueOf(strType);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().info("[DiveItem-Data]ItemType「"+strType+"」は存在しません（暫定Other）");
                type = ItemType.Other;
            }

            String str = conf.getString("Rarity","Normal");
            Rarity rarity;
            try{
                rarity = Rarity.valueOf(str);
            } catch (IllegalArgumentException e){
                Bukkit.getLogger().info("[DiveItem-Data]レアリティ「"+str+"」は存在しません（暫定Normal）");
                rarity = Rarity.Normal;
            }

            List<String> text = null;
            if (conf.isList("Text")) {
                text = conf.getStringList("Text");
                for (int j = 0; j < text.size(); j++) {
                    text.set(j, ChatColor.translateAlternateColorCodes('&', text.get(j)));
                }
            }


            //itemDataを作成
            ItemData itemData = new ItemData(
                    type,
                    area,
                    text,
                    rarity,
                    sell,
                    level,
                    food,
                    damage,
                    exp
            );

            // gatherData関連
            if(conf.isSet("Gather")){

                // 破壊可能なブロック
                Set<Material> materialSet = new HashSet<>();
                if (conf.isList("Gather.Material")){
                    List<String> materialList = conf.getStringList("Gather.Material");
                    for (String materialID : materialList){
                        try{
                            materialSet.add(Material.valueOf(materialID));
                        } catch (IllegalArgumentException ignored){}
                    }
                }

                try{
                    itemData.setToolData(new ToolData(
                            ToolType.valueOf(conf.getString("Gather.Type")),
                            (float) conf.getDouble("Gather.DropRate", 1.0),
                            conf.getInteger("Gather.MaxDepth", 100),
                            materialSet
                    ));
                } catch (IllegalArgumentException e){
                    Bukkit.getLogger().info("[DiveItem-Data]ID-"+id+"のToolTypeの取得に失敗しました");
                }
            }
            // Rope関連
            if(conf.isSet("Rope")) {
                itemData.setRopeData(new RopeData(
                        conf.getInteger("Rope.Length",5),
                        conf.getBoolean("Rope.IsUpper",false)
                ));
            }
            // Artifact関連
            if (conf.isSet("Artifact")) {
                itemData.setArtifactData(new ArtifactData(
                        conf.getInteger("Artifact.Value", 1)
                ));
            }
            // Cook関連
            if (conf.isSet("Cook")) {
                int cookTo = conf.getInteger("cook",0);
                if (cookTo != 0) {
                    itemData.setCookData(new CookData(
                            conf.getInteger("Cook",0)
                    ));
                } else {
                    Bukkit.getLogger().info("[DiveItem-Data]ID-"+id+"の料理先IDの取得に失敗しました");
                }
            }

            ItemDataObject.INSTANCE.getItemDataMap().put(id,itemData);

            count++;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /*
        //ファイルでループ
        for(File from : files){
            FileConfiguration file = YamlConfiguration.loadConfiguration(from);
            //中にあるアイテムIDでループ
            for(String i : file.getKeys(false)){
                //IDの単位が違ったら除外
                int id;
                try {
                    id = Integer.parseInt(i);
                } catch(NumberFormatException e){
                    Bukkit.getLogger().info("[DiveItem-Data]ID-"+i+"の取得に失敗しました");
                    continue;
                }
                if(!file.isSet(i+".area") || !file.isSet(i+".type")){
                    Bukkit.getLogger().info("[DiveItem-Data]ID-"+i+"の取得に失敗しました");
                    continue;
                }
                //読み込み
                String area = file.getString(i+".area");
                String type = file.getString(i+".type");
                int damage = loadOrDefault("Item",file,i+".damage",0);
                double sell = loadOrDefault("Item",file,i+".sell",0.0);
                int maxSize = loadOrDefault("Item",file,i+".maxSize",3000);
                int expPoint = loadOrDefault("Item",file,i+".expPoint",0);
                int level = loadOrDefault("Item",file,i+".level",1);
                int food = loadOrDefault("Item",file,i+".food",0);

                String str = loadOrDefault("Item",file,i+".rarity","Normal");
                RarityType rarity;
                try{
                    rarity = RarityType.valueOf(str);
                } catch (IllegalArgumentException e){
                    Bukkit.getLogger().info("[DiveItem-Data]レアリティ「"+str+"」は存在しません（暫定Normal）");
                    rarity = RarityType.Normal;
                }

                List<String> text = null;
                if (file.isList(i+".text")){
                    text = file.getStringList(i+".text");
                    for (int j = 0; j < text.size(); j++){
                        text.set(j, ChatColor.translateAlternateColorCodes('&',text.get(j)));
                    }
                }
                //itemDataを作成
                ItemData itemData = new ItemData();

                itemData.setArea(area);
                itemData.setType(type);
                itemData.setDamage(damage);
                itemData.setSellMoney(sell);
                itemData.setMaxSize(maxSize);
                itemData.setExpPoint(expPoint);
                itemData.setLevel(level);
                itemData.setFood(food);
                itemData.setRarity(rarity);
                itemData.setText(text);


                // gatherData関連
                if(file.isSet(i+".gather")){
                    // typeの判定
                    try{
                        String toolTypeStr = file.getString(i+".gather.type");
                        itemData.getGatherData().setToolType(ToolType.valueOf(toolTypeStr));
                    } catch (IllegalArgumentException e){
                        Bukkit.getLogger().info("[DiveItem-Data]ID-"+i+"のToolTypeの取得に失敗しました");
                    }

                    // 破壊可能なブロック
                    if (file.isList(i+".gather.material")){
                        List<String> materialList = file.getStringList(i+".gather.material");
                        for (String materialID : materialList){
                            try{
                                itemData.getGatherData().addBreakAbleMaterial(Material.valueOf(materialID));
                            } catch (IllegalArgumentException ignored){}
                        }
                    }

                    // 他のデータ
                    itemData.getGatherData().dropRate = (float) loadOrDefault("Item",file,i+".gather.dropRate",1.0);
                    itemData.getGatherData().gatherSpeed = (float) loadOrDefault("Item",file,i+".gather.gatherSpeed",1.0);
                    itemData.getGatherData().maxDepth = loadOrDefault("Item",file,i+".gather.maxDepth", 100);
                }


                ItemDataMain.setItemData(id,itemData);

                count++;
            }
        }
         */
        Bukkit.getLogger().info("[DiveItem-Data]アイテム情報の読み込みを完了しました(アイテム数："+count+")");
    }

    @Override
    public void save() {

    }

    public void fileUpdate(int pId,ItemData data) {
        //既存があればそこに保存
        for(int i = 1; i < files.length; i++) {
            File f = files[i];
            FileConfiguration conf = YamlConfiguration.loadConfiguration(f);
            ConfigurationSection sec = conf.getConfigurationSection("");
            if(sec == null){
                continue;
            }
            //全体でID検索
            for(String text : conf.getKeys(false)){
                int id = Integer.parseInt(text);
                //IDが一致した時
                if(id==pId){
                    //ファイルに書き込みしてセーブ
                    save(id,sec,data);
                    try {
                        conf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }

        //既存が無ければ1に保存
        File f1 = new File(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/itemdata","item1.yml");
        FileConfiguration conf2 = YamlConfiguration.loadConfiguration(f1);
        ConfigurationSection sec2 = conf2.getConfigurationSection("");
        if (sec2 == null){
            conf2.createSection("");
        }
        assert sec2 != null;
        //ファイルに書き込みしてセーブ
        save(pId,sec2,data);
        try {
            conf2.save(f1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(int id, ConfigurationSection sec, ItemData data){
        //idが存在するか確認
        if(sec.contains(String.valueOf(id))){
            sec.createSection(String.valueOf(id));
        }
        ConfigurationSection sec2 = sec.getConfigurationSection(String.valueOf(id));
        assert sec2 != null;
        //各セクションが存在するか確認

        checkAndWrite(sec2, "area", data.getArea());
        checkAndWrite(sec2,"type",data.getType());
        checkAndWrite(sec2,"sell",data.getMoney());
        checkAndWrite(sec2,"level",data.getLevel());
        checkAndWrite(sec2,"food",data.getFood());
        checkAndWrite(sec2,"rarity",data.getRarity().toString());
    }
}
