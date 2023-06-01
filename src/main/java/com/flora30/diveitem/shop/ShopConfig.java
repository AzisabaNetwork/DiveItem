package com.flora30.diveitem.shop;

import com.flora30.divelib.util.Config;
import com.flora30.diveitem.DiveItem;
import com.flora30.divelib.data.ShopItem;
import com.flora30.divelib.data.ShopObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ShopConfig extends Config {
    private static File[] shopFiles = new File[100];

    public ShopConfig() {
        folderCheck(DiveItem.plugin.getDataFolder().getAbsolutePath() + "/shop");
        shopFiles = new File(DiveItem.plugin.getDataFolder().getAbsolutePath() + "/shop").listFiles();
    }

    @Override
    public void load() {

        //フォルダ内のファイルを検索
        for (File separated : shopFiles) {
            FileConfiguration file2 = YamlConfiguration.loadConfiguration(separated);
            for (String key : file2.getKeys(false)) {
                int shopID;
                try {
                    shopID = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().info("[DiveCore-Shop]「" + key + "」は数字ではありません");
                    continue;
                }
                ShopObject.INSTANCE.getShopMap().put(shopID, getShopItemList(file2.getStringList(key)));
                Bukkit.getLogger().info("[DiveCore-Shop]「" + shopID + "」をロードしました");
            }
        }
        Bukkit.getLogger().info("[DiveCore-Shop]ショップのロードが完了しました");
    }

    @Override
    public void save() {

    }

    private List<ShopItem> getShopItemList(List<String> list){
        List<ShopItem> shopItemList = new ArrayList<>();
        if (list == null){
            return shopItemList;
        }
        for (String str : list){
            String[] s = str.split(",");
            try{
                ShopItem shopItem = new ShopItem(
                        Integer.parseInt(s[0]),
                        Integer.parseInt(s[1]),
                        Integer.parseInt(s[2])
                );
                shopItemList.add(shopItem);
            } catch (NumberFormatException ignored){}
        }
        return shopItemList;
    }
}