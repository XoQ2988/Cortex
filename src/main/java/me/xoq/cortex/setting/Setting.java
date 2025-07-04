package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.xoq.cortex.util.Utils;

import java.util.List;

public abstract class Setting<T> {
    protected final String name;
    protected final String title;
    protected final String description;

    protected T value;
    protected final T defaultValue;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    // Accessors
    public String getName()        { return name; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public T getDefault()          { return defaultValue; }
    public T get()                 { return value; }
    public void set(T value)       { this.value = value; }
    public void resetToDefault()   { this.value = defaultValue; }

    public List<String> getSuggestions() {
        return List.of();
    }

    public T parseValue(String raw) {
        throw new UnsupportedOperationException("parseValue not implemented");
    }

    // Serialization
    public void toJson(JsonObject root) {
        root.add("setting." + name, serializeValue());
    }

    public void fromJson(JsonObject root) {
        if (root.has("setting." + name)) {
            deserializeValue(root.get("setting." + name));
        }
    }

    protected abstract JsonElement serializeValue();
    protected abstract void deserializeValue(JsonElement json);
}
