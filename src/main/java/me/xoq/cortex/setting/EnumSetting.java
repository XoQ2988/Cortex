package me.xoq.cortex.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EnumSetting<T extends Enum<T>> extends Setting<T>{
    private final Class<T> enumClass;

    private EnumSetting(String name, String description, Class<T> enumClass, T defaultValue) {
        super(name, description, defaultValue);
        this.enumClass = enumClass;
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive(value.name());
    }

    @Override
    protected void deserializeValue(JsonElement json) {
        // Read the string, uppercase it, and convert to the enum
        String str = json.getAsString();
        try {
            this.value = Enum.valueOf(enumClass, str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new JsonSyntaxException(
                    "Unknown " + enumClass.getSimpleName() + " value: " + str);
        }
    }

    @Override
    public List<String> getSuggestions() {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> e.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    @Override
    public T parseValue(String raw) {
        String key = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Enum.valueOf(enumClass, key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid " + enumClass.getSimpleName() + ": " + raw);
        }
    }

    public static class Builder<T extends Enum<T>> {
        private String name;
        private String description = "";
        private Class<T> enumClass;
        private T defaultValue;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<T> enumClass(Class<T> enumClass) {
            this.enumClass = enumClass;
            return this;
        }

        public Builder<T> defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public EnumSetting<T> build() {
            if (name == null || enumClass == null || defaultValue == null) {
                throw new IllegalStateException("EnumSetting requires name, enumClass, and defaultValue");
            }
            return new EnumSetting<>(name, description, enumClass, defaultValue);
        }
    }
}
