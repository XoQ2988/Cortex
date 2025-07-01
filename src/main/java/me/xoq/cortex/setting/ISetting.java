package me.xoq.cortex.setting;

import com.google.gson.JsonObject;

import java.util.List;

public interface ISetting<T> {
    String getName();
    String getTitle();
    String getDescription();
    T get();
    void set(T value);
    void resetToDefault();

    List<String> getSuggestions();
    T parseValue(String raw);

    void toJson(JsonObject root);
    void fromJson(JsonObject root);
}
