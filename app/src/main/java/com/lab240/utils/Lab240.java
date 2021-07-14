package com.lab240.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;
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
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
        Map<Long, Dashboard> list = new TreeMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String i = it.next();
            try {
                JSONObject dashboard = jsonObject.getJSONObject(i);
                Dashboard db = new Dashboard(dashboard.getString(DASHBOARD_NAME), dashboard.getLong(DASHBOARD_ID), dashboard.getString(DASHBOARD_GROUP));
                list.put(db.getId(), db);

                JSONObject items = dashboard.getJSONObject(DASHBOARD_ITEMS);
                for (Iterator<String> iter = items.keys(); iter.hasNext(); ) {
                    String j = iter.next();
                    try {
                        JSONObject itemJO = items.getJSONObject(j);
                        Item item = new Item(itemJO.getLong(ITEM_ID), itemJO.getString(ITEM_NAME), itemJO.getString(ITEM_TOPIC));
                        db.getItems().put(item.getId(), item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static String serializeDashboards(Map<Long, Dashboard> dashboards){
        JSONObject arr = new JSONObject();
        for(Map.Entry<Long, Dashboard> dbPair : dashboards.entrySet()){
            try {
                Dashboard db = dbPair.getValue();
                JSONObject dbJson = new JSONObject();
                dbJson
                        .put(DASHBOARD_GROUP, db.getGroup())
                        .put(DASHBOARD_ID, db.getId())
                        .put(DASHBOARD_NAME, db.getName());

                JSONObject items = new JSONObject();
                for(Item i : db.getItems().values()){
                    JSONObject item = new JSONObject();
                    item
                            .put(ITEM_TOPIC, i.getTopic())
                            .put(ITEM_NAME, i.getName())
                            .put(ITEM_ID, i.getId());
                    items.put(String.valueOf(i.getId()), item);
                }
                dbJson.put(DASHBOARD_ITEMS, items);
                arr.put(String.valueOf(dbPair.getKey()), dbJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arr.toString();
    }
}