package com.flora30.diveitem.loot;

import com.flora30.divelib.BlockLoc;
import com.flora30.divelib.data.player.LayerData;
import com.flora30.divelib.data.player.PlayerData;
import com.flora30.divelib.data.player.PlayerDataObject;
import com.flora30.divelib.util.Mathing;
import com.flora30.diveitem.DiveItem;
import com.flora30.diveitem.loot.gui.LootGUI;
import com.flora30.diveconstant.data.LayerObject;
import com.flora30.diveconstant.data.loot.Loot;
import com.flora30.diveconstant.data.loot.LootLevel;
import com.flora30.diveconstant.data.loot.LootLocation;
import com.flora30.diveconstant.data.loot.LootObject;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.IntStream;

public class LootMain {

    public static void registerChest(Player player){
        String layer = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData().getLayer();
        Loot loot = LootObject.INSTANCE.getLootMap().get(layer);
        if (loot == null){
            loot = new Loot();
            LootObject.INSTANCE.getLootMap().put(layer,loot);
            LootObject.INSTANCE.getAmountMap().put(layer,3);
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

        LootLocation lootLoc = new LootLocation(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                BlockFace.NORTH,
                false
        );

        loot.getLocationList().add(lootLoc);

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(layer);
        player.sendMessage("ルートチェストを登録しました[layer = "+layer+"][ID - "+loot.getID(location)+"]");

        lootLoc.getLocation().getBlock().setType(Material.AIR);
        PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData().getLootMap().put(loot.getLocationList().size()-1,getRandomLootLevel());
    }

    public static void registerChest(Player player, Location placed){
        String layer = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData().getLayer();
        Loot loot = LootObject.INSTANCE.getLootMap().get(layer);
        if (loot == null){
            loot = new Loot();
            LootObject.INSTANCE.getLootMap().put(layer,loot);
            LootObject.INSTANCE.getAmountMap().put(layer,3);
            Bukkit.getLogger().info("[DiveCore-Loot]新規登録 - "+layer);
        }
        if (placed.getBlock().getType() != Material.CHEST){
            player.sendMessage("チェストの登録に失敗しました");
            return;
        }
        if (placed.getWorld() == null) return;

        LootLocation lootLoc = new LootLocation(
                placed.getWorld().getName(),
                placed.getBlockX(),
                placed.getBlockY(),
                placed.getBlockZ(),
                ((Chest)placed.getBlock().getBlockData()).getFacing(),
                false
        );

        loot.getLocationList().add(lootLoc);

        LootConfig lootConfig = new LootConfig();
        lootConfig.save(layer);
        player.sendMessage("ルートチェストを登録しました[layer = "+layer+"][ID - "+loot.getID(placed)+"]");

        lootLoc.getLocation().getBlock().setType(Material.AIR);
        PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData().getLootMap().put(loot.getLocationList().size()-1,getRandomLootLevel());
    }

    public static void unregisterChest(Player player, Location location){
        String layer = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData().getLayer();
        Loot loot = LootObject.INSTANCE.getLootMap().get(layer);
        //Bukkit.getLogger().info("比較："+location.getX()+","+location.getY()+","+location.getZ());
        for (int i = 0; i < loot.getLocationList().size(); i++){
            Location loc = loot.getLootLoc(i).getLocation();
            //Bukkit.getLogger().info("対象"+i+": "+loc.getX()+","+loc.getY()+","+loc.getZ());
            if (loot.getLootLoc(i).check(location)){
                loot.getLocationList().remove(loot.getLootLoc(i));

                LootConfig lootConfig = new LootConfig();
                lootConfig.save(layer);
                player.sendMessage("ルートチェストを削除しました[layer = "+layer);
                return;
            }
        }
        player.sendMessage("ルートチェストの削除に失敗しました[layer = "+ layer+"]");
    }

    public static void executeRandomSpawn(Player player, Location location){
        LayerData layerData = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData();
        //送られた場所の階層を判定
        String layerName = LayerObject.INSTANCE.getLayerMap().get(LayerObject.INSTANCE.getLayerName(location)).getGroupName();
        Loot loot = LootObject.INSTANCE.getLootMap().get(layerName);
        if (loot == null){
            Bukkit.getLogger().info("lootなし - "+layerName);
            layerData.getLootMap().clear();
            layerData.setLootLayer(layerName);
            return;
        }
        if (loot.getLocationList().size() == 0){
            Bukkit.getLogger().info("lootLocなし - "+layerName);
            layerData.getLootMap().clear();
            layerData.setLootLayer(layerName);
            return;
        }

        //登録されていた場所のルートチェストを削除
        layerData.getLootMap().clear();

        layerData.setLootLayer(layerName);

        // ランダムに座標取得/playerDataに登録
        // 生成されるルートチェストの数
        int amount = LootObject.INSTANCE.getAmountMap().get(layerName);
        // 階層Lootに登録されているルートチェストの上限数
        int max = loot.getLocationList().size();
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
            int j = Mathing.INSTANCE.getRandomInt(unSpawnedList.size());
            int lootLevel = getRandomLootLevel();
            layerData.getLootMap().put(unSpawnedList.get(j), getRandomLootLevel());
            unSpawnedList.remove(j);
            switch (lootLevel) {
                case 1 -> countLv1++;
                case 2 -> countLv2++;
                case 3 -> countLv3++;
            }
        }

        //プレイヤーにお知らせ
        //player.sendMessage( QuestAPI.getStory(layerName).displayName+"にチェストを配置しました");
        Bukkit.getLogger().info("("+player.getDisplayName()+","+LayerObject.INSTANCE.getLayerName(location)+") -> チェスト配置 (lv1 - "+countLv1+") " +
                        "(Lv2 - " + countLv2 + ") (Lv3 - " + countLv3 +")");
    }

