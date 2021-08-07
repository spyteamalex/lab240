package com.lab240.devices;

public class OutLine {
    public enum Type{
        IN, OUT, LOG
    }

    public OutLine(String value, Type type) {
        this.type = type;
        this.value = value;
    }

    public final Type type;
    public final String value;
}
