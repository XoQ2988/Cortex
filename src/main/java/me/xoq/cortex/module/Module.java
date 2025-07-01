package me.xoq.cortex.module;

import com.google.gson.JsonObject;
import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.utils.ChatUtils;
import org.lwjgl.glfw.GLFW;

public abstract class Module {
    private final String name;
    private final String description;
    private boolean enabled = false;
    private int keybind = GLFW.GLFW_KEY_UNKNOWN;

    protected Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKeybind() {
        return keybind;
    }

    public final void enable() {
        if (enabled) return;
        onEnable();
        EventBus.register(this);
        enabled = true;
    }

    public final void disable() {
        if (!enabled) return;
        onDisable();
        EventBus.unregister(this);
        enabled = false;
    }

    public final void toggle() {
        if (enabled) disable();
        else enable();
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    protected abstract void onEnable();
    protected abstract void onDisable();

    public JsonObject toJson() {
        JsonObject config = new JsonObject();

        config.addProperty("enabled", enabled);
        config.addProperty("keybind", keybind);

        return config;
    }

    public void fromJson(JsonObject obj) {
        ChatUtils.info("Loading settings for " + name);
        if (obj.has("enabled") && obj.get("enabled").getAsBoolean()) enable();
        if (obj.has("keybind")) this.keybind = obj.get("keybind").getAsInt();
    }
}
