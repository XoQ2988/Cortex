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
    private Modules() { }

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

    private static void onKeyPress(KeyEvent.Press event) {
        // don't toggle modules while any GUI is open
        if (mc.currentScreen != null) return;

        // capture logic
        if (pendingBind != null) {
            int key = event.getKey();

            if (key == GLFW.GLFW_KEY_ESCAPE) ChatUtils.info("§cBinding cancelled for " + pendingBind.getTitle());
            else {
                pendingBind.setKeybind(key);
                ChatUtils.info("§aBound §6" + pendingBind.getTitle() + " §ato §6" + Utils.keyToString(key) + "§r.");
            }
            pendingBind = null;
            event.cancel();
            Config.save();
            return;
        }

        // toggle logic
        for (Module module : getModules()) {
            int bind = module.getKeybind();
            if (bind < 0) continue;  // skip unbound
            if (event.getKey() == bind) {
                module.toggle();
                break;  // only toggle one module per key
            }
        }
    }

    private static void onKeyRelease(KeyEvent.Release event) {
        if (mc.currentScreen != null) return;

        for (Module module : getModules()) {
            int bind = module.getKeybind();
            // only momentary modules, and only if currently enabled
            if (module.isMomentary() && bind >= 0 && event.getKey() == bind && module.isEnabled()) {
                module.toggle();
                event.cancel();  // consume the release
                break;           // only one module per key
            }
        }
    }

    public static void register(Module module) {
        MODULES.put(module.getName(), module);
    }

    public static Collection<Module> getModules() {
        return Collections.unmodifiableCollection(MODULES.values());
    }

    public static Module get(String name) {
        return MODULES.get(name);
    }

    public static void startBinding(Module module) {
        pendingBind = module;
        ChatUtils.info("§ePress a key to bind " + module.getTitle() + "§r, or §cESC §rto cancel.");
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonObject mods = new JsonObject();

        for (Module m : MODULES.values()) {
            JsonObject mCfg = m.toJson();
            if (!mCfg.isEmpty()) mods.add("module." + m.getName(), mCfg);
        }
        if (!mods.isEmpty()) root.add("modules", mods);

        return root;
    }

    public static void fromJson(JsonObject obj) {
        if (!obj.has("modules")) return;

        JsonObject mods = obj.getAsJsonObject("modules");
        for (Module module : MODULES.values()) {
            if (mods.has("module." + module.getName()))
                module.fromJson(mods.getAsJsonObject("module." + module.getName()));
        }
    }
}
