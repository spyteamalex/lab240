package com.lab240.devices;

import com.lab240.utils.HintManager;

public enum Devices {
    TEMP_EQUALIZER(
            "Выравниватель температуры",
            "in/params",
            "out/info",
            "out/log",
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
            HintManager.DEFAULT_HINTS),
    STATION_CONTROL(
            "Контроль станции",
            "in/params",
            "out/info",
            "out/log",
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
            HintManager.DEFAULT_HINTS),
    THERMOSTAT(
            "Термостат",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{
                    new Out("r1", "out", "relays")
            },
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("temp_out", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS),
    SOCKET_WITH_TIMER(
            "Розетка с таймером",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{
                    new Out("r1", "out", "relays"),
                    new Out("r2", "out", "relays")
            },
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS),
    SOCKET_WITH_CURRENT_CONTROL(
            "Розетка с контролем тока",
            "in/params",
            "out/info",
            "out/log",
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
            HintManager.DEFAULT_HINTS),
    STATION_CONTROL_AND_THERMOSTAT(
            "Контроль станции и термостат",
            "in/params",
            "out/info",
            "out/log",
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
            HintManager.DEFAULT_HINTS),
    LIGHTING_AND_WATERING(
            "Подсветка и полив",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{
                    new Out("r1", "out", "relays"),
                    new Out("r2", "out", "relays")
            },
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS),
    THERMOSTAT_WITH_SOCKET(
            "Термостат с розеткой",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{
                    new Out("r1", "out", "relays"),
                    new Out("r2", "out", "relays")
            },
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("temp_out", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS),
    CURRENT_CONTROLLER_1PHASE_SCT013(
            "Контроль тока(1 фаза) на sct013",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{},
            new Out[]{
                    new Out("sct013_1", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS),
    CURRENT_CONTROLLER_3PHASE(
            "Контроль тока(3 фазы) на sct013",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{},
            new Out[]{
                    new Out("sct013_1", "out", "sensors"),
                    new Out("sct013_2", "out", "sensors"),
                    new Out("sct013_3", "out", "sensors"),
                    new Out("sct013x3", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS),
    CURRENT_CONTROLLER_1PHASE_PZEM004(
            "Контроль тока(1 фаза) на pzem004",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{},
            new Out[]{
                    new Out("pzem_current", "out", "sensors"),
                    new Out("pzem_voltage", "out", "sensors"),
                    new Out("pzem_energy", "out", "sensors"),
                    new Out("pzem_power", "out", "sensors"),
                    new Out("pzem004", "out", "sensors"),
                    new Out("time_up", "out")
            },
            HintManager.DEFAULT_HINTS);



    public Hint[] hints;
    public Out[] outs, relays;
    public String name;
    public String mainIn;
    public String mainOut;
    public String log;
    Devices(String name, String mainIn, String mainOut, String log, Out[] relays, Out[] outs, Hint[] hints){
        this.name = name;
        this.hints = hints;
        this.outs = outs;
        this.mainIn = mainIn;
        this.mainOut = mainOut;
        this.log = log;
        this.relays = relays;
    }


}
