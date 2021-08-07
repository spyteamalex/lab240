package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.lab240.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder>{

    public DeviceAdapter(List<String> groups, Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters, Map<Pair<String, Out>, String> values, @Nullable DeviceHolder.TerminalCaller tc, @Nullable Runnable update) {
        this.update = update;
        this.values = values;
        this.updaters = updaters;
        this.tc = tc;
        this.groups = groups;
    }

    private final Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters;
    private final Map<Pair<String, Out>, String> values;
    private final @Nullable DeviceHolder.TerminalCaller tc;
    private final List<String> groups;

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_device, parent, false), groups, updaters, values, tc, update);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Device d = devices.get(position);
        holder.item = d;
        holder.name.setText(d.getName());
        holder.type.setText(d.getType().name);
        holder.adapter.setData(d.getName(), Arrays.asList(d.getType().relays), d.getOuts());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public final List<Device> devices = new ArrayList<>();
    private final @Nullable Runnable update;

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
