package com.lab240.devices;

import java.util.Set;
import java.util.TreeSet;

public class Device {

    private String name;
    private String group;
    private final long id;
    private final Devices type;
    private final Set<Out> outs = new TreeSet<>();

    public String getName() {
        return name;
    }

    public Devices getType() {
        return type;
    }

    public Device setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public Device setGroup(String group) {
        this.group = group;
        return this;
    }

    public long getId() {
        return id;
    }

    public Set<Out> getOuts() {
        return outs;
    }

    public Device(String name, String group, long id, Devices type) {
        this.name = name;
        this.group = group;
        this.id = id;
        this.type = type;
    }
}
