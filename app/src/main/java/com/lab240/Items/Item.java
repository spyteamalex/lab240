package com.lab240.Items;

import com.google.gson.annotations.SerializedName;

public abstract class Item {
    protected Item(long id) {
        this.id = id;
    }


    /**
     * Добавление нового элемента:
     * 1) добавить в Type
     * 2) добавить в RecycleView в onCreateViewHolder и onBindViewHolder
     */
    public enum Type{
        TEXT(TextItem.class),
        RING(RingItem.class),
        LINE_CHART(LineChartItem.class);

        Class cl;
        Type(Class cl){
            this.cl = cl;
        }
    }

    @SerializedName("type")
    public abstract Type getType();

    public long getId() {
        return id;
    }

    private final long id;
}
