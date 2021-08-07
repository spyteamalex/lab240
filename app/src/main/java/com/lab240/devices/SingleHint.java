package com.lab240.devices;

public class SingleHint implements Hint{
    @Override
    public String getGroup() {
        return group;
    }

    /**
     * @return Single command in array
     */
    @Override
    public String[] getCommands() {
        return cmd;
    }

    @Override
    public boolean showDialog() {
        return false;
    }

    public SingleHint(String group, String cmd) {
        this.group = group;
        this.cmd = new String[]{cmd};
    }

    private final String group;
    private final String[] cmd;
}