package me.xoq.cortex.module;

import com.google.gson.JsonObject;
import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.Utils;

public abstract class Module {
    private final String name;
    private final String title;
    private final String description;
    private boolean enabled = false;
    private boolean momentary = false;
    private int keybind = -1;

    protected Module(String name, String description) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
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

    public final boolean isMomentary() {
        return momentary;
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
        ChatUtils.info("Toggled §e" + title + "§r §" + (enabled ? "aON" : "cOFF") + "§r.");
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public final void setMomentary(boolean momentary) {
        this.momentary = momentary;
    }

    protected void onEnable() { }
    protected void onDisable() { }

    public JsonObject toJson() {
        JsonObject config = new JsonObject();

        config.addProperty("enabled", enabled);
        config.addProperty("keybind", keybind);
        config.addProperty("momentary", momentary);

        return config;
    }

    public void fromJson(JsonObject obj) {
        if (obj.has("enabled") && obj.get("enabled").getAsBoolean()) enable();
        if (obj.has("keybind")) this.keybind = obj.get("keybind").getAsInt();
        if (obj.has("momentary")) this.momentary = obj.get("momentary").getAsBoolean();
    }

}
