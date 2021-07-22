package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.lab240.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder>{

    public DeviceAdapter(Multimap<Out, TextView> views, Map<Out, String> values, Runnable update) {
        this.update = update;
        this.values = values;
        this.views = views;
    }

    private final Multimap<Out, TextView> views;
    private final Map<Out, String> values;

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_device, parent, false), views, values, update);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Device d = devices.get(position);
        holder.item = d;
        holder.items.setVisibility(d.getOuts().isEmpty() ? View.GONE : View.VISIBLE);
        holder.name.setText(d.getName());
        holder.type.setText(d.getType().name);
        holder.adapter.setData(d.getOuts());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public final List<Device> devices = new ArrayList<>();
    private final Runnable update;

    public void setData(Collection<Device> data){
        devices.clear();
        devices.addAll(data);
        Collections.sort(devices, (d1, d2)->{
            if(d1.getName().equals(d2.getName()))
                return Long.compare(d1.getId(), d2.getId());
            return d1.getName().compareTo(d2.getName());
        });
        notifyDataSetChanged();
    }
}
