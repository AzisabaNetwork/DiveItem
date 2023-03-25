package com.flora30.diveitem.loot;

import com.flora30.diveapi.DiveAPI;
import com.flora30.diveapi.data.PlayerData;
import com.flora30.diveapi.data.player.LayerData;
import com.flora30.diveapi.plugins.CoreAPI;
import com.flora30.diveapi.plugins.QuestAPI;
import com.flora30.diveapi.plugins.RegionAPI;
import com.flora30.diveapi.tools.BlockLoc;
import com.flora30.diveapi.tools.Mathing;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.loot.gui.LootGUI;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.IntStream;

public class LootMain {
    //String = レイヤーID
    private static final Map<String, Loot>lootMap = new HashMap<>();
    private static final Map<String, Integer>amountMap = new HashMap<>();
    private static final List<LootLevel> lootLevelList = new ArrayList<>();

    //yml取得
    public static int particleCount = 10;
    public static double particleRange = 1.0;
    public static double particleDistance = 20;
    public static LootGood failedLoot = new LootGood();
    public static boolean fillAir = false;

    public static void registerChest(Player player){
        String layer = CoreAPI.getPlayerData(player.getUniqueId()).layerData.layer;
        Loot loot = lootMap.get(layer);
        if (loot == null){
            loot = new Loot();
            lootMap.put(layer,loot);
            amountMap.put(layer,3);
            Bukkit.getLogger().info("[DiveCore-Loot]新規登録 - "+layer);
        }
        Location location = player.getLocation();
        if (location.getBlock().getType() != Material.CHEST){
            player.sendMessage("チェストの上に立ってください");
            return;
        }
        if (location.getWorld() == null){
            return;
        }

        LootLocation lootLoc = new LootLocation();

        lootLoc.setWorld(location.getWorld().getName());
        lootLoc.setBlockX(location.getBlockX());
        lootLoc.setBlockY(location.getBlockY());
        lootLoc.setBlockZ(location.getBlockZ());

        loot.addLocation(lootLoc);

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(layer);
        player.sendMessage("ルートチェストを登録しました[layer = "+layer+"][ID - "+loot.getID(location)+"]");

        lootLoc.getLocation().getBlock().setType(Material.AIR);
        CoreAPI.getPlayerData(player.getUniqueId()).layerData.lootMap.put(loot.locationList.size()-1, getRandomLootLevel());
    }

    public static void registerChest(Player player, Location placed){
        String layer = CoreAPI.getPlayerData(player.getUniqueId()).layerData.layer;
        Loot loot = lootMap.get(layer);
        if (loot == null){
            loot = new Loot();
            lootMap.put(layer,loot);
            amountMap.put(layer,3);
            Bukkit.getLogger().info("[DiveCore-Loot]新規登録 - "+layer);
        }
        if (placed.getBlock().getType() != Material.CHEST){
            player.sendMessage("チェストの登録に失敗しました");
            return;
        }
        if (placed.getWorld() == null) return;

        LootLocation lootLoc = new LootLocation();

        lootLoc.setWorld(placed.getWorld().getName());
        lootLoc.setBlockX(placed.getBlockX());
        lootLoc.setBlockY(placed.getBlockY());
        lootLoc.setBlockZ(placed.getBlockZ());
        lootLoc.setFace(((Chest)placed.getBlock().getBlockData()).getFacing());

        loot.addLocation(lootLoc);

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(layer);
        player.sendMessage("ルートチェストを登録しました[layer = "+layer+"][ID - "+loot.getID(placed)+"]");

        lootLoc.getLocation().getBlock().setType(Material.AIR);
        CoreAPI.getPlayerData(player.getUniqueId()).layerData.lootMap.put(loot.locationList.size()-1, getRandomLootLevel());
    }

    public static void unregisterChest(Player player, Location location){
        String layer = CoreAPI.getPlayerData(player.getUniqueId()).layerData.layer;
        Loot loot = lootMap.get(layer);
        //Bukkit.getLogger().info("比較："+location.getX()+","+location.getY()+","+location.getZ());
        for (int i = 0; i < loot.getLocationAmount(); i++){
            Location loc = loot.getLootLoc(i).getLocation();
            //Bukkit.getLogger().info("対象"+i+": "+loc.getX()+","+loc.getY()+","+loc.getZ());
            if (loot.getLootLoc(i).check(location)){
                loot.removeLocation(loot.getLootLoc(i));

                LootConfig lootConfig = new LootConfig();
                lootConfig.save(layer);
                player.sendMessage("ルートチェストを削除しました[layer = "+layer);
                return;
            }
        }
        player.sendMessage("ルートチェストの削除に失敗しました[layer = "+ layer+"]");
    }

