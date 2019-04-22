package com.superychen.monitor.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonUtil {

    private static final Gson GSON = new Gson();
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        return GSON.fromJson(json, cls);
    }

    public static <T> T fromJson(String json, Type t) {
        return GSON.fromJson(json, t);
    }

    public static String toPrettyJson(Object o) {
        return GSON_PRETTY.toJson(o);
    }

}
