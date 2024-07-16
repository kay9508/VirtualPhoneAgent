package com.sinyoung.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class JsonUtil {

    // 객체 -> json 문자열
    public static final String toJson(Object obj) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        return gson.toJson(obj);
    }

    // Map -> json 문자열
    public static final String toJson(Map<String, Object> mapData) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        return gson.toJson(mapData);
    }

    // json 문자열 -> clazz 객체
    public static final <T> T fromJson(String json, Class<T> valueType) {
        Gson gson = new GsonBuilder().
                setLenient()
                .create();
        return gson.fromJson(json, valueType);
    }

    public static final <T> T fromJson(String json, TypeToken<T> token) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        return gson.fromJson(json, token.getType());
    }

    // json 문자열 -> Map
    public static final Map<String, Object> fromJson(String json) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        return gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
    }
}