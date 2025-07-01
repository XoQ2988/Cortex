package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public class BoolSetting extends Setting<Boolean> {
    private BoolSetting(String name, String description, Boolean defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public List<String> getSuggestions() {
        return List.of("true", "false");
    }

    @Override
    public Boolean parseValue(String raw) {
        String lower = raw.toLowerCase();
        if (lower.equals("true") || lower.equals("false")) {
            return Boolean.parseBoolean(lower);
        }
        throw new IllegalArgumentException("Must be true or false");
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive(value);
    }

    @Override
    protected void deserializeValue(JsonElement json) {
        this.value = json.getAsBoolean();
    }

    public static class Builder {
        private String name;
        private String description = "";
        private boolean defaultValue = true;

        public Builder name(String s) {
            this.name = s; return this;
        }

        public Builder description(String s) {
            this.description = s; return this;
        }

        public Builder defaultValue(Boolean b) {
            this.defaultValue = b; return this;
        }

        public BoolSetting build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("BoolSetting requires a name");
            }
            return new BoolSetting(name, description, defaultValue);
        }
    }
}
