package com.lab240.Items;

public class TextItem extends IndicatorItem {

    public TextItem( long id, String name, String topic) {
        super(id, name, topic);
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
