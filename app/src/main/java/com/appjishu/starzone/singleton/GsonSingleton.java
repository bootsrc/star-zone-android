package com.appjishu.starzone.singleton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

public class GsonSingleton {
    private static final GsonSingleton ourInstance = new GsonSingleton();

    public static GsonSingleton getInstance() {
        return ourInstance;
    }

    private GsonSingleton() {
    }
    private static Gson gson;
    private static Gson dateGson;

    static {
        gson = new Gson();

        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        dateGson = builder.create();
    }

    public Gson getGson() {
        return gson;
    }

    public static Gson getDateGson() {
        return dateGson;
    }
}
