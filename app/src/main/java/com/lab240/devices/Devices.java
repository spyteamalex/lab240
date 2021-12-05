package com.lab240.devices;

public enum Devices {
    TEMP_EQUALIZER(
            "Выравниватель температуры",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
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
    STATION_CONTROL(
            "Контроль станции",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
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
    THERMOSTAT(
            "Термостат",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
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
    SOCKET_WITH_TIMER(
            "Розетка с таймером",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
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
    SOCKET_WITH_CURRENT_CONTROL(
            "Розетка с контролем тока",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
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
    STATION_CONTROL_AND_THERMOSTAT(
            "Контроль станции и термостат",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
            new Out[]{
                    new Out("r1", "out", "relays"),
                    new Out("r2", "out", "relays")
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
    LIGHTING_AND_WATERING(
            "Подсветка и полив",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
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
    THERMOSTAT_WITH_SOCKET(
            "Термостат с розеткой",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
            new Out[]{
                    new Out("r1", "out", "relays"),
                    new Out("r2", "out", "relays")
            },
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("temp_out", "out", "sensors"),
                    new Out("time_up", "out")
            },
            Hints.DEFAULT_SETTER_HINTS,
            Hints.DEFAULT_GETTER_HINTS),
    CURRENT_CONTROLLER_1PHASE_SCT013(
            "Контроль тока(1 фаза) на sct013",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
            new Out[]{},
            new Out[]{
                    new Out("sct013_1", "out", "sensors"),
                    new Out("time_up", "out")
            },
            Hints.DEFAULT_SETTER_HINTS,
            Hints.DEFAULT_GETTER_HINTS),
    CURRENT_CONTROLLER_3PHASE(
            "Контроль тока(3 фазы) на sct013",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
            new Out[]{},
            new Out[]{
                    new Out("sct013_1", "out", "sensors"),
                    new Out("sct013_2", "out", "sensors"),
                    new Out("sct013_3", "out", "sensors"),
                    new Out("sct013x3", "out", "sensors"),
                    new Out("time_up", "out")
            },
            Hints.DEFAULT_SETTER_HINTS,
            Hints.DEFAULT_GETTER_HINTS),
    CURRENT_CONTROLLER_1PHASE_PZEM004(
            "Контроль тока(1 фаза) на pzem004",
            new Out("params", "in"),
            new Out("info", "out"),
            new Out("log", "out"),
            new Out[]{},
            new Out[]{
                    new Out("pzem_current", "out", "sensors"),
                    new Out("pzem_voltage", "out", "sensors"),
                    new Out("pzem_energy", "out", "sensors"),
                    new Out("pzem_power", "out", "sensors"),
                    new Out("pzem004", "out", "sensors"),
                    new Out("time_up", "out")
            },
            Hints.DEFAULT_SETTER_HINTS,
            Hints.DEFAULT_GETTER_HINTS);


    public Out[] outs, relays;
    public String name;
    public String[] setterHints;
    public String[] getterHints;
    public Out mainIn;
    public Out mainOut;
    public Out log;

    Devices(String name, Out mainIn, Out mainOut, Out log, Out[] relays, Out[] outs, String[] setterHints, String[] getterHints){
        this.name = name;
        this.setterHints = setterHints;
        this.getterHints = getterHints;
        this.outs = outs;
        this.mainIn = mainIn;
        this.mainOut = mainOut;
        this.log = log;
        this.relays = relays;
    }


}
