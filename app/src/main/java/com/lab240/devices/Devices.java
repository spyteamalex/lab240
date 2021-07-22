package com.lab240.devices;

public enum Devices {
    THERMOSTAT(
            "Термостат",
            new Out[]{
                    new Out("temp_in", new String[]{"out", "sensors"}),
                    new Out("temp_out", new String[]{"out", "sensors"}),
                    new Out("time_up", new String[]{"out"})
            },
            new String[]{
                    "sh net",
                    "sh lschm",
                    "sh tls",
                    "sh bschm{param}"
            }),
    THERMOSTAT2(
            "Термостат2",
            new Out[]{
                    new Out("temp_in", new String[]{"out", "sensors"}),
                    new Out("temp_out", new String[]{"out", "sensors"})
            },
            new String[]{
                    "sh net",
                    "sh lschm",
                    "sh tls",
                    "sh bschm{param}"
            });

    public String[] commands;
    public Out[] outs;
    public String name;
    Devices(String name, Out[] outs, String[] commands){
        this.name = name;
        this.commands = commands;
        this.outs = outs;
    }
}
