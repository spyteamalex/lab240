package com.lab240.devices;

import com.lab240.utils.HintManager;

public enum Devices {
    THERMOSTAT(
            "Термостат",
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
