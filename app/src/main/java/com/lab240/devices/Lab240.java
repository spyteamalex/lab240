package com.lab240.devices;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lab240.utils.DeviceAdapter;
import com.lab240.utils.DeviceListAdapter;
import com.lab240.utils.DeviceTypeAdapter;
import com.lab240.utils.DeviceTypeMapAdapter;
import com.lab240.utils.HintAdapter;
import com.lab240.utils.HintListAdapter;
import com.lab240.utils.OutAdapter;
import com.lab240.utils.OutSetAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class Lab240 {

    public static final String APP_PREFERENCES = "Config", NAME = "Name", PASS = "Pass", DEVICES = "Devices", DEVICE_TYPES = "Device Types", HIDDEN_GROUPS = "Hidden Groups";

    private static MQTT mqtt = null;
    private static final List<Device> devices = new ArrayList<>();
    private static final Map<Long, DeviceTypes> deviceTypes = new HashMap<>();
    private static final Set<String> hiddenGroups = new HashSet<>();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Device.class, new DeviceAdapter())
            .registerTypeAdapter(DeviceTypes.class, new DeviceTypeAdapter())
            .registerTypeAdapter(Out.class, new OutAdapter())
            .registerTypeAdapter(Hint.class, new HintAdapter())
            .registerTypeAdapter(new TypeToken<List<Device>>(){}.getType(), new DeviceListAdapter())
            .registerTypeAdapter(new TypeToken<Map<Long, DeviceTypes>>(){}.getType(), new DeviceTypeMapAdapter())
            .registerTypeAdapter(new TypeToken<Set<Out>>(){}.getType(), new OutSetAdapter())
            .registerTypeAdapter(new TypeToken<List<Hint>>(){}.getType(), new HintListAdapter())
            .create();


    public static final Map<Long, DeviceTypes> DEFAULT_TYPES = new TreeMap<>();
    static {
        DEFAULT_TYPES.put(DeviceTypes.EMPTY.getId(), DeviceTypes.EMPTY);
    }

    public static class Config{
        public String name;
        public String pass;

        public Config(String name, String pass, List<Device> devices, Set<String> hiddenGroups, Map<Long, DeviceTypes> deviceTypes) {
            this.name = name;
            this.pass = pass;
            this.devices = devices;
            this.deviceTypes = deviceTypes;
            this.deviceTypes.put(DeviceTypes.EMPTY.getId(), DeviceTypes.EMPTY);
            this.hiddenGroups = new HashSet<>();
            if(devices != null && hiddenGroups != null)
                for(Device d : devices){
                    if(hiddenGroups.contains(d.getGroup()))
                        this.hiddenGroups.add(d.getGroup());
                    if(!deviceTypes.containsKey(d.getType()))
                        d.setType(DeviceTypes.EMPTY.getId());
                }
        }

        public List<Device> devices;
        public Map<Long, DeviceTypes> deviceTypes;
        public Set<String> hiddenGroups;
    }

    public static void setMqtt(MQTT mqtt) {
        Lab240.mqtt = mqtt;
    }

    /**
     * @return true, if mqtt client created, connected
     */
    public static boolean isInited() {
        return mqtt != null && mqtt.isConnected();
    }

    public static MQTT getMqtt() {
        return mqtt;
    }

    public static List<Device> getDevices() {
        return devices;
    }

    public static void saveConfig(Context c, Config conf){
        Log.i("call", "Save config in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(NAME, conf.name);
        edit.putString(PASS, conf.pass);
        edit.putString(DEVICES, serializeDevices(conf.devices));
        edit.putString(HIDDEN_GROUPS, serializeGroups(conf.hiddenGroups));
        edit.putString(DEVICE_TYPES, serializeDeviceTypes(conf.deviceTypes));
        edit.apply();
    }

    public static void setConfig(MQTT mqtt, Config conf){
        Log.i("call", "Set config in Lab240");
        setMqtt(mqtt);
        devices.clear();
        devices.addAll(conf.devices);
        hiddenGroups.clear();
        hiddenGroups.addAll(conf.hiddenGroups);
        deviceTypes.clear();
        deviceTypes.putAll(conf.deviceTypes);
    }

    public static Optional<Config> getConfig(Context c){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(!sp.contains(NAME) || !sp.contains(PASS) || !sp.contains(DEVICES))
            return Optional.absent();
        return Optional.of(new Config(sp.getString(NAME, ""), sp.getString(PASS, ""), deserializeDevices(sp.getString(DEVICES, "")), deserializeGroups(sp.getString(HIDDEN_GROUPS, "")), deserializeDeviceTypes(sp.getString(DEVICE_TYPES, ""))));
    }

    public static void saveDevices(Context c, List<Device> devices){
        Log.i("call", "Save devices in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(DEVICES, serializeDevices(devices));
        edit.apply();
    }

    public static void saveDeviceTypes(Context c, Map<Long, DeviceTypes> deviceTypes){
        Log.i("call", "Save device types in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(DEVICE_TYPES, serializeDeviceTypes(deviceTypes));
        edit.apply();
    }

    public static void saveHiddenGroups(Context c, Set<String> groups){
        Log.i("call", "Save devices in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(HIDDEN_GROUPS, serializeGroups(groups));
        edit.apply();
    }

    public static void exit(Context c){
        Log.i("call", "Exit in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.apply();
    }

    public static Pair<List<Device>, Map<Long, DeviceTypes>> fromDeviceConfig(String string){
        try {
            JsonObject jo = gson.fromJson(string, JsonObject.class);
            Map<Long, DeviceTypes> types = jo.has(DEVICE_TYPES) ? gson.fromJson(jo.get(DEVICE_TYPES), new TypeToken<Map<Long, DeviceTypes>>() {
            }.getType()) : Collections.emptyMap();
            List<Device> devices = jo.has(DEVICES) ? gson.fromJson(jo.get(DEVICES), new TypeToken<List<Device>>() {
            }.getType()) : Collections.emptyList();
            for (Device d : devices) {
                if (!types.containsKey(d.getType()))
                    d.setType(DeviceTypes.EMPTY.getId());
            }
            Map<Long, Long> toReplace = new TreeMap<>();
            long i = 0;
            toReplace.put(DeviceTypes.EMPTY.getId(), DeviceTypes.EMPTY.getId());
            for (Map.Entry<Long, DeviceTypes> t : types.entrySet()) {
                long key = -1;
                for (Map.Entry<Long, DeviceTypes> p : Lab240.deviceTypes.entrySet()) {
                    if (Objects.equals(Lab240.deviceTypes.get(p.getKey()), t.getValue())) {
                        key = p.getKey();
                        break;
                    }
                }
                if (key == -1) {
                    if (!Lab240.deviceTypes.containsKey(t.getKey())) {
                        toReplace.put(t.getKey(), t.getKey());
                        continue;
                    }
                    for (i++; types.containsKey(i) || Lab240.deviceTypes.containsKey(i); i++) ;
                    key = i;
                }
                toReplace.put(t.getKey(), key);
            }
            Map<Long, DeviceTypes> types2 = new HashMap<>();
            List<Device> devices2 = new ArrayList<>();
            for (Device d : devices) {
                Device d2 = new Device(d.getName(), d.getIdentificator(), d.getGroup(), d.getId(), toReplace.get(d.getType()), d.getRelays(), d.getOuts());
                devices2.add(d2);
            }
            for (Map.Entry<Long, DeviceTypes> t : types.entrySet()) {
                DeviceTypes dt = t.getValue();
                types2.put(toReplace.get(t.getKey()), new DeviceTypes(dt.getName(), toReplace.get(t.getKey()), dt.getRelays(), dt.getOuts(), dt.getGetterHints(), dt.getSetterHints()));
            }
            return Pair.create(devices2, types2);
        }catch (JsonSyntaxException e){
            return Pair.create(Collections.emptyList(), Collections.emptyMap());
        }
    }

    public static String toDeviceConfig(List<Device> devices, Map<Long, DeviceTypes> deviceTypes){
        JsonElement devices1 = gson.toJsonTree(devices);
        JsonElement deviceTypes1 = gson.toJsonTree(deviceTypes);
        JsonObject jo = new JsonObject();
        jo.add(DEVICES, devices1);
        jo.add(DEVICE_TYPES, deviceTypes1);
        return gson.toJson(jo);
    }

    public static List<Device> deserializeDevices(String s){
        return gson.fromJson(s, new TypeToken<List<Device>>(){}.getType());
    }

    public static String serializeDevices(List<Device> devices){
        return gson.toJson(devices);
    }

    public static Map<Long, DeviceTypes> deserializeDeviceTypes(String s){
        return gson.fromJson(s, new TypeToken<Map<Long, DeviceTypes>>(){}.getType());
    }

    public static String serializeDeviceTypes(Map<Long, DeviceTypes> devices){
        return gson.toJson(devices);
    }

    public static Set<String> deserializeGroups(String s){
        return gson.fromJson(s, new TypeToken<Set<String>>(){}.getType());
    }

    public static String serializeGroups(Set<String> devices){
        return gson.toJson(devices);
    }

    public static List<OutLine> deserializeOutLines(String s){
        return gson.fromJson(s, new TypeToken<List<OutLine>>(){}.getType());
    }

    public static String serializeOutLines(List<OutLine> outLines){
        return gson.toJson(outLines);
    }

    public static String getOutPath(Device d, Out o){
        if(mqtt == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(mqtt.getName());
        sb.append("/").append(d.getIdentificator());
        for (String s : o.getPath())
            sb.append("/").append(s);
        sb.append("/").append(o.getName());
        return sb.toString();
    }

    public static Map<Long, DeviceTypes> getDeviceTypes() {
        return deviceTypes;
    }

    public static Set<String> getHiddenGroups() {
        return hiddenGroups;
    }
}