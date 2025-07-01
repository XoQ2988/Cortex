package me.xoq.cortex.module;

import com.google.gson.JsonObject;
import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.KeyEvent;
import me.xoq.cortex.module.modules.ProtectVillager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Modules {
    private Modules() { }

    private static final Map<String, Module> MODULES = new LinkedHashMap<>();

    public static void init() {
        register(new ProtectVillager());

        EventBus.register(KeyEvent.Press.class, Modules::onKeyPress);
    }

    private static void onKeyPress(KeyEvent.Press event) {
        for (Module module : getModules()) {
            if (event.getKey() == module.getKeybind()) module.toggle();
            break;  // only toggle one module per key
        }
    }

    public static void register(Module module) {
        MODULES.put(module.getName(), module);
    }

    public static Collection<Module> getModules() {
        return Collections.unmodifiableCollection(MODULES.values());
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonObject mods = new JsonObject();

        for (Module m : MODULES.values()) {
            mods.add(m.getName(), m.toJson());
        }
        root.add("modules", mods);

        return root;
    }

    public static void fromJson(JsonObject obj) {
        if (!obj.has("modules")) return;

        JsonObject mods = obj.getAsJsonObject("modules");
        for (Module module : MODULES.values()) {
            if (mods.has(module.getName())) module.fromJson(mods.getAsJsonObject(module.getName()));
        }
    }
}
