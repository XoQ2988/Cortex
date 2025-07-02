package me.xoq.cortex.event.misc;

import me.xoq.cortex.event.CancellableEvent;

/**
 * Fired for each raw keyboard event.
 * Listeners may cancel to prevent further processing
 */
public class KeyEvent extends CancellableEvent {
    private final long window;
    private final int key;
    private final int scancode;
    private final int modifiers;

    public KeyEvent(long window, int key, int scancode, int modifiers) {
        this.window = window;
        this.key = key;
        this.scancode = scancode;
        this.modifiers = modifiers;
    }

    public long getWindow() { return window; }
    public int  getKey() { return key; }
    public int  getScancode() { return scancode; }
    public int  getModifiers() { return modifiers; }

    // Fired on GLFW_PRESS
    public static final class Press extends KeyEvent {
        public Press(long window, int key, int scancode, int modifiers) {
            super(window, key, scancode, modifiers);
        }
    }

    // Fired on GLFW_RELEASE
    public static final class Release extends KeyEvent {
        public Release(long window, int key, int scancode, int modifiers) {
            super(window, key, scancode, modifiers);
        }
    }

    // Fired on GLFW_REPEAT
    public static final class Repeat extends KeyEvent {
        public Repeat(long window, int key, int scancode, int modifiers) {
            super(window, key, scancode, modifiers);
        }
    }
}
