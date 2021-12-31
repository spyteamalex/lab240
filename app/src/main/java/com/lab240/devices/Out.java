package com.lab240.devices;

import com.lab240.utils.Comparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Out implements Comparable<Out>  {

    public final String getName() {
        return name;
    }

    public Out(String name, String... path) {
        this.name = name;
        this.path = Collections.unmodifiableList(Arrays.asList(path));
    }

    public Out(String name, List<String> path) {
        this.name = name;
        this.path = Collections.unmodifiableList(path);
    }

    private final String name;

    public final List<String> getPath() {
        return path;
    }

    private final List<String> path;

    @Override
    public int compareTo(Out out) {
        int names = getName().compareTo(out.getName());
        int paths = Comparator.compare(path, out.path);
        if(names != 0)
            return names;
        return paths;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Out out = (Out) o;
        return compareTo(out) == 0;
    }
}
