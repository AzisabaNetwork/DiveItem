package com.flora30.diveitem.shop;

public class ShopGood {
    private int itemId;
    private int amount;
    private int money;

    public int getAmount() {
        return amount;
    }

    public int getMoney() {
        return money;
    }

    public int getItemId() {
        return itemId;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMoney(int money) {
            this.money = money;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
