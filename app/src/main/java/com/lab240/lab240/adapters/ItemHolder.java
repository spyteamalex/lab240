package com.lab240.lab240.adapters;

import android.graphics.Color;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.Out;
import com.lab240.lab240.R;

public class ItemHolder extends RecyclerView.ViewHolder{

    final TextView topic, value;
    Pair<String, Out> p;
    GroupAdapter.Updater updater;

    public ItemHolder(@NonNull View itemView) {
        super(itemView);
        topic = itemView.findViewById(R.id.topic);
        value = itemView.findViewById(R.id.value);

        updater = this::update;
    }

    public void update(String v){
        value.setText(v);
    }
}
