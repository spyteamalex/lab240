package com.lab240.devices;

public class HintGroup implements Hint{
    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String[] getCommands() {
        return cmds;
    }

    @Override
    public boolean showDialog() {
        return true;
    }

    public HintGroup(String group, String... cmds) {
        this.group = group;
        this.cmds = cmds;
    }

    private final String group;
    private final String[] cmds;
}
