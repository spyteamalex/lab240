package com.lab240.devices;

public enum Devices {
    THERMOSTAT(
            "Термостат",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("temp_out", "out", "sensors"),
                    new Out("time_up", "out")
            },
            new String[]{
                    "sh net",
                    "sh lschm",
                    "sh tls",
                    "sh bschm{param}",
                    "sh bschm\\{{param}\\}{param2}{param3}{param2}{param3}{param2}{param3}{param2}{param3}{param2}{param3}"
            }),
    THERMOSTAT2(
            "Термостат2",
            "in/params",
            "out/info",
            "out/log",
            new Out[]{
                    new Out("temp_in", "out", "sensors"),
                    new Out("temp_out", "out", "sensors")
            },
            new String[]{
                    "sh net",
                    "sh lschm",
                    "sh tls",
                    "sh bschm{param}"
            });

    public String[] hints;
    public Out[] outs;
    public String name;
    public String mainIn;
    public String mainOut;
    public String log;
    Devices(String name, String mainIn, String mainOut, String log, Out[] outs, String[] hints){
        this.name = name;
        this.hints = hints;
        this.outs = outs;
        this.mainIn = mainIn;
        this.mainOut = mainOut;
        this.log = log;
    }
}
