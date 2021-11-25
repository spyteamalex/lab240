package com.lab240.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.devices.OutLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lab240 {

    public static final String APP_PREFERENCES = "Config", NAME = "Name", PASS = "Pass", DEVICES = "Devices";

    private static MQTT mqtt = null;
    private static final List<Device> devices = new ArrayList<>();

    public static class Config{
        public String name;
        public String pass;

        public Config(String name, String pass, List<Device> devices) {
            this.name = name;
            this.pass = pass;
            this.devices = devices;
        }

        public List<Device> devices;
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
        edit.apply();
    }

    public static Optional<Config> getConfig(Context c){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(!sp.contains(NAME) || !sp.contains(PASS) || !sp.contains(DEVICES))
            return Optional.absent();
        return Optional.of(new Config(sp.getString(NAME, ""), sp.getString(PASS, ""), deserializeDevices(sp.getString(DEVICES, ""))));
    }

    public static List<Device> loadDevices(Context c){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(!sp.contains(DEVICES))
            return Collections.emptyList();
        return deserializeDevices(sp.getString(DEVICES, "[]"));
    }

    public static void saveDevices(Context c, List<Device> devices){
        Log.i("call", "Save devices in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(DEVICES, serializeDevices(devices));
        edit.apply();
    }

    public static void exit(Context c){
        Log.i("call", "Exit in Lab240");
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.apply();
    }

    public static List<Device> deserializeDevices(String s){
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(s, new TypeToken<List<Device>>(){}.getType());
    }

    public static String serializeDevices(List<Device> devices){
        Gson gson = new GsonBuilder().create();
        return gson.toJson(devices);
    }

    public static List<OutLine> deserializeOutLines(String s){
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(s, new TypeToken<List<OutLine>>(){}.getType());
    }

    public static String serializeOutLines(List<OutLine> outLines){
        Gson gson = new GsonBuilder().create();
        return gson.toJson(outLines);
    }

    public static String getOutPath(Device d, Out o){
        if(mqtt == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(mqtt.getName());
        sb.append("/").append(d.getIdentificator());
        for(String s : o.getPath())
            sb.append("/").append(s);
        sb.append("/").append(o.getName());
        return sb.toString();

    }
}