package com.lab240.lab240.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
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

public class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupHolder(fm, opened, LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_group, parent, false), updaters, values, tc);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        String g = groups.get(position);
        holder.group = g;
        holder.adapter.setData(devices.get(g));
        holder.setVisible(opened.contains(g), false);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    private final Multimap<String, Device> devices = ArrayListMultimap.create();
    private final Multimap<Pair<String, Out>, ItemHolder.Updater> updaters = ArrayListMultimap.create();
    private final Map<Pair<String, Out>, Pair<String, Long>> values = new HashMap<>();
    private final List<String> groups = new ArrayList<>();
    private final @Nullable
    DeviceHolder.Functions tc;
    private final FragmentManager fm;

    private final Set<String> opened = new HashSet<>();

    public GroupAdapter(FragmentManager fm, @Nullable DeviceHolder.Functions tc) {
        this.tc = tc;
        this.fm = fm;
    }

    public Set<Pair<String, MQTT.MessageCallback>> callbacks = new HashSet<>();

    public static final String OUT_DEFAULT = "—";
    public static final long MAX_NO_MSG_TIME = 1000 * 60 * 5;

    public synchronized void setData(Collection<Device> data) {
        for (Pair<String, MQTT.MessageCallback> p : callbacks)
            Lab240.getMqtt().removeListener(p.first, p.second);
        callbacks.clear();
        groups.clear();
        devices.clear();
        for (Device d : data) {
            for (Out o : d.getOuts()) {
                Pair<String, Out> p = Pair.create(d.getIdentificator(), o);
                if(values.containsKey(p) &&
                        !((System.currentTimeMillis() - values.get(p).second) < MAX_NO_MSG_TIME
                                && Lab240.getMqtt() != null
                                && Lab240.getMqtt().isConnected()))
                    values.remove(p);
            }
            for (Out o : d.getRelays()) {
                Pair<String, Out> p = Pair.create(d.getIdentificator(), o);
                if(values.containsKey(p) &&
                        !((System.currentTimeMillis() - values.get(p).second) < MAX_NO_MSG_TIME
                                && Lab240.getMqtt() != null
                                && Lab240.getMqtt().isConnected()))
                    values.remove(p);
            }
            devices.put(d.getGroup(), d);
            if (Lab240.getMqtt() == null || Lab240.getMqtt().isConnected()) {
                for (Out o : d.getOuts()) {
                    String path = Lab240.getOutPath(d, o);
                    Lab240.getMqtt().subscribe(path, 0, ListActivity.KEY, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                        }
                    });
                    MQTT.MessageCallback mc = (topic, msg) -> {
                        values.put(Pair.create(d.getIdentificator(), o), Pair.create(msg.toString(), System.currentTimeMillis()));
                        updateValues(d.getIdentificator(), o, OUT_DEFAULT);
                    };
                    Lab240.getMqtt().addListener(path, mc);
                    callbacks.add(Pair.create(path, mc));
                }
                for (Out o : d.getRelays()) {
                    String path = Lab240.getOutPath(d, o);
                    Lab240.getMqtt().subscribe(path, 0, ListActivity.KEY, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                        }
                    });
                    MQTT.MessageCallback mc = (topic, msg) -> {
                        values.put(Pair.create(d.getIdentificator(), o), Pair.create(msg.toString(), System.currentTimeMillis()));
                        updateValues(d.getIdentificator(), o, ItemHolder.RELAY_DEFAULT);
                    };
                    Lab240.getMqtt().addListener(path, mc);
                    callbacks.add(Pair.create(path, mc));
                }
            }
        }
        groups.addAll(devices.keySet());
        Collections.sort(groups);
        notifyDataSetChanged();
    }

    public synchronized void updateValues(String device, Out out, String def) {
        Pair<String, Out> p = Pair.create(device, out);
        String str = values.containsKey(p) ? values.get(p).first : def;
        for (ItemHolder.Updater i : updaters.get(p)) {
            i.update(str);
        }
    }
}