    public static void openChest(Player player, Location location){
        //Bukkit.getLogger().info("[DiveItem-Loot]open判定");
        PlayerData playerData = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        LayerData data = playerData.getLayerData();
        if (data.getLootLayer() == null){
            Bukkit.getLogger().info("[DiveItem-Loot]layer＝なし");
            return;
        } else if (LootObject.INSTANCE.getLootMap().get(data.getLootLayer()) == null) {
            Bukkit.getLogger().info("[DiveItem-Loot]layer「"+data.getLootLayer()+"」にLootが設定されていません");
            return;
        }
        int id = LootObject.INSTANCE.getLootMap().get(data.getLootLayer()).getID(location);
        //playerDataの座標に無い場合はエラー
        if(!data.getLootMap().containsKey(id)){
            //音
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED,1,1);
            player.sendMessage("チェストの中は空のようだ・・・");
            return;
        }

        //座標を登録
        data.setOpenLootLocID(id);
        //Bukkit.getLogger().info(player.getDisplayName()+"のチェストを開く - "+id);
        //音
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN,1,1);

        //宝箱GUIを表示
        LootGUI.open(player, LootObject.INSTANCE.getLootMap().get(data.getLootLayer()), data.getLootMap().get(id));
    }

    public static void sendChest(Player player){
        PlayerData pData = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if (pData == null) return;

        LayerData data = pData.getLayerData();
        Loot loot = LootObject.INSTANCE.getLootMap().get(data.getLootLayer());
        //loot未設定
        if (loot == null){
            Bukkit.getLogger().info("[DiveCore-Loot]loot未設定 - "+data.getLootLayer());
            return;
        }

        Location playerLoc = player.getLocation();
        //座標で回す
        for(int i : data.getLootMap().keySet()){
            DiveItem.plugin.asyncTask(() -> sendChestAsync(player,data,loot,playerLoc,i));
        }
    }

    public static void sendChestAsync(Player player, LayerData data, Loot loot, Location playerLoc, int i){
        LootLocation lootLoc = loot.getLootLoc(i);
        if (lootLoc == null){
            Bukkit.getLogger().info(("[DiveCore-Loot]存在しないチェストID["+data.getLayer()+" - "+i+"]を持っています - "+player.getDisplayName()));
            return;
        }
        if(lootLoc.getLocation().getWorld() != playerLoc.getWorld()) return;

        Location location = lootLoc.getLocation();
        LootLevel lootLevel;
        try{
            // lootが削除されたタイミングと被ったらNullPointerExceptionになる→削除後なのでチェストは送らない
            lootLevel = LootObject.INSTANCE.getLootLevelList().get(data.getLootMap().get(i) - 1);
        } catch (NullPointerException e) { return; }

        // 向きの設定
        BlockData blockData = lootLevel.getMaterial().createBlockData();
        if (blockData instanceof Directional directional) {
            directional.setFacing(lootLoc.getFace());
        }

        player.sendBlockChange(location, blockData);
        //Bukkit.getLogger().info("[DiveCore-Loot]チェストの設置を確認 - "+i);
        //近場にパーティクル
        if(location.distance(playerLoc) <= LootObject.INSTANCE.getParticleDistance()){
            Location particleLoc = location.clone().add(0.5,0.5,0.5);
            player.spawnParticle(lootLevel.getParticle(),particleLoc,LootObject.INSTANCE.getParticleCount(),LootObject.INSTANCE.getParticleRange(),LootObject.INSTANCE.getParticleRange(),LootObject.INSTANCE.getParticleRange(),0.05);
        }
    }

    public static void removeChest(Player player, int id){
        LayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData();
        data.getLootMap().remove(id);
        //Bukkit.getLogger().info(player.getDisplayName()+"のチェストを削除しました - "+id);
    }

    public static void closeChest(Player player, int id){
        //Bukkit.getLogger().info("loot close");
        LayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()).getLayerData();
        Loot loot = LootObject.INSTANCE.getLootMap().get(data.getLootLayer());
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

    /**
     * @return レベル（1～3）
     */
    public static int getRandomLootLevel(){
        List<LootLevel> reverseList = new ArrayList<>(LootObject.INSTANCE.getLootLevelList());
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

    /**
     * 起動時にルートチェストの座標にあるサーバー側のブロックをAirにする
     * チェストが無い場合にAirに見えるため、プレイヤーが通行できるようにする
     */
    public static void sendAllAir(){
        if (!LootObject.INSTANCE.getFillAir()) return;

        // 動作が重いので数回に分ける
        // Asyncでやると怒られた
        int count = 1;
        long time = System.currentTimeMillis();

        for (Loot loot : LootObject.INSTANCE.getLootMap().values()){
            for (LootLocation loc : loot.getLocationList()){
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
        PlayerData data = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        if (data == null) return false;

        String layer = LayerObject.INSTANCE.getLayerName(location);
        Loot loot = LootObject.INSTANCE.getLootMap().get(layer);
        if (loot == null) return false;

        // Block座標を使うためBlockLocに変換
        BlockLoc blockLoc = new BlockLoc(location);

        // 原因不明のエラーを回避（最大IDより1多いIDが出る）
        try{
            // プレイヤーの持っているチェストを検索
            for (int id : data.getLayerData().getLootMap().keySet()) {
                LootLocation lootLoc = loot.getLocationList().get(id);
                if (blockLoc.getX() == lootLoc.getBlockX() &&
                        blockLoc.getY() == lootLoc.getBlockY() &&
                        blockLoc.getZ() == lootLoc.getBlockZ()) return true;
            }
        } catch (IndexOutOfBoundsException e) {
            // 上記エラーでは調べるものがこれ以上ないのでfalse
            return false;
        }

        return false;
    }
}