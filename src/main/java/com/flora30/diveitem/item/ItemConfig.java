package com.flora30.diveitem.item;

import com.flora30.divelib.ItemMain;
import com.flora30.divelib.util.Config;
import com.flora30.diveitem.DiveItem;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.io.MythicConfig;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class ItemConfig extends Config {
    private static File[] files = new File[100];
    public ItemConfig(){
        folderCheck(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/itemdata");
        files = new File(DiveItem.plugin.getDataFolder().getAbsolutePath() +"/itemdata").listFiles();
    }

    @Override
    public void load() {
        //アイテム数記録用
        int count = 0;
        //ファイルでループ
        Bukkit.getLogger().info("[DiveCore-Item]アイテムの読み込みを開始します...");

        //MythicMobs本体からロード
        for(MythicItem item : MythicMobs.inst().getItemManager().getItems()) {
            if (!item.getConfig().isConfigurationSection("Dive")) continue;
            MythicConfig conf = item.getConfig().getNestedConfig("Dive");


            //読み込み
            int id = conf.getInteger("Id");
            String name = item.getInternalName();
            ItemMain.INSTANCE.getMythicItemMap().put(id, name);
            Bukkit.getLogger().info(id + " : " + name);
            loadLore(id);

            count++;
        }
        /*
        for(File from : files) {
            FileConfiguration file = YamlConfiguration.loadConfiguration(from);
            //中にあるアイテムIDでループ
            for (String i : file.getKeys(false)) {
                //IDの単位が違ったら除外
                int id;
                try {
                    id = Integer.parseInt(i);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().info("[DiveCore-Item]ID-" + i + "の取得に失敗しました");
                    continue;
                }
                if (file.isSet(i + ".mythicName")) {
                    String name = file.getString(i + ".mythicName");
                    ItemStackMain.putMythicItem(id, name);
                    loadLore(id);
                } else if (file.isItemStack(i + ".item")) {
                    ItemStack item = file.getItemStack(i + ".item");
                    ItemStackMain.putItem(id, item);
                    loadLore(id);
                } else {
                    Bukkit.getLogger().info("[DiveCore-Item]ID-" + i + "の取得に失敗しました");
                    continue;
                }
                count++;
            }
        }
         */
        Bukkit.getLogger().info("[DiveCore-Item]アイテムの読み込みを完了しました(数："+count+")");
    }

    private void loadLore(int id){
    }

    public void save(int id) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(files[0]);
        //なければ新規作成
        String strId = String.valueOf(id);
        if(config.isConfigurationSection(strId)){
            config.createSection(strId);
        }
        ConfigurationSection section = config.getConfigurationSection(strId);
        assert section != null;

        String mythicName = ItemMain.INSTANCE.getMythicItemMap().get(id);
        if(mythicName != null){
            //既存のitemがあれば削除
            if(config.isConfigurationSection(id+".item")){
                config.set(id+".item",null);
            }
            checkAndWrite(section,"mythicName",mythicName);
        }
        else{
            ItemStack item = ItemMain.INSTANCE.getNeutralItem(id);
            if(item != null){
                //既存のmythicNameがあれば削除
                if(config.isConfigurationSection(id+".mythicName")){
                    config.set(id+".mythicName",null);
                }
                checkAndWrite(section,"item",item);
            }
            else{
                Bukkit.getLogger().info("[DiveCore-Item]id-"+id+"の保存に失敗しました");
                return;
            }
        }



        try {
            config.save(files[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        Bukkit.getLogger().info("[DiveCore-Item] セーブでは ID を指定してください（内部ミス）");
    }
}
