package com.lab240.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Dashboard {

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public Dashboard setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public Dashboard setGroup(String group) {
        this.group = group;
        return this;
    }

    private String name;
    private String group;
    private final long id;

    public Map<Long, Item> getItems() {
        return items;
    }

    private final Map<Long, Item> items = new TreeMap<>();

    public Dashboard(String name, long id, String group) {
        this.name = name;
        this.group = group;
        this.id = id;
    }
}
