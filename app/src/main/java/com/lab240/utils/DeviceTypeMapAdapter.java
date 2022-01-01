package com.lab240.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lab240.devices.DeviceTypes;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DeviceTypeMapAdapter implements JsonSerializer<Map<Long, DeviceTypes>>, JsonDeserializer<Map<Long, DeviceTypes>> {

    @Override
    public Map<Long, DeviceTypes> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<Long, DeviceTypes> res = new HashMap<>();
        if(!json.isJsonObject())
            return res;
        for (Map.Entry<String, JsonElement> i : json.getAsJsonObject().entrySet()) {
            try {
                DeviceTypes dt = context.deserialize(i.getValue(), DeviceTypes.class);
                if (dt != null) {
                    res.put(Long.parseLong(i.getKey()), dt);
                }
            }catch(IllegalStateException | ClassCastException | NumberFormatException ignored){}
        }
        return res;
    }

    @Override
    public JsonElement serialize(Map<Long, DeviceTypes> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jo = new JsonObject();
        for (Map.Entry<Long, DeviceTypes> i : src.entrySet()) {
            jo.add(context.serialize(i.getKey()).toString(), context.serialize(i.getValue()));
        }
        return jo;
    }
}
