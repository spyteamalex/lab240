package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.lab240.ListActivity;
import com.lab240.lab240.R;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupAdapter extends RecyclerView.Adapter<GroupHolder>{

    interface Updater{
        void update(String s);
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_group, parent, false), groups, updaters, values, tc, update);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        String g = groups.get(position);
        holder.group = g;
        holder.name.setText(g);
        holder.adapter.setData(devices.get(g));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    private final Multimap<String, Device> devices = ArrayListMultimap.create();
    private final Multimap<Pair<String, Out>, Updater> updaters = ArrayListMultimap.create();
    private final Map<Pair<String, Out>, String> values = new HashMap<>();
    private final List<String> groups = new ArrayList<>();
    private final @Nullable DeviceHolder.TerminalCaller tc;

    public GroupAdapter(@Nullable DeviceHolder.TerminalCaller tc, @Nullable Runnable update) {
        this.update = update;
        this.tc = tc;
    }

    private final @Nullable
    Runnable update;

    public Set<Pair<String, MQTT.MessageCallback>> callbacks = new HashSet<>();

    public synchronized void setData(Collection<Device> data){
        for(Pair<String, MQTT.MessageCallback> p : callbacks)
            Lab240.getMqtt().removeListener(p.first, p.second);
        callbacks.clear();
        groups.clear();
        devices.clear();
        for (Device d: data) {
            devices.put(d.getGroup(), d);
            for(Out o : d.getOuts()){
                String path = Lab240.getOutPath(d, o);
                Lab240.getMqtt().subscribe(Lab240.getOutPath(d, o), 0, ListActivity.KEY, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        values.put(Pair.create(d.getName(), o), "Fail");
                    }
                });
                MQTT.MessageCallback mc = (topic, msg) -> {
                    values.put(Pair.create(d.getName(), o), msg.toString());
                    updateValues(d.getName(), o);
                };
                Lab240.getMqtt().addListener(path, mc);
                callbacks.add(Pair.create(path, mc));

            }
            for(Out o : d.getType().relays){
                String path = Lab240.getOutPath(d, o);
                Lab240.getMqtt().subscribe(Lab240.getOutPath(d, o), 0, ListActivity.KEY, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        values.put(Pair.create(d.getName(), o), "0");
                    }
                });
                MQTT.MessageCallback mc = (topic, msg) -> {
                    values.put(Pair.create(d.getName(), o), msg.toString());
                    updateValues(d.getName(), o);
                };
                Lab240.getMqtt().addListener(path, mc);
                callbacks.add(Pair.create(path, mc));
            }
        }
        groups.addAll(devices.keySet());
        Collections.sort(groups);
        notifyDataSetChanged();
    }

    public synchronized void updateValues(String device, Out out) {
        Pair<String, Out> p = Pair.create(device, out);
        String str = values.containsKey(p) ? values.get(p) : "—";
        for (Updater i: updaters.get(p)) {
            i.update(str);
        }
    }
}