    public static void executeRandomSpawn(Player player, Location location){
        LayerData layerData = CoreAPI.getPlayerData(player.getUniqueId()).layerData;
        //送られた場所の階層を判定
        String layerName = RegionAPI.getLayer(RegionAPI.getLayerName(location)).groupName;
        Loot loot = getLoot(layerName);
        if (loot == null){
            Bukkit.getLogger().info("lootなし - "+layerName);
            layerData.lootMap = new HashMap<>();
            layerData.lootLayer = layerName;
            return;
        }
        if (loot.getLocationAmount() <= 0){
            Bukkit.getLogger().info("lootLocなし - "+layerName);
            layerData.lootMap = new HashMap<>();
            layerData.lootLayer = layerName;
            return;
        }

        //登録されていた場所のルートチェストを削除
        layerData.lootMap = new HashMap<>();

        layerData.lootLayer = layerName;

        // ランダムに座標取得/playerDataに登録
        // 生成されるルートチェストの数
        int amount = getAmount(layerName);
        // 階層Lootに登録されているルートチェストの上限数
        int max = loot.getLocationAmount();
        // 未生成のID（生成ごとに消えていく）
        List<Integer> unSpawnedList = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            unSpawnedList.add(i);
        }

        // 未生成の中から一つ選び、lootSetに追加、未生成から削除
        int countLv1 = 0;
        int countLv2 = 0;
        int countLv3 = 0;
        for(int i = 0; i < amount; i++){
            int j = Mathing.getRandomInt(unSpawnedList.size());
            int lootLevel = getRandomLootLevel();
            layerData.lootMap.put(unSpawnedList.get(j), getRandomLootLevel());
            unSpawnedList.remove(j);
            switch (lootLevel) {
                case 1 -> countLv1++;
                case 2 -> countLv2++;
                case 3 -> countLv3++;
            }
        }

