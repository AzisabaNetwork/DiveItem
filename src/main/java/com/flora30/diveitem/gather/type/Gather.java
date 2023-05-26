package com.flora30.diveitem.gather.type;

import com.flora30.diveapin.ItemEntityObject;
import com.flora30.diveapin.ItemMain;
import com.flora30.diveapin.data.player.PlayerData;
import com.flora30.diveapin.data.player.PlayerDataObject;
import com.flora30.diveapin.event.HelpEvent;
import com.flora30.diveapin.event.HelpType;
import com.flora30.diveitem.gather.GatherMain;
import com.flora30.diveitem.item.ItemEntityMain;
import com.flora30.diveitem.item.ItemStackMain;
import com.flora30.divenew.data.GatherData;
import com.flora30.divenew.data.LayerObject;
import com.flora30.divenew.data.PointObject;
import com.flora30.divenew.data.item.ItemDataObject;
import com.flora30.divenew.data.item.ToolData;
import com.flora30.divenew.data.item.ToolType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

// 使うたびに new して捨てるため、速度が遅くなるかも
// 1 採集辺りに行うならそこまで影響しない・・・はず？
public abstract class Gather {
    // 採集するプレイヤー
    protected Player player;
    // 採集する場所
    protected Location location;
    // 採取するツール
    protected int toolId;

    // 採取に必要なデータ
    protected PlayerData playerData;
    protected ToolData toolData;
    protected GatherData gatherData;

    // 採取に使うスタミナ
    protected int staminaCost = 20;

    // 採取成功時の音（通常）
    protected Sound normalSound = Sound.BLOCK_STONE_BREAK;

    public static double firstRelicRate = 0.1;
    public static double firstMonsterRate = 0.1;

    /**
     * 採集システムを作成する
     */
    public Gather(Player player, Location location, int toolId){
        this.player = player;
        this.location = location;
        this.toolId = toolId;
    }
    /**
     * 採集システムを実行する
     */
    public void execute() {
        Bukkit.getPluginManager().callEvent(new HelpEvent(player, HelpType.Gather));
        ///////////////////////////////////
        // テンプレート処理
        ///////////////////////////////////

        //Bukkit.getLogger().info("Gather: 採掘開始");
        // 採取に必要な PlayerData がない
        if(PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId()) == null) return;
        // 採取に必要な ToolData がない
        if(ItemDataObject.INSTANCE.getItemDataMap().get(toolId) == null) return;
        // 採取に必要な GatherData がない
        if(LayerObject.INSTANCE.getGatherMap().get(LayerObject.INSTANCE.getLayerName(location)) == null) return;

        //Bukkit.getLogger().info("Gather: データOK");

        // 必要なデータを取得
        playerData = PlayerDataObject.INSTANCE.getPlayerDataMap().get(player.getUniqueId());
        toolData = ItemDataObject.INSTANCE.getItemDataMap().get(toolId).getToolData();
        gatherData = LayerObject.INSTANCE.getGatherMap().get(LayerObject.INSTANCE.getLayerName(location));

