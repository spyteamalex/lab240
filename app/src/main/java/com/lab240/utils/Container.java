package com.lab240.utils;

public class Container<T> {
    public T get() {
        return t;
    }

    public Container<T> set(T t) {
        this.t = t;
        return this;
    }

    private T t;
    public Container(T t){
        this.t = t;
    }
}
