package me.xoq.cortex.module;

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

        // TODO delete
        for (Module module : getModules()) {
            module.enable();
        }
    }

    public static void register(Module module) {
        MODULES.put(module.getName(), module);
    }

    public static Module get(String name) {
        return MODULES.get(name);
    }

    public static Collection<Module> getModules() {
        return Collections.unmodifiableCollection(MODULES.values());
    }
}
