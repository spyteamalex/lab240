package com.lab240.Items;

public abstract class IndicatorItem extends Item{
    protected IndicatorItem(long id, String name, String topic) {
        super(id);
        this.name = name;
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public Item setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getName() {
        return name;
    }

    public Item setName(String name) {
        this.name = name;
        return this;
    }

    private String name;
    private String topic;
}