        //プレイヤーにお知らせ
        //player.sendMessage( QuestAPI.getStory(layerName).displayName+"にチェストを配置しました");
        Bukkit.getLogger().info("("+player.getDisplayName()+","+RegionAPI.getLayerName(location)+") -> チェスト配置 (lv1 - "+countLv1+") " +
                        "(Lv2 - " + countLv2 + ") (Lv3 - " + countLv3 +")");
    }

    public static void openChest(Player player, Location location){
        //Bukkit.getLogger().info("[DiveItem-Loot]open判定");
        PlayerData playerData = CoreAPI.getPlayerData(player.getUniqueId());
        LayerData data = playerData.layerData;
        if (data.lootLayer == null){
            Bukkit.getLogger().info("[DiveItem-Loot]layer＝なし");
            return;
        } else if (getLoot(data.lootLayer) == null) {
            Bukkit.getLogger().info("[DiveItem-Loot]layer「"+data.lootLayer+"」にLootが設定されていません");
            return;
        }
        int id = getLoot(data.lootLayer).getID(location);
        //playerDataの座標に無い場合はエラー
        if(!playerData.layerData.lootMap.containsKey(id)){
            //音
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED,1,1);
            player.sendMessage("チェストの中は空のようだ・・・");
            return;
        }

        //座標を登録
        data.openLootLocID = (id);
        //Bukkit.getLogger().info(player.getDisplayName()+"のチェストを開く - "+id);
        //音
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN,1,1);

        //宝箱GUIを表示
        LootGUI.open(player, getLoot(data.lootLayer), playerData.layerData.lootMap.get(id));
    }

    public static void sendChest(Player player){
        PlayerData pData = CoreAPI.getPlayerData(player.getUniqueId());
        if (pData == null) return;

        LayerData data = pData.layerData;
        Loot loot = getLoot(data.lootLayer);
        //loot未設定
        if (loot == null){
            Bukkit.getLogger().info("[DiveCore-Loot]loot未設定 - "+data.lootLayer);
            return;
        }

        Location playerLoc = player.getLocation();
        //座標で回す
        for(int i : data.lootMap.keySet()){
            DiveItem.plugin.asyncTask(() -> sendChestAsync(player,data,loot,playerLoc,i));
        }
    }

    public static void sendChestAsync(Player player, LayerData data, Loot loot, Location playerLoc, int i){
        LootLocation lootLoc = loot.getLootLoc(i);
        if (lootLoc == null){
            Bukkit.getLogger().info(("[DiveCore-Loot]存在しないチェストID["+data.layer+" - "+i+"]を持っています - "+player.getDisplayName()));
            return;
        }
        if(lootLoc.getLocation().getWorld() != playerLoc.getWorld()) return;

        Location location = lootLoc.getLocation();
        LootLevel lootLevel;
        try{
            // lootが削除されたタイミングと被ったらNullPointerExceptionになる→削除後なのでチェストは送らない
            lootLevel = lootLevelList.get(data.lootMap.get(i) - 1);
        } catch (NullPointerException e) { return; }

        // 向きの設定
        BlockData blockData = lootLevel.material.createBlockData();
        if (blockData instanceof Directional directional) {
            directional.setFacing(lootLoc.face);
        }

        player.sendBlockChange(location, blockData);
        //Bukkit.getLogger().info("[DiveCore-Loot]チェストの設置を確認 - "+i);
        //近場にパーティクル
        if(location.distance(playerLoc) <= particleDistance){
            Location particleLoc = location.clone().add(0.5,0.5,0.5);
            player.spawnParticle(lootLevel.particle,particleLoc,particleCount,particleRange,particleRange,particleRange,0.05);
        }
    }

    public static void removeChest(Player player, int id){
        LayerData data = CoreAPI.getPlayerData(player.getUniqueId()).layerData;
        data.lootMap.remove(id);
        //Bukkit.getLogger().info(player.getDisplayName()+"のチェストを削除しました - "+id);
    }

    public static void closeChest(Player player, int id){
        //Bukkit.getLogger().info("loot close");
        LayerData data = CoreAPI.getPlayerData(player.getUniqueId()).layerData;
        Loot loot = getLoot(data.lootLayer);
        Location location = loot.getLootLoc(id).getLocation();
        double pRange = 1.0;
        //演出
        player.playSound(location,Sound.BLOCK_CHEST_CLOSE,1,1);
        DiveItem.plugin.delayedTask(10, () -> {
            player.playSound(location, Sound.BLOCK_STONE_BREAK,1,1);
            player.sendBlockChange(location, Material.AIR.createBlockData());
            player.spawnParticle(Particle.BLOCK_CRACK, location,10,pRange,pRange,pRange,Bukkit.createBlockData(Material.CHEST));
        });
        removeChest(player,id);

        //高さ-5以上のチェストを吹き飛ばす -- 一時削除
        /*
        for(int i : data.getLoots().keySet()){
            if(loot.getLootLoc(i).getBlockY() >= location.getBlockY() - 5){
                removeChest(player,i);
            }
        }
         */
    }

    public static void setLoot(String layer, Loot loot){
        lootMap.put(layer,loot);
    }

    public static void setAmount(String layer, int amount){
        amountMap.put(layer,amount);
    }

    public static void addLootLevel(LootLevel lootLevel){
        lootLevelList.add(lootLevel);
    }

    public static Loot getLoot(String layer){
        return lootMap.get(layer);
    }

    public static int getAmount(String layer){
        return amountMap.get(layer);
    }

    public static Set<String> getLayers(){
        return lootMap.keySet();
    }

    /**
     * @return レベル（1～3）
     */
    public static int getRandomLootLevel(){
        List<LootLevel> reverseList = new ArrayList<>(lootLevelList);
        Collections.reverse(reverseList);
        double currentRate = 0;
        int size = reverseList.size();
        for(int i = 0; i < size; i++){
            currentRate += reverseList.get(i).getPercent();
            if(Math.random() <= currentRate){
                //Bukkit.getLogger().info("lootLevel決定 - "+(reverseList.size()-i));
                return size-i;
            }
        }
        //Bukkit.getLogger().info("lootLevel決定 - 0");
        return 0;
    }

    public static LootLevel getLootLevel(int level){
        try{
            return lootLevelList.get(level-1);
        } catch (IndexOutOfBoundsException e){
            Bukkit.getLogger().info("[DiveCore-Loot]登録されたlootlevelの数を越えました(上限："+lootLevelList.size()+")(指定："+level+")");
            return null;
        }
    }

    /**
     * 起動時にルートチェストの座標にあるサーバー側のブロックをAirにする
     * チェストが無い場合にAirに見えるため、プレイヤーが通行できるようにする
     */
    public static void sendAllAir(){
        if (!fillAir) return;

        // 動作が重いので数回に分ける
        // Asyncでやると怒られた
        int count = 1;
        long time = System.currentTimeMillis();

        for (Loot loot : lootMap.values()){
            for (LootLocation loc : loot.locationList){
                if (loc.getLocation().getBlock().getType() != Material.AIR) {
                    loc.getLocation().getBlock().setType(Material.AIR);
                }
                count++;
                if (System.currentTimeMillis() > time + 1000L) {
                    Bukkit.getLogger().info("[DiveItem-Loot] ルートチェストの座標に空気を送っています...("+count+")");
                    time = System.currentTimeMillis();
                }
            }
        }
    }

    /**
     * プレイヤーが持っているチェストの座標か？
     */
    public static boolean isExistLootLocation(Player player, Location location) {
        PlayerData data = CoreAPI.getPlayerData(player.getUniqueId());
        if (data == null) return false;

        String layer = RegionAPI.getLayerName(location);
        Loot loot = LootMain.getLoot(layer);
        if (loot == null) return false;

        // Block座標を使うためBlockLocに変換
        BlockLoc blockLoc = new BlockLoc(location);

        // 原因不明のエラーを回避（最大IDより1多いIDが出る）
        try{
            // プレイヤーの持っているチェストを検索
            for (int id : data.layerData.lootMap.keySet()) {
                LootLocation lootLoc = loot.locationList.get(id);
                if (blockLoc.x == lootLoc.blockX &&
                        blockLoc.y == lootLoc.blockY &&
                        blockLoc.z == lootLoc.blockZ) return true;
            }
        } catch (IndexOutOfBoundsException e) {
            // 上記エラーでは調べるものがこれ以上ないのでfalse
            return false;
        }

        return false;
    }
}