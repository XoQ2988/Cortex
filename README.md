# Cortex Fabric Mod

Cortex is a client‐side Fabric mod framework for Minecraft 1.21.7 that provides:

- A lightweight event bus and mixin hooks for block, entity & key events  
- A modular “feature” system with toggleable modules and per-module keybinds  
- A commands suite for listing, toggling, binding & inspecting modules  
- JSON-backed persistence of enabled state and keybinds  

---

## Requirements

- Minecraft **1.21.7**  
- Fabric Loader **0.16.14**  
- Fabric Loom **1.10-SNAPSHOT**  
- Java 21  
- Gradle (wrapper provided)  
- IntelliJ IDEA (or VS Code/Eclipse) with Fabric development plugins  

---

## Quick Start

1. **Clone & open** this project in your IDE.  
2. Run `./gradlew genSources` then refresh your IDE’s Gradle project.  
3. In IntelliJ, run the **runClient** configuration.

---

## Modules

All functionality lives in **modules** under `me.xoq.cortex.module`. Each extends the abstract `Module` class and toggles on/off via keybind or commands.

---

## Configuration

On first run, `cortex.json` is created in the Fabric `config/` folder:

```json
{
  "modules": {
    "protect-villager": {
      "enabled": true,
      "keybind": 86,
      "momentary": false
    }
  }
}
````

* **enabled**: module on/off state
* **keybind**: GLFW key code (use `.binds` to list them)

Changes are saved on toggle or at shutdown.

---

## Commands

All commands use the leading `.` prefix:

| Command                              | Description                                          |                                                   
|--------------------------------------|------------------------------------------------------| 
| **`.help`**                          | Show this help list                                  |                                                   
| **`.list`**                          | List all modules and their ON/OFF state              |                                                   
| **`.status`**                        | Same as `.list`                                      |                                                   
| **`.toggle <module> [on\off]`**      | Toggle or explicitly set a module’s enabled state    |
| **`.bind <module>`**                 | Enter “bind mode” — press a key (or ESC) to set bind |                                                    
| **`.binds`**                         | Show current key bindings for all modules            |                                                   
| **`.reset [<module>]`**              | Clear all binds (or for one module)                  |                                                   
| **`.reload`**                        | Reload config from disk                              |                                                   
| **`.enableall`** / **`.disableall`** | Enable or disable all modules                        |                                                   
| **`.info <module>`**                 | Show a module’s description, state, and bind         |

Example:

```text
.cortex> .list
[cortex] Modules (1):
[cortex]  • [ ON ] Protect Villager
```

---

## Adding a New Module

1. **Create** a class in `me.xoq.cortex.module.modules` extending `Module`.
2. **Register** it in `Modules.init()`.
3. **Override** `onEnable()` / `onDisable()` for your feature logic.
4. **Set** a default key with `setKeybind(GLFW_KEY_…)` or leave unbound.

---

## Event Bus & Mixins

* **EventBus**: thread-safe bus for any `CancellableEvent`.
* **Mixins** under `me.xoq.cortex.mixin` fire events like `BlockAttackEvent`, `EntityAttackEvent`, `KeyEvent`.
* **Listeners** annotate methods with `@EventListener` in any registered object (including modules).

---

## Utilities

* **ChatUtils**: send styled client-side messages to chat.
* **Utils.keyToString(keycode)**: pretty-print GLFW key codes.
* **StringUtils.titleize(name)**: convert `"protect-villager"` → `"Protect Villager"`.

---

## Contributing

1. Fork & branch.
2. Implement your feature or fix.
3. Add a module or command as needed.
4. Open a PR - happy to review!
