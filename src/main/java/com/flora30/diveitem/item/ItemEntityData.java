package com.flora30.diveitem.item;

import java.util.UUID;

public class ItemEntityData {
    private int remain;
    private UUID id;
    public UUID armorStandId;

    public int getRemain() {
        return remain;
    }

    public UUID getId() {
        return id;
    }

    public void setRemain(int remain) {
        this.remain = remain;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
