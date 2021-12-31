package com.lab240.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.devices.Out;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class DeviceTypeAdapter implements JsonSerializer<DeviceTypes>, JsonDeserializer<DeviceTypes> {
    private static final String OUTS = "outs", RELAYS = "relays", NAME = "name", SETTER_HINTS = "setterHints", GETTER_HINTS = "getterHints", ID = "id";
    @Override
    public JsonElement serialize(DeviceTypes src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jo = new JsonObject();
        jo.add(OUTS, context.serialize(src.outs));
        jo.add(RELAYS, context.serialize(src.relays));
        jo.addProperty(NAME, src.name);
        jo.addProperty(ID, src.id);
        jo.add(SETTER_HINTS, context.serialize(src.setterHints));
        jo.add(GETTER_HINTS, context.serialize(src.getterHints));
        return jo;
    }

    @Override
    public DeviceTypes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonObject())
            return null;
        try {
            JsonObject jo = json.getAsJsonObject();
            return new DeviceTypes(
                    jo.get(NAME).getAsString(),
                    jo.get(ID).getAsLong(),
                    context.deserialize(jo.get(RELAYS), new TypeToken<Set<Out>>(){}.getType()),
                    context.deserialize(jo.get(OUTS), new TypeToken<Set<Out>>(){}.getType()),
                    context.deserialize(jo.get(SETTER_HINTS), new TypeToken<List<String>>(){}.getType()),
                    context.deserialize(jo.get(GETTER_HINTS), new TypeToken<List<String>>(){}.getType())
            );
        }catch (IllegalStateException | ClassCastException e){
            return null;
        }
    }
}
