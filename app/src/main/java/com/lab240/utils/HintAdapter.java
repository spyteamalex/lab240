package com.lab240.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lab240.devices.Hint;

import java.lang.reflect.Type;

public class HintAdapter implements JsonSerializer<Hint>, JsonDeserializer<Hint> {
    private static final String CMD = "cmd", HINT = "hint";
    @Override
    public Hint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonObject())
            return null;
        try {
            JsonObject jo = json.getAsJsonObject();
            return new Hint(
                    jo.get(CMD).getAsString(),
                    jo.get(HINT).getAsString());
        }catch (IllegalStateException | ClassCastException e){
            return null;
        }
    }

    @Override
    public JsonElement serialize(Hint src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jo = new JsonObject();
        jo.addProperty(CMD, src.getCmd());
        jo.addProperty(HINT, src.getHint());
        return jo;
    }
}
