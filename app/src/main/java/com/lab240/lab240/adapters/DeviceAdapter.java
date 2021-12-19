package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.Lab240;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder>{

    public DeviceAdapter(FragmentManager fm, Multimap<Pair<String, Out>, ItemHolder.Updater> updaters, Map<Pair<String, Out>, Pair<String, Long>> values, @Nullable DeviceHolder.Functions tc) {
        this.values = values;
        this.updaters = updaters;
        this.tc = tc;
        this.fm = fm;
    }

    private final Multimap<Pair<String, Out>, ItemHolder.Updater> updaters;
    private final Map<Pair<String, Out>, Pair<String, Long>> values;
    private final FragmentManager fm;
    private final @Nullable DeviceHolder.Functions tc;

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceHolder(fm,LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_device, parent, false), updaters, values, tc);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Device d = devices.get(position);
        holder.item = d;
        holder.name.setText(d.getName());
         holder.type.setText(String.format(Locale.getDefault(), "%s %s", Lab240.getDeviceTypes().get(d.getType()).name, d.getIdentificator()));
        holder.adapter.setData(d.getIdentificator(), d.getRelays(), d.getOuts());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public final List<Device> devices = new ArrayList<>();

    public void setData(Collection<Device> data){
        devices.clear();
        devices.addAll(data);
        Collections.sort(devices, (d1, d2)->{
            if(d1.getIdentificator().equals(d2.getIdentificator()))
                return Long.compare(d1.getId(), d2.getId());
            return d1.getIdentificator().compareTo(d2.getIdentificator());
        });
        notifyDataSetChanged();
    }
}
