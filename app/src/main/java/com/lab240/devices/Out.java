package com.lab240.devices;

import com.lab240.utils.Comparator;

import java.util.Arrays;
import java.util.Objects;

public class Out implements Comparable<Out>  {

    public String getName() {
        return name;
    }

    public Out(String name, String[] path) {
        this.name = name;
        this.path = path;
    }

    private final String name;

    public String[] getPath() {
        return path;
    }

    private final String[] path;

    @Override
    public int compareTo(Out out) {
        int names = out.getName().compareTo(getName());
        int paths = Comparator.compare(path, out.path);
        if(names != 0)
            return names;
        return paths;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(path);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Out out = (Out) o;
        return compareTo(out) == 0;
    }
}
