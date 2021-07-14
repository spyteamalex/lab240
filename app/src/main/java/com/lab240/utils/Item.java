package com.lab240.utils;

public class Item {
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

    public long getId() {
        return id;
    }

    public Item(long id, String name, String topic) {
        this.name = name;
        this.topic = topic;
        this.id = id;
    }

    private final long id;
}
