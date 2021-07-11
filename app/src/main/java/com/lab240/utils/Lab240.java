package com.lab240.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class Lab240 {

    public static final String APP_PREFERENCES = "Config", NAME = "Name", PASS = "Pass", DASHBOARDS = "Dashboards";
    public static String DASHBOARD_NAME = "Name", DASHBOARD_ID = "Id", DASHBOARD_GROUP = "Group";

    private static MQTT mqtt = null;
    private static final List<Dashboard> dashboards = new ArrayList<>();

    public static class Config{
        public String name;
        public String pass;

        public Config(String name, String pass, List<Dashboard> dashboards) {
            this.name = name;
            this.pass = pass;
            this.dashboards = dashboards;
        }

        public List<Dashboard> dashboards;
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

    public static List<Dashboard> getDashboards() {
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

    public static List<Dashboard> loadDashboards(Context c){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(!sp.contains(DASHBOARDS))
            return Collections.emptyList();
        return deserializeDashboards(sp.getString(DASHBOARDS, "[]"));
    }

    public static void saveDashboards(Context c, List<Dashboard> dashboards){
        SharedPreferences sp = c.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString(DASHBOARDS, serializeDashboards(dashboards));
        edit.apply();
    }

    public static List<Dashboard> deserializeDashboards(String s){
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(s);
        } catch (JSONException e) {
            return Collections.emptyList();
        }
        ArrayList<Dashboard> list = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject dashboard = jsonArray.getJSONObject(i);
                Dashboard db = new Dashboard(dashboard.getString(DASHBOARD_NAME), dashboard.getLong(DASHBOARD_ID), dashboard.getString(DASHBOARD_GROUP));
                list.add(db);
            } catch (JSONException ignored) {}
        }
        return list;
    }

    public static String serializeDashboards(List<Dashboard> dashboards){
        JSONArray arr = new JSONArray();
        for(Dashboard db : dashboards){
            try {
                JSONObject dbJson = new JSONObject();
                dbJson
                        .put(DASHBOARD_GROUP, db.getGroup())
                        .put(DASHBOARD_ID, db.getId())
                        .put(DASHBOARD_NAME, db.getName());
                arr.put(dbJson);
            } catch (JSONException ignored) {}
        }
        System.out.println(arr.toString());
        return arr.toString();
    }
}