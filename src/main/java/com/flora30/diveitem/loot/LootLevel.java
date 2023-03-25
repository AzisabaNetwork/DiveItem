package com.flora30.diveitem.loot;

import org.bukkit.Material;
import org.bukkit.Particle;

public class LootLevel {
    //yml
    private String titlePlus = "Lv.1";
    private int chestSlot = 9;
    private double percent = 0;
    //yml取得
    public Particle particle = Particle.END_ROD;
    public Material material = Material.CHEST;


    public String getTitlePlus() {
        return titlePlus;
    }

    public int getChestSlot() {
        return chestSlot;
    }

    public double getPercent() {
        return percent;
    }

    public void setTitlePlus(String titlePlus) {
        this.titlePlus = titlePlus;
    }

    public void setChestSlot(int chestSlot) {
        this.chestSlot = chestSlot;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
