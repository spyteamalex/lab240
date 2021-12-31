package com.lab240.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lab240.devices.Out;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class OutSetAdapter implements JsonSerializer<Set<Out>>, JsonDeserializer<Set<Out>> {

    @Override
    public Set<Out> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Set<Out> res = new HashSet<>();
        if(!json.isJsonArray())
            return res;
        for (JsonElement i : json.getAsJsonArray()) {
            try {
                Out out = context.deserialize(i, Out.class);
                if (out != null) {
                    res.add(out);
                }
            }catch(IllegalStateException | ClassCastException | NumberFormatException ignored){}
        }
        return res;
    }

    @Override
    public JsonElement serialize(Set<Out> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jo = new JsonArray();
        for (Out i : src) {
            jo.add(context.serialize(i));
        }
        return jo;
    }
}
