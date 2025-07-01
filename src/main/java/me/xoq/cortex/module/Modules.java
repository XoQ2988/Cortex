package me.xoq.cortex.module;

import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.KeyEvent;
import me.xoq.cortex.module.modules.ProtectVillager;
import org.lwjgl.glfw.GLFW;

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
}
