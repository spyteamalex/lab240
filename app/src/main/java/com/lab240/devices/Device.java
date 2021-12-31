package com.lab240.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Device {

    public static final int SAVE_COUNT = 10;
    private String identificator;       //Техническое имя устойства
    private String name;
    private String group;
    private final long id ;      //Уникальный номер
    private long type;
    private final Set<Out> outs = new TreeSet<>();
    private final Set<Out> relays = new TreeSet<>();

    public Device setType(long type) {
        this.type = type;
        return this;
    }


    public List<OutLine> getConsoleLasts() {
        return consoleLasts;
    }

    private final List<OutLine> consoleLasts = new ArrayList<>();

    public String getIdentificator() {
        return identificator;
    }

    public long getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Device setName(String name) {
        this.name = name;
        return this;
    }

    public Device setIdentificator(String identificator) {
        this.identificator = identificator;
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
    
    public Set<Out> getRelays() {
        return relays;
    }

    public Device(String name, String identificator, String group, long id, long type) {
        this.name = name;
        this.identificator = identificator;
        this.group = group;
        this.id = id;
        this.type = type;
    }

    public Device(String name, String identificator, String group, long id, long type, Set<Out> relays, Set<Out> outs) {
        this.name = name;
        this.identificator = identificator;
        this.group = group;
        this.id = id;
        this.type = type;
        this.relays.clear();
        this.relays.addAll(relays);
        this.outs.clear();
        this.outs.addAll(outs);
    }
}
