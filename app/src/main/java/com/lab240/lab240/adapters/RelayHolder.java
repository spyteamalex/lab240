package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.Out;
import com.lab240.lab240.R;

public class RelayHolder extends RecyclerView.ViewHolder{

    final TextView topic;
    final ImageView value;
    Pair<String, Out> p;
    ItemHolder.Updater updater;
    DeviceHolder deviceHolder;

    public RelayHolder(@NonNull View itemView) {
        super(itemView);
        topic = itemView.findViewById(R.id.topic);
        value = itemView.findViewById(R.id.value);

        updater = this::update;
    }

    public void update(@Nullable String v){
        value.setImageResource("1".equals(v) ? R.drawable.relay_on_image : R.drawable.relay_off_image);
        if(deviceHolder != null) deviceHolder.setAvailable(v != null);
    }
}
