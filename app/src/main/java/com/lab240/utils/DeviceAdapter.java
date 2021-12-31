package com.lab240.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.lab240.devices.Device;
import com.lab240.devices.Out;

import java.lang.reflect.Type;
import java.util.Set;

public class DeviceAdapter implements JsonSerializer<Device>, JsonDeserializer<Device> {
    private static final String IDENTIFICATOR = "identificator", NAME = "name", GROUP = "group", TYPE = "type", OUTS = "outs", RELAYS = "relays";
    private static long id;
    static {
        id = System.currentTimeMillis();
    }
    @Override
    public Device deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonObject())
            return null;
        try {
            JsonObject jo = json.getAsJsonObject();
            return new Device(
                    jo.get(NAME).getAsString(),
                    jo.get(IDENTIFICATOR).getAsString(),
                    jo.get(GROUP).getAsString(),
                    id++,
                    jo.get(TYPE).getAsLong(),
                    context.deserialize(jo.get(RELAYS),new TypeToken<Set<Out>>(){}.getType()),
                    context.deserialize(jo.get(OUTS),new TypeToken<Set<Out>>(){}.getType())
            );
        }catch (IllegalStateException | ClassCastException e){
            return null;
        }
    }

    @Override
    public JsonElement serialize(Device src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jo = new JsonObject();
        jo.addProperty(IDENTIFICATOR, src.getIdentificator());
        jo.addProperty(NAME, src.getName());
        jo.addProperty(GROUP, src.getGroup());
        jo.addProperty(TYPE, src.getType());
        jo.add(OUTS, context.serialize(src.getOuts(), new TypeToken<Set<Out>>(){}.getType()));
        jo.add(RELAYS, context.serialize(src.getRelays(), new TypeToken<Set<Out>>(){}.getType()));
        return null;
    }
}
