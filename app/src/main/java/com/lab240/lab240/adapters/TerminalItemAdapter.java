package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.common.collect.Multimap;
import com.lab240.devices.Out;
import com.lab240.lab240.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TerminalItemAdapter extends RecyclerView.Adapter<ViewHolder>{

    final Map<Pair<String, Out>, Pair<String, Long>> values;
    final Multimap<Pair<String, Out>, ItemHolder.Updater> updaters;

    public TerminalItemAdapter(Multimap<Pair<String, Out>, ItemHolder.Updater> updaters, Map<Pair<String, Out>, Pair<String, Long>> values) {
        this.values = values;
        this.updaters = updaters;
    }

    private static final int RELAY = 0, ITEM = 1;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        switch (viewType) {
            case ITEM:
                viewHolder = new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_terminal_item, parent, false));
                break;
            case RELAY:
                viewHolder = new RelayHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_terminal_relay, parent, false));
                break;
            default:
                throw new RuntimeException("Unknown item type");
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Out out;
        switch (getItemViewType(position)) {
            case RELAY:
                RelayHolder relayHolder = (RelayHolder) holder;
                out = relays.get(position);
                relayHolder.topic.setText(out.getName());

                updaters.remove(relayHolder.p, relayHolder.updater);

                Pair<String, Out> relayP = Pair.create(device, out);
                relayHolder.p = relayP;
                updaters.put(relayP, relayHolder.updater);
                if (values.containsKey(relayP) && "1".equals(values.get(relayP).first))
                    relayHolder.value.setImageResource(R.drawable.relay_on_image);
                else
                    relayHolder.value.setImageResource(R.drawable.relay_off_image);
                break;
            case ITEM:
                ItemHolder itemHolder = (ItemHolder) holder;
                out = outs.get(position-relays.size());
                itemHolder.topic.setText(out.getName());
                updaters.remove(itemHolder.p, itemHolder.updater);

                Pair<String, Out> itemP = Pair.create(device, out);
                updaters.put(itemP, itemHolder.updater);
                itemHolder.p = itemP;
                if (values.containsKey(itemP))
                    itemHolder.update(values.get(itemP).first);
                else
                    itemHolder.update(null);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return outs.size()+relays.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position < relays.size())
            return RELAY;
        return ITEM;
    }

    private final List<Out> outs = new ArrayList<>();
    private final List<Out> relays = new ArrayList<>();
    private String device = "";

    public void setData(String device, Collection<Out> relays, Collection<Out> outs){
        this.device = device;
        this.outs.clear();
        this.outs.addAll(outs);
        Collections.sort(this.outs);
        this.relays.clear();
        this.relays.addAll(relays);
        Collections.sort(this.relays);
        notifyDataSetChanged();
    }
}
