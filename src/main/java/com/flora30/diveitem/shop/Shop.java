package com.flora30.diveitem.shop;

import java.util.ArrayList;
import java.util.List;

public class Shop {
    private List<ShopGood> goodsList = new ArrayList<>();

    public List<ShopGood> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<ShopGood> goodsList) {
        this.goodsList = goodsList;
    }
}
