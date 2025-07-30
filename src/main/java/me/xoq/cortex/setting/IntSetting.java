package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public class IntSetting extends Setting<Integer> {
    private final Integer min, max;

    private IntSetting(String name, String description, Integer defaultValue, Integer min, Integer max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive(value);
    }

    @Override
    protected void deserializeValue(JsonElement json) {
        this.value = json.getAsInt();
    }

    @Override
    public List<String> getSuggestions() {
        // Suggest default, min, and max (if present)
        List<String> s = new ArrayList<>();
        s.add(defaultValue.toString());
        if (min != null) s.add(min.toString());
        if (max != null) s.add(max.toString());
        return s;
    }

    @Override
    public Integer parseValue(String raw) {
        int v;
        try {
            v = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid integer");
        }
        if (min != null && v < min) {
            throw new IllegalArgumentException("Value must be ≥ " + min);
        }
        if (max != null && v > max) {
            throw new IllegalArgumentException("Value must be ≤ " + max);
        }
        return v;
    }

    public static class Builder {
        private String name, description = "";
        private Integer defaultValue;
        private Integer min = null, max = null;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder defaultValue(int def) {
            this.defaultValue = def;
            return this;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public IntSetting build() {
            if (name == null) {
                throw new IllegalStateException("IntSetting requires a name");
            }
            if (defaultValue == null) {
                throw new IllegalStateException("IntSetting requires a defaultValue");
            }

            if (min != null && defaultValue < min) {
                throw new IllegalStateException("Default value must be ≥ min");
            }
            if (max != null && defaultValue > max) {
                throw new IllegalStateException("Default value must be ≤ max");
            }

            return new IntSetting(name, description, defaultValue, min, max);
        }
    }
}
