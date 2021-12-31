package com.lab240.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.lab240.devices.Device;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeviceListAdapter implements JsonSerializer<List<Device>>, JsonDeserializer<List<Device>> {

    @Override
    public List<Device> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Device> res = new ArrayList<>();
        if(!json.isJsonArray())
            return res;
        for (JsonElement i : json.getAsJsonArray()) {
            try {
                Device t = context.deserialize(i, Device.class);
                if (t != null) {
                    res.add(t);
                }
            }catch(IllegalStateException | ClassCastException | NumberFormatException ignored){}
        }
        return res;
    }

    @Override
    public JsonElement serialize(List<Device> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jo = new JsonArray();
        for (Device i : src) {
            jo.add(context.serialize(i));
        }
        return jo;
    }
}
