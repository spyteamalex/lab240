package com.lab240.utils;

public class Point<A extends Number, B extends Number>{
    public B getY() {
        return y;
    }

    private final A x;

    public A getX() {
        return x;
    }

    public Point(A x, B y) {
        this.x = x;
        this.y = y;
    }

    private final B y;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return (x == point.x || x.equals(point.x)) &&
                (y == point.y || y.equals(point.y));
    }
}
