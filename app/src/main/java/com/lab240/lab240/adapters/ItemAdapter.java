package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
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

public class ItemAdapter extends RecyclerView.Adapter<ItemHolder>{

    final Map<Out, String> values;
    final Multimap<Out, TextView> views;

    public ItemAdapter(Multimap<Out, TextView> views, Map<Out, String> values) {
        this.values = values;
        this.views = views;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        Out out = outs.get(position);
        holder.topic.setText(out.getName());
        views.remove(holder.out, holder.value);
        views.put(out, holder.value);
        holder.out = out;
        if(values.containsKey(out))
            holder.value.setText(values.get(out));
        else
            holder.value.setText("—");
    }

    @Override
    public int getItemCount() {
        return outs.size();
    }

    private final List<Out> outs = new ArrayList<>();

    public void setData(Collection<Out> outs){
        this.outs.clear();
        this.outs.addAll(outs);
        Collections.sort(this.outs);
        notifyDataSetChanged();
    }
}
