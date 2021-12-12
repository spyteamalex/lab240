package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.Out;
import com.lab240.lab240.R;

public class ItemHolder extends RecyclerView.ViewHolder{

    public static final String OUT_DEFAULT = "—";
    final TextView topic, value;
    Pair<String, Out> p;
    Updater updater;

    public ItemHolder(@NonNull View itemView) {
        super(itemView);
        topic = itemView.findViewById(R.id.topic);
        value = itemView.findViewById(R.id.value);

        updater = this::update;
    }

    public void update(@Nullable String v){
        if(v == null)
            value.setText(OUT_DEFAULT);
        else
            value.setText(v);
    }

    public interface Updater {
        void update(String s);
    }
}
