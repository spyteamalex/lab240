package com.lab240.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lab240.devices.Device;
import com.lab240.devices.Hint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HintListAdapter implements JsonSerializer<List<Hint>>, JsonDeserializer<List<Hint>> {

    @Override
    public List<Hint> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Hint> res = new ArrayList<>();
        if(!json.isJsonArray())
            return res;
        for (JsonElement i : json.getAsJsonArray()) {
            try {
                Hint t = context.deserialize(i, Hint.class);
                if (t != null) {
                    res.add(t);
                }
            }catch(IllegalStateException | ClassCastException | NumberFormatException ignored){}
        }
        return res;
    }

    @Override
    public JsonElement serialize(List<Hint> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jo = new JsonArray();
        for (Hint i : src) {
            jo.add(context.serialize(i));
        }
        return jo;
    }
}
