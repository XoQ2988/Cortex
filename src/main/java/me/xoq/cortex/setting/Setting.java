package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class Setting<T> implements ISetting<T> {
    protected final String name, description;
    protected T value;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    @Override public String getName()        { return name; }
    @Override public String getDescription() { return description; }
    @Override public T get()                 { return value; }
    @Override public void set(T value)       { this.value = value; }

    @Override
    public void toJson(JsonObject root) {
        root.add(name, serializeValue());
    }
    @Override
    public void fromJson(JsonObject root) {
        if (root.has(name)) {
            deserializeValue(root.get(name));
        }
    }

    protected abstract JsonElement serializeValue();
    protected abstract void deserializeValue(JsonElement json);
}
