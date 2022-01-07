package com.lab240.lab240.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.DeviceTypes;
import com.lab240.lab240.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeviceTypesAdapter extends RecyclerView.Adapter<DeviceTypesHolder>{

    public DeviceTypesAdapter(FragmentManager fm, DeviceTypesHolder.Functions functions) {
        this.functions = functions;
        this.fm = fm;
    }

    @NonNull
    @Override
    public DeviceTypesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceTypesHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_device_type, parent, false), fm, functions);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceTypesHolder holder, int position) {
        DeviceTypes dt = dts.get(position);
        holder.name.setText(dt.name);
        holder.dt = dt;
        holder.relaysAdapter.setData(dt.relays);
        holder.relays.setVisibility(dt.relays.isEmpty() ? View.GONE : View.VISIBLE);
        holder.relaysLabel.setVisibility(dt.relays.isEmpty() ? View.GONE : View.VISIBLE);

        holder.outsAdapter.setData(dt.outs);
        holder.outs.setVisibility(dt.outs.isEmpty() ? View.GONE : View.VISIBLE);
        holder.outsLabel.setVisibility(dt.outs.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return dts.size();
    }

    private final List<DeviceTypes> dts = new ArrayList<>();
    private final DeviceTypesHolder.Functions functions;
    private final FragmentManager fm;

    public void setData(Collection<DeviceTypes> dts){
        this.dts.clear();
        this.dts.addAll(dts);
        notifyDataSetChanged();
    }
}
