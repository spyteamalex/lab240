package com.lab240.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lab240.devices.Out;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class OutAdapter implements JsonSerializer<Out>, JsonDeserializer<Out> {
    private static final String NAME = "name", PATH = "path";
    @Override
    public Out deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonObject())
            return null;
        try {
            JsonObject jo = json.getAsJsonObject();
            return new Out(
                    jo.get(NAME).getAsString(),
                    (String[])context.deserialize(jo.get(PATH).getAsJsonArray(), String[].class)
            );
        }catch (IllegalStateException | ClassCastException e){
            return null;
        }
    }

    @Override
    public JsonElement serialize(Out src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jo = new JsonObject();
        jo.addProperty(NAME, src.getName());
        jo.add(PATH, context.serialize(src.getPath()));
        return jo;
    }
}
