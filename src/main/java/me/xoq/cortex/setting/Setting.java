package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.xoq.cortex.util.Utils;

import java.util.List;

public abstract class Setting<T> implements ISetting<T> {
    protected final String name, title, description;
    protected T value, defaultValue;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    @Override public String getName()        { return name; }
    @Override public String getTitle()       { return title; }
    @Override public String getDescription() { return description; }
    @Override public T getDefault()     { return defaultValue; }
    @Override public T get()                 { return value; }
    @Override public void set(T value)       { this.value = value; }
    @Override public void resetToDefault()   { this.value = defaultValue; }

    @Override
    public List<String> getSuggestions() {
        return List.of();
    }

    @Override
    public T parseValue(String raw) {
        throw new UnsupportedOperationException("parseValue not implemented");
    }

    @Override
    public void toJson(JsonObject root) {
        root.add("setting." + name, serializeValue());
    }

    @Override
    public void fromJson(JsonObject root) {
        if (root.has("setting." + name)) {
            deserializeValue(root.get("setting." + name));
        }
    }

    protected abstract JsonElement serializeValue();
    protected abstract void deserializeValue(JsonElement json);
}
