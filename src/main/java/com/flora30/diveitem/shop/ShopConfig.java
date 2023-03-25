package com.flora30.diveitem.shop;

import com.flora30.diveapi.tools.Config;
import com.flora30.diveitem.DiveItem;
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

                Shop shop = new Shop();
                shop.setGoodsList(getShopGoodList(file2.getStringList(key)));

                ShopMain.shopMap.put(shopID,shop);
                Bukkit.getLogger().info("[DiveCore-Shop]「" + shopID + "」をロードしました");
            }
        }
        Bukkit.getLogger().info("[DiveCore-Shop]ショップのロードが完了しました");
    }

    @Override
    public void save() {

    }

    private List<ShopGood> getShopGoodList (List<String> list){
        List<ShopGood> shopGoodList = new ArrayList<>();
        if (list == null){
            return shopGoodList;
        }
        for (String str : list){
            String[] s = str.split(",");
            int id, amount,money;
            ShopGood good = new ShopGood();
            try{
                id = Integer.parseInt(s[0]);
                amount = Integer.parseInt(s[1]);
                money = Integer.parseInt(s[2]);

                good.setItemId(id);
                good.setAmount(amount);
                good.setMoney(money);
            } catch (NumberFormatException e){
                continue;
            }
            shopGoodList.add(good);
        }
        return shopGoodList;
    }
}