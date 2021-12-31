package com.lab240.devices;

import com.lab240.utils.Showable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class DeviceTypes implements Showable {
    public static final DeviceTypes EMPTY = new DeviceTypes("Устройство", 0);

    //todo delete
    public static DeviceTypes[] DEFAULT_TYPES = new DeviceTypes[]{new DeviceTypes("Выравниватель температуры", 0,
            new Out[]{
                    new Out("r1", "out", "relays"),
                    new Out("r2", "out", "relays")
            },
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("temp_out", "out", "sensors"),
                    new Out("temp_odd", "out", "sensors"),
                    new Out("time_up", "out")
            },
            Hints.DEFAULT_SETTER_HINTS,
            Hints.DEFAULT_GETTER_HINTS),
            new DeviceTypes("Контроль станции", 1,
                    new Out[]{
                            new Out("r1", "out", "relays")
                    },
                    new Out[]{
                            new Out("temp_in", "out", "sensors"),
                            new Out("temp_out", "out", "sensors"),
                            new Out("current", "out", "sensors"),
                            new Out("strong_sec", "out", "sensors"),
                            new Out("time_up", "out")
                    },
                    Hints.DEFAULT_SETTER_HINTS,
                    Hints.DEFAULT_GETTER_HINTS),
            new DeviceTypes("Термостат", 2,
                    new Out[]{
                            new Out("r1", "out", "relays")
                    },
                    new Out[]{
                            new Out("temp_in", "out", "sensors"),
                            new Out("temp_out", "out", "sensors"),
                            new Out("time_up", "out")
                    },
                    Hints.DEFAULT_SETTER_HINTS,
                    Hints.DEFAULT_GETTER_HINTS),
            new DeviceTypes("Розетка с таймером", 3,
                    new Out[]{
                            new Out("r1", "out", "relays"),
                            new Out("r2", "out", "relays")
                    },
                    new Out[]{
                            new Out("temp_in", "out", "sensors"),
                            new Out("time_up", "out")
                    },
                    Hints.DEFAULT_SETTER_HINTS,
                    Hints.DEFAULT_GETTER_HINTS),
            new DeviceTypes("Розетка с контролем тока", 4,
                    new Out[]{
                            new Out("r1", "out", "relays"),
                            new Out("r2", "out", "relays")
                    },
                    new Out[]{
                            new Out("temp_in", "out", "sensors"),
                            new Out("current", "out", "sensors"),
                            new Out("strong_sec", "out", "sensors"),
                            new Out("time_up", "out")
                    },
                    Hints.DEFAULT_SETTER_HINTS,
                    Hints.DEFAULT_GETTER_HINTS),
//            new DeviceTypes("Контроль станции и термостат", 5,
//                    new Out[]{
//                            new Out("r1", "out", "relays"),
//                            new Out("r2", "out", "relays")
//                    },
//                    new Out[]{
//                            new Out("temp_in", "out", "sensors"),
//                            new Out("temp_out", "out", "sensors"),
//                            new Out("current", "out", "sensors"),
//                            new Out("strong_sec", "out", "sensors"),
//                            new Out("time_up", "out")
//                    },
//                    Hints.DEFAULT_SETTER_HINTS,
//                    Hints.DEFAULT_GETTER_HINTS),
//            new DeviceTypes("Подсветка и полив", 6,
//                    new Out[]{
//                            new Out("r1", "out", "relays"),
//                            new Out("r2", "out", "relays")
//                    },
//                    new Out[]{
//                            new Out("temp_in", "out", "sensors"),
//                            new Out("time_up", "out")
//                    },
//                    Hints.DEFAULT_SETTER_HINTS,
//                    Hints.DEFAULT_GETTER_HINTS),
//            new DeviceTypes("Термостат с розеткой", 7,
//                    new Out[]{
//                            new Out("r1", "out", "relays"),
//                            new Out("r2", "out", "relays")
//                    },
//                    new Out[]{
//                            new Out("temp_in", "out", "sensors"),
//                            new Out("temp_out", "out", "sensors"),
//                            new Out("time_up", "out")
//                    },
//                    Hints.DEFAULT_SETTER_HINTS,
//                    Hints.DEFAULT_GETTER_HINTS),
//            new DeviceTypes("Контроль тока(1 фаза) на sct013", 8,
//                    new Out[]{},
//                    new Out[]{
//                            new Out("sct013_1", "out", "sensors"),
//                            new Out("time_up", "out")
//                    },
//                    Hints.DEFAULT_SETTER_HINTS,
//                    Hints.DEFAULT_GETTER_HINTS),
//            new DeviceTypes("Контроль тока(3 фазы) на sct013", 9,
//                    new Out[]{},
//                    new Out[]{
//                            new Out("sct013_1", "out", "sensors"),
//                            new Out("sct013_2", "out", "sensors"),
//                            new Out("sct013_3", "out", "sensors"),
//                            new Out("sct013x3", "out", "sensors"),
//                            new Out("time_up", "out")
//                    },
//                    Hints.DEFAULT_SETTER_HINTS,
//                    Hints.DEFAULT_GETTER_HINTS),
//            new DeviceTypes("Контроль тока(1 фаза) на pzem004", 10,
//                    new Out[]{},
//                    new Out[]{
//                            new Out("pzem_current", "out", "sensors"),
//                            new Out("pzem_voltage", "out", "sensors"),
//                            new Out("pzem_energy", "out", "sensors"),
//                            new Out("pzem_power", "out", "sensors"),
//                            new Out("pzem004", "out", "sensors"),
//                            new Out("time_up", "out")
//                    },
//                    Hints.DEFAULT_SETTER_HINTS,
//                    Hints.DEFAULT_GETTER_HINTS)
    };

    public Set<Out> outs, relays;
    public String name;
    public long id;
    public List<String> setterHints;
    public List<String> getterHints;
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

    DeviceTypes(String name, long id, Out[] relays, Out[] outs, String[] setterHints, String[] getterHints) {
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

    public DeviceTypes(String name, long id, Set<Out> relays, Set<Out> outs, List<String> setterHints, List<String> getterHints) {
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
        List<String> setters1 = new ArrayList<>(setterHints), setters2 = new ArrayList<>(that.setterHints);
        List<String> getters1 = new ArrayList<>(getterHints), getters2 = new ArrayList<>(that.getterHints);
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
