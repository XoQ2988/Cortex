package me.xoq.cortex.setting;

import com.google.gson.JsonObject;

public interface ISetting<T> {
    String getName();
    String getDescription();
    T get();
    void set(T value);

    void toJson(JsonObject root);
    void fromJson(JsonObject root);
}
