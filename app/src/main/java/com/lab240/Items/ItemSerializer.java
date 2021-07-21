package com.lab240.Items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ItemSerializer implements JsonSerializer<Item>, JsonDeserializer<Item> {
    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonObject())
            return null;
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = json.getAsJsonObject();
        return gson.fromJson(json, (Type) Item.Type.values()[jsonObject.get("type").getAsInt()].cl);
    }

    @Override
    public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement jsonObject = context.serialize(src);
        JsonObject asJsonObject = jsonObject.getAsJsonObject();
        asJsonObject.addProperty("type", src.getType().ordinal());
        return asJsonObject;
    }
}
