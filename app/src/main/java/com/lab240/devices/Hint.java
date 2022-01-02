package com.lab240.devices;

import com.lab240.utils.Comparator;

import java.util.Objects;

public class Hint implements Comparable<Hint> {
    private final String cmd;

    public String getCmd() {
        return cmd;
    }

    public String getHint() {
        return hint;
    }

    private final String hint;

    public Hint(String cmd, String hint) {
        this.cmd = cmd;
        this.hint = hint;
    }

    public Hint(String cmd) {
        this.cmd = cmd;
        this.hint = cmd;
    }

    @Override
    public int compareTo(Hint v) {
        return Comparator.unite(cmd.compareTo(v.cmd), hint.compareTo(v.hint));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hint hint1 = (Hint) o;
        return Objects.equals(cmd, hint1.cmd) &&
                Objects.equals(hint, hint1.hint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cmd, hint);
    }
}
