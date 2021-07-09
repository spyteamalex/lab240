package com.lab240.utils;

public class Item {
    enum Type{
        RING,
        TEXT
    }

    enum Size{
        s1x1(1,1),
        s2x2(2,2),
        s3x3(3,3);

        int x,y;
        Size(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    public static class Point{
        public int x;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int y;
    }


    Type type;
    String topic;
    String name;
    Point pos;
    Point size;

    public Item(Type type, String topic, String name, Point pos, Point size) {
        this.type = type;
        this.topic = topic;
        this.name = name;
        this.pos = pos;
        this.size = size;
    }
}
