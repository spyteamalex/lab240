package com.lab240.Items;

import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.lab240.utils.Point;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class LineChartItem extends IndicatorItem {

    public LineChartItem(long id, String name, String topic) {
        super(id, name, topic);
    }

    @Override
    public Type getType() {
        return Type.LINE_CHART;
    }

    private final Set<Point<Long, Double>> points = new TreeSet<>(((t1, t2) -> {
        if(t1.getX()-t2.getX() == 0)
            return (int)Math.abs(t1.getY()-t2.getY());
        return (int)Math.abs(t1.getX()-t2.getX());
    }));

    @Override
    public Item setTopic(String topic) {
        points.clear();
        return super.setTopic(topic);
    }

    public Set<Point<Long, Double>> getPoints() {
        return points;
    }
}
