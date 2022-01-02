package com.lab240.devices;

import com.lab240.utils.Showable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class DeviceTypes implements Showable {
    public static final DeviceTypes EMPTY = new DeviceTypes("Устройство", 0);
    public final Set<Out> outs, relays;
    public final String name;
    public final long id;
    public final List<Hint> setterHints;
    public final List<Hint> getterHints;
    public static final Out mainIn = new Out("params", "in");
    public static final Out mainOut = new Out("info", "out");
    public static final Out log = new Out("log", "out");

    public DeviceTypes(String name, long id) {
        this.id = id;
        this.name = name;
        this.setterHints = Collections.emptyList();
        this.getterHints = Collections.emptyList();
        this.outs = Collections.emptySet();
        this.relays = Collections.emptySet();
    }

    DeviceTypes(String name, long id, Out[] relays, Out[] outs, Hint[] setterHints, Hint[] getterHints) {
        this.id = id;
        this.name = name;
        this.setterHints = Collections.unmodifiableList(Arrays.asList(setterHints));
        this.getterHints = Collections.unmodifiableList(Arrays.asList(getterHints));

        Set<Out> outs1 = new TreeSet<>();
        Collections.addAll(outs1, outs);
        this.outs = Collections.unmodifiableSet(outs1);

        Set<Out> relays1 = new TreeSet<>();
        Collections.addAll(relays1, relays);
        this.relays = Collections.unmodifiableSet(relays1);
    }

    public DeviceTypes(String name, long id, Set<Out> relays, Set<Out> outs, List<Hint> setterHints, List<Hint> getterHints) {
        this.id = id;
        this.name = name;
        this.setterHints = Collections.unmodifiableList(setterHints);
        this.getterHints = Collections.unmodifiableList(getterHints);
        this.outs = Collections.unmodifiableSet(outs);
        this.relays = Collections.unmodifiableSet(relays);
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outs, relays, name, id, setterHints, getterHints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceTypes that = (DeviceTypes) o;
        List<Hint> setters1 = new ArrayList<>(setterHints), setters2 = new ArrayList<>(that.setterHints);
        List<Hint> getters1 = new ArrayList<>(getterHints), getters2 = new ArrayList<>(that.getterHints);
        Collections.sort(setters1);
        Collections.sort(setters2);
        Collections.sort(getters1);
        Collections.sort(getters2);
        return Objects.equals(outs, that.outs) &&
                Objects.equals(relays, that.relays) &&
                Objects.equals(name, that.name) &&
                Objects.equals(setters1, setters2) &&
                Objects.equals(getters1, getters2);
    }
}
