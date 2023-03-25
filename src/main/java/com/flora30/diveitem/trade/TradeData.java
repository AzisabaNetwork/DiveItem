package com.flora30.diveitem.trade;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeData {
    public TradePhase phase = TradePhase.Invite;
    public final UUID to;
    public final List<ItemStack> items = new ArrayList<>();

    // invite用
    public int inviteTime = 0;

    public TradeData(UUID to) {
        this.to = to;
        for (int i = 0; i < 15; i++) {
            items.add(null);
        }
    }
}