        // 深度判定
        if (LayerObject.INSTANCE.getDepth(location) > toolData.getMaxDepth()) {
            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_DEATH, 1, 1);
            player.sendMessage(ChatColor.RED + "道具が深度に耐えられないようだ・・・");
            return;
        }

        // スタミナを使う処理
        if(!checkStamina()) {
            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_DEATH, 1, 1);
            player.sendMessage(ChatColor.RED + "スタミナが不足している・・・");
            return;
        }
        //Bukkit.getLogger().info("Gather: スタミナOK");

        // 耐久値消費
        ItemStack item;
        if (ItemMain.INSTANCE.getItemId(player.getInventory().getItemInMainHand()) == toolId) {
            item = player.getInventory().getItemInMainHand();
        }
        else if (ItemMain.INSTANCE.getItemId(player.getInventory().getItemInOffHand()) == toolId) {
            item = player.getInventory().getItemInOffHand();
        }
        else{
            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_DEATH, 1, 1);
            player.sendMessage(ChatColor.RED + "採掘に失敗しました（運営に報告してください）");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && !meta.isUnbreakable()) {
            if (meta instanceof Damageable dMeta) {
                int damage = dMeta.getDamage();
                int maxDamage = item.getType().getMaxDurability();

                if (damage + 1 <= maxDamage) {
                    dMeta.setDamage(damage + 1);
                    item.setItemMeta(meta);
                }
                else {
                    player.playSound(player.getLocation(),Sound.ENTITY_ITEM_BREAK,1,1);
                    item.setAmount(0);
                }
            }
        }

        // 原生生物のスポーン判定
        if(!checkMonster()) return;
        //Bukkit.getLogger().info("Gather: 原生生物OK");

        // 採集時の演出
        gatherEffect();

        // ドロップ判定
        if(!checkDrop()) {
            //Bukkit.getLogger().info("Gather: ドロップ失敗");
            // 失敗したときの音
            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_DEATH, 1, 1);
            return;
        }
        drop();
    }

    /**
     * ドロップを実行する
     */
    protected void drop() {
        //Bukkit.getLogger().info("Gather: ドロップ成功");
        int dropId = GetDropId();

        // ドロップアイテムが存在しない時
        if(dropId == -1){
            Bukkit.getLogger().info("[DiveItem-Gather]ドロップアイテムがありません");
            return;
        }

        ItemEntityObject.INSTANCE.spawnItem(ItemMain.INSTANCE.getItem(dropId), location.add(0.5, 0.5, 0.5), player);
    }

    ///////////////////////////////////
    // テンプレートの内部処理
    ///////////////////////////////////

    /**
     * スタミナの消費を行う
     * @return 処理を続けて大丈夫？（スタミナの消費が行われれば True）
     */
    protected boolean checkStamina(){
        if(playerData.getCurrentST() < staminaCost) {
            return false;
        }
        playerData.setCurrentST(playerData.getCurrentST() - staminaCost);
        return true;
    }

    /**
     * 原生生物のスポーン判定を行う
     * @return 処理を続けて大丈夫？（スポーンされなければ True）
     */
    protected abstract boolean checkMonster();

    /**
     * 採集時の演出を行う
     */
    protected abstract void gatherEffect();

    /**
     * ドロップするかの判定を行う
     * @return ドロップするとき True
     */
    protected boolean checkDrop(){
        // ドロップ判定：ツール固有の確率
        double dropRate = toolData.getDropRate();

        // 0.0 ~ 1.0 の中で確率判定
        return Math.random() <= dropRate;
    }

    /**
     * 遺物アイテムがドロップするかの判定を行う
     * @return ドロップするとき True
     */
    protected boolean isRelic(){
        // 遺物ドロップ判定 : 初期 firstRelicRate
        double rate = firstRelicRate * PointObject.INSTANCE.getGatherRelicRate(playerData.getLevelData().getPointLuc());
        // 0.0 ~ 1.0 の中で確率判定
        return Math.random() <= rate;
    }

    /**
     * ドロップアイテムのIDを取得する
     */
    protected int GetDropId(){
        ToolType toolType = toolData.getToolType();

        if(!isRelic()){
            // 通常ドロップの演出
            player.playSound(player.getLocation(), normalSound,1,1);
            return gatherData.getNormalDropMap().get(toolType);
        }

        // 遺物が当たった演出
        player.playSound(player.getLocation(), Sound.ENTITY_BEE_STING, 1, 1);
        player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE,1,1);

        // どの遺物？
        double rateWhich = Math.random();
        double rateNow = 0;
        for(int relicId : gatherData.getRelicRateMap().keySet()){
            rateNow += gatherData.getRelicRateMap().get(relicId);
            if(rateWhich <= rateNow){
                return relicId;
            }
        }
        //Bukkit.getLogger().info("[DiveItem-Gather]遺物ドロップ判定に失敗しました");
        return gatherData.getNormalDropMap().get(toolType);
    }
}
