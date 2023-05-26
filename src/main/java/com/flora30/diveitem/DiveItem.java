package com.flora30.diveitem;

import com.comphenix.protocol.ProtocolLibrary;
import com.flora30.diveapin.DiveAPIN;
import com.flora30.diveapin.ItemEntityObject;
import com.flora30.diveapin.data.Rarity;
import com.flora30.diveitem.craft.CraftConfig;
import com.flora30.diveitem.item.data.ItemDataConfig;
import com.flora30.diveitem.gather.GatherConfig;
import com.flora30.diveitem.item.ItemConfig;
import com.flora30.diveitem.item.ItemEntityMain;
import com.flora30.diveitem.loot.LootConfig;
import com.flora30.diveitem.loot.LootMain;
import com.flora30.diveitem.mythic.MythicConfig;
import com.flora30.diveitem.mythic.MythicListener;
import com.flora30.diveitem.rope.RopeConfig;
import com.flora30.diveitem.shop.ShopConfig;
import com.flora30.diveitem.util.FlyFilter;
import com.flora30.diveitem.whistle.WhistleConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class DiveItem extends JavaPlugin {

    public static DiveItem plugin;
    public static PluginManager pluginManager;
    final AtomicInteger count = new AtomicInteger();
    final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    Listeners listeners = new Listeners();

    @Override
    public void onEnable() {
        // Plugin startup logic

        //変数 plugin にこの場所を設定
        plugin = this;
        pluginManager = plugin.getServer().getPluginManager();

        relateMythicMobs();


        getCommand("item").setExecutor(listeners);
        getCommand("loot").setExecutor(listeners);

        getServer().getPluginManager().registerEvents(listeners, this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new BlockChangeListener());

        // fly kickログを停止
        ((Logger) LogManager.getRootLogger()).addFilter(new FlyFilter());


        loadConfig();

        LootMain.sendAllAir();

        onTimer();

        tryCreateColorTeamMap();
    }
    private void onTimer(){
        int time = 1;
        if(count.intValue() == 0){
            Bukkit.getLogger().info("[DiveItem]Timer Started");
        }

        scheduler.scheduleSyncDelayedTask(this, () -> {
            count.getAndIncrement();
            onTimer();
            //ここでやりたいことを入れる
            listeners.onTimer();
        }, time);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ItemEntityMain.removeArmorstands();
    }

    public void relateMythicMobs(){
        if(pluginManager.isPluginEnabled("MythicMobs")){
            getServer().getPluginManager().registerEvents(new MythicListener(), this);
        }
    }

    public void delayedTask(int delay,Runnable task){
        scheduler.scheduleSyncDelayedTask(this,task,delay);
    }

    public void asyncTask(Runnable task){
        scheduler.runTaskAsynchronously(this,task);
    }

    public void syncTask(Runnable task) {
        scheduler.scheduleSyncDelayedTask(this, task,0);
    }

    public void loadConfig() {
        // 何も必要ない
        new ItemDataConfig().load();
        DiveAPIN.plugin.setItemDataReady(true);

        // 何も必要ない
        new WhistleConfig().load();

        // ItemData が必要
        new ItemConfig().load();
        DiveAPIN.plugin.setItemStackReady(true);

        // Item が必要
        CraftConfig.load();

        new GatherConfig().load();
        new LootConfig().load();
        new MythicConfig().load();
        new RopeConfig().load();
        new ShopConfig().load();
    }

    public void tryCreateColorTeamMap() {
        Map<Rarity, Team> map = ItemEntityObject.INSTANCE.getColorTeamMap();
        if (Bukkit.getScoreboardManager() == null) {
            delayedTask(2,this::tryCreateColorTeamMap);
            return;
        }
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Rarity rarity : Rarity.values()) {
            Team team;
            ChatColor color = ItemEntityMain.getChatColor(rarity);
            try{
                team = board.registerNewTeam("c_"+color.name());
            } catch (IllegalArgumentException e) {
                team = board.getTeam("c_"+color.name());
            }
            assert team != null;
            team.setColor(color);
            map.put(rarity,team);
        }
        Bukkit.getLogger().info("[DiveItem-ItemEntity]color loaded "+map.keySet().size());
    }
}
