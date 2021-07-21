package com.lab240.Items;

public class RingItem extends IndicatorItem {

    public RingItem(long id, String name, String topic) {
        super(id, name, topic);
    }

    @Override
    public Type getType() {
        return Type.RING;
    }

    public float getMinValue() {
        return minValue;
    }

    public RingItem setMinValue(float minValue) {
        this.minValue = minValue;
        return this;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public RingItem setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    private float minValue, maxValue;

    public RingItem(long id, String name, String topic, float minValue, float maxValue) {
        super(id, name, topic);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}
