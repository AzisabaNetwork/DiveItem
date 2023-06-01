package com.flora30.diveitem.craft;

import com.flora30.divelib.ItemMain;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divelib.data.recipe.HideType;
import com.flora30.divelib.data.recipe.Recipe;
import com.flora30.divelib.data.recipe.RecipeObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class CraftConfig{
    private static final File file = new File(DiveItem.plugin.getDataFolder(), File.separator + "recipe.yml");
    private static final String[] materialKeys = {"m1", "m2", "m3", "m4"};
    private static final String[] hideKeys = {"hide1", "hide2", "hide3", "hide4"};

    public static void load() {
        Bukkit.getLogger().info("[DiveItem-Craft]レシピの読み込みを開始します...");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            int id;
            try{
                id = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().info("[DiveItem-Craft]ID「"+key+"」は数値ではありません");
                continue;
            }
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            // 完成品が存在するかチェック
            ItemStack item = ItemMain.INSTANCE.getNeutralItem(id);
            if (item == null) {
                Bukkit.getLogger().info("[DiveItem-Craft]ID「"+key+"」のアイテムは存在しません");
                continue;
            }

            // 数を取得
            int amount = section.getInt("amount",1);

            int[] materials = new int[12];
            HideType[] hides = new HideType[12];

            boolean error = false;
            for (int i = 0; i < 4; i++) {
                // str = o,101,o
                String mKey = materialKeys[i];
                String str = section.getString(mKey);
                if (str == null) {
                    error = true;
                    break;
                }
                // array = [o, 101, o]
                String[] array = str.split(",");
                if (array.length != 3) {
                    error = true;
                    break;
                }
                // 素材IDを登録（何もないときはo）
                for (int j = 0; j < 3; j++) {
                    String mId = array[j];
                    if (mId.equals("o")) continue;
                    try{
                        materials[i*3 + j] = Integer.parseInt(mId);
                    } catch (NumberFormatException e) {
                        error = true;
                        break;
                    }
                }
            }

            if (error) {
                Bukkit.getLogger().info("[DiveItem-Craft]ID「"+key+"」の素材取得でエラーが発生しました");
                continue;
            }

            for (int i = 0; i < 4; i++) {
                // str = o,101,o
                String hKey = hideKeys[i];
                String str = section.getString(hKey);
                if (str == null) {
                    error = true;
                    break;
                }
                // array = [o, 101, o]
                String[] array = str.split(",");
                if (array.length != 3) {
                    error = true;
                    break;
                }
                // 素材IDを登録（何もないときはo）
                for (int j = 0; j < 3; j++) {
                    String h = array[j];
                    if (h.equals("o")) continue;
                    try{
                        hides[i*3 + j] = HideType.valueOf(h);
                    } catch (IllegalArgumentException e) {
                        error = true;
                        break;
                    }
                }
            }

            if (error) {
                Bukkit.getLogger().info("[DiveItem-Craft]ID「"+key+"」の隠れ取得でエラーが発生しました");
                continue;
            }

            // レシピを作成
            Recipe recipe = new Recipe(materials,hides,amount);
            RecipeObject.INSTANCE.getRecipeMap().put(id,recipe);
            Bukkit.getLogger().info("[DiveItem-Craft]レシピ "+id+" を登録");
        }

        Bukkit.getLogger().info("[DiveItem-Craft]レシピの読み込みが完了しました");
    }

    public static void save(int id) {
        Recipe recipe = RecipeObject.INSTANCE.getRecipeMap().get(id);
        if (recipe == null) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection(String.valueOf(id));
        if (section == null) {
            section = config.createSection(String.valueOf(id));
        }

        // 名前（メモ用）
        ItemStack item = ItemMain.INSTANCE.getNeutralItem(id);
        if (item == null || item.getItemMeta() == null) return;
        String name = item.getItemMeta().getDisplayName();
        section.set("name(Option)",name);
        section.set("amount",recipe.getAmount());

        // Material
        for (int i = 0; i < 4; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 3; j++) {
                if (j != 0) {
                    sb.append(",");
                }

                String str = String.valueOf(recipe.getMaterials()[i*3 + j]);
                if (str.equals("0")) {
                    str = "o";
                }
                sb.append(str);
            }
            section.set(materialKeys[i], sb.toString());
        }

        // hide
        for (int i = 0; i < 4; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 3; j++) {
                if (j != 0) {
                    sb.append(",");
                }
                String str = String.valueOf(recipe.getHides()[i*3 + j]);
                if (str.equals("null")) {
                    str = "o";
                }
                sb.append(str);
            }
            section.set(hideKeys[i], sb.toString());
        }

        try{
            config.save(file);
        } catch (IOException e){
            Bukkit.getLogger().info("[DiveItem-Craft]レシピ"+id+"（"+name+"）の保存に失敗しました");
            e.printStackTrace();
            return;
        }

        Bukkit.getLogger().info("[DiveItem-Craft]レシピ"+id+"（"+name+"）を保存しました");
    }
}
