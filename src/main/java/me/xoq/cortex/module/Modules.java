package me.xoq.cortex.module;

import com.google.gson.JsonObject;
import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.misc.KeyEvent;
import me.xoq.cortex.module.modules.*;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.Config;
import me.xoq.cortex.util.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static me.xoq.cortex.CortexClient.mc;

public final class Modules {
    private static final Map<String, Module> MODULES = new LinkedHashMap<>();
    private static Module pendingBind = null;

    public static void init() {
        register(new AntiBreak());
        register(new AutoFarm());
        register(new AutoSneak());
        register(new AutoTool());
        register(new BetterTooltips());
        register(new BreakDelay());
        register(new Collisions());
        register(new MLGAssist());
        register(new DebugHUD());

        EventBus.register(KeyEvent.Press.class, Modules::onKeyPress);
        EventBus.register(KeyEvent.Release.class, Modules::onKeyRelease);
    }

    // Key event handlers
    private static void onKeyPress(KeyEvent.Press event) {
        if (mc.currentScreen != null) return;  // don't toggle modules while any GUI is open

        if (captureBind(event.getKey())) {
            event.cancel();
            Config.save();
            return;
        }

        MODULES.values().stream()
                .filter(mod -> mod.getKeybind() >= 0 && mod.getKeybind() == event.getKey())
                .findFirst()
                .ifPresent(Module::toggle);
    }

    private static void onKeyRelease(KeyEvent.Release event) {
        if (mc.currentScreen != null) return;

        MODULES.values().stream()
                .filter(Module::isMomentary)
                .filter(mod -> mod.getKeybind() >= 0 && mod.getKeybind() == event.getKey() && mod.isEnabled())
                .findFirst()
                .ifPresent(mod -> {
                    mod.toggle();
                    event.cancel();
                });
    }

    private static boolean captureBind(int key) {
        if (pendingBind == null) return false;

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            ChatUtils.info("§cBinding cancelled for " + pendingBind.getTitle());
        } else {
            pendingBind.setKeybind(key);
            ChatUtils.info("§aBound §6" + pendingBind.getTitle()
                    + " §ato §6" + Utils.keyToString(key) + "§r.");
        }
        pendingBind = null;
        return true;
    }

    // Public API
    public static void register(Module module) {
        MODULES.put(module.getName(), module);
    }

    public static void startBinding(Module module) {
        pendingBind = module;
        ChatUtils.info("§ePress a key to bind " + module.getTitle() + "§r, or §cESC §rto cancel.");
    }

    public static Collection<Module> getModules() {
        return Collections.unmodifiableCollection(MODULES.values());
    }

    public static Module get(String name) {
        return MODULES.get(name);
    }

    // Serialization
    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonObject mods = new JsonObject();

        MODULES.values().forEach(mod -> {
            JsonObject mCfg = mod.toJson();
            if (!mCfg.isEmpty()) {
                mods.add("module." + mod.getName(), mCfg);
            }
        });

        if (!mods.isEmpty()) {
            root.add("modules", mods);
        }
        return root;
    }

    public static void fromJson(JsonObject obj) {
        if (!obj.has("modules")) return;

        JsonObject mods = obj.getAsJsonObject("modules");
        for (Module mod : MODULES.values()) {
            if (mods.has("module." + mod.getName()))
                mod.fromJson(mods.getAsJsonObject("module." + mod.getName()));
        }
    }
}
