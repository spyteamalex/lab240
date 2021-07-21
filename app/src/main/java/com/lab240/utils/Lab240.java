package com.lab240.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lab240.Items.Dashboard;
import com.lab240.Items.Item;
import com.lab240.Items.ItemSerializer;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Lab240 {

    public static final String APP_PREFERENCES = "Config", NAME = "Name", PASS = "Pass", DASHBOARDS = "Dashboards";
    public static String DASHBOARD_NAME = "Name", DASHBOARD_ID = "Id", DASHBOARD_GROUP = "Group", DASHBOARD_ITEMS = "Items";
    public static String ITEM_NAME = "Name", ITEM_TOPIC = "Topic", ITEM_ID = "Id";

    private static MQTT mqtt = null;
    private static final Map<Long, Dashboard> dashboards = new TreeMap<>();

    public static class Config{
        public String name;
        public String pass;

        public Config(String name, String pass, Map<Long, Dashboard> dashboards) {
            this.name = name;
            this.pass = pass;
            this.dashboards = dashboards;
        }

        public Map<Long, Dashboard> dashboards;
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

    public static Map<Long, Dashboard> getDashboards() {
        return dashboards;
    }

    public static void saveConfig(Context c, Config conf){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(NAME, conf.name);
        edit.putString(PASS, conf.pass);
        edit.putString(DASHBOARDS, serializeDashboards(conf.dashboards));
        edit.apply();
    }

    public static Optional<Config> getConfig(Context c){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(!sp.contains(NAME) || !sp.contains(PASS) || !sp.contains(DASHBOARDS))
            return Optional.absent();
        return Optional.of(new Config(sp.getString(NAME, ""), sp.getString(PASS, ""), deserializeDashboards(sp.getString(DASHBOARDS, ""))));
    }

    public static Map<Long, Dashboard> loadDashboards(Context c){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(!sp.contains(DASHBOARDS))
            return Collections.emptyMap();
        return deserializeDashboards(sp.getString(DASHBOARDS, "[]"));
    }

    public static void saveDashboards(Context c, Map<Long, Dashboard> dashboards){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(DASHBOARDS, serializeDashboards(dashboards));
        edit.apply();
    }

    public static Map<Long, Dashboard> deserializeDashboards(String s){
        Gson gson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemSerializer()).create();
        return gson.fromJson(s, new TypeToken<TreeMap<Long, Dashboard>>(){}.getType());
    }

    public static String serializeDashboards(Map<Long, Dashboard> dashboards){
        Gson gson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemSerializer()).create();
        return gson.toJson(dashboards);
    }
}