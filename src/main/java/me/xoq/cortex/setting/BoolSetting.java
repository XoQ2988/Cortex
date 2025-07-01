package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting<Boolean> {
    private BoolSetting(String name, String description, Boolean defaultValue) {
        super(name, description, defaultValue);
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
