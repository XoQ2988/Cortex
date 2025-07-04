package me.xoq.cortex.module;

import com.google.gson.JsonObject;
import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.module.modules.DebugHUD;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.Utils;

import java.util.*;

public abstract class Module {
    private final String name;
    private final String title;
    private final String description;

    private boolean enabled = false;
    private boolean momentary = false;
    private int keybind = -1;

    private final List<Setting<?>> settings = new ArrayList<>();

    // Constructor
    protected Module(String name, String description) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
    }

    // Public API
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public final boolean isMomentary() { return momentary; }
    public int getKeybind() { return keybind; }

    public final void enable() {
        if (enabled) return;
        onEnable();
        EventBus.register(this);
        DebugHUD.registerLine("module."  + name + ".status", this::getStatus);
        enabled = true;
    }

    public final void disable() {
        if (!enabled) return;
        onDisable();
        EventBus.unregister(this);
        DebugHUD.unregisterLine("module."  + name + ".status");
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

    // Hooks
    protected void onEnable() { }
    protected void onDisable() { }
    protected String getStatus() { return null; }

    // Settings management
    public Collection<Setting<?>> getSettings() {
        return Collections.unmodifiableCollection(settings);
    }

    protected <T> Setting<T> registerSetting(Setting<T> setting) {
        settings.add(setting);
        return setting;
    }

    // Serialization
    public JsonObject toJson() {
        JsonObject configJson = new JsonObject();

        if (enabled)        configJson.addProperty("enabled", true);
        if (keybind != -1)  configJson.addProperty("keybind", keybind);
        if (momentary)      configJson.addProperty("momentary", true);

        if (!settings.isEmpty()) {
            JsonObject settingsJson = new JsonObject();
            for (Setting<?> s : settings) {
                if (!Objects.equals(s.get(), s.getDefault())) s.toJson(settingsJson);
            }
            if (!settingsJson.isEmpty()) configJson.add("settings", settingsJson);
        }

        return configJson;
    }

    public void fromJson(JsonObject configJson) {
        if (configJson.has("enabled") && configJson.get("enabled").getAsBoolean()) enable();
        if (configJson.has("keybind")) this.keybind = configJson.get("keybind").getAsInt();
        if (configJson.has("momentary")) this.momentary = configJson.get("momentary").getAsBoolean();

        if (!configJson.has("settings")) return;
        JsonObject settingsJson = configJson.getAsJsonObject("settings");
        for (Setting<?> s : settings) {
            s.fromJson(settingsJson);
        }
    }

}
