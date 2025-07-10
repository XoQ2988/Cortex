package me.xoq.cortex.event.misc;

import me.xoq.cortex.event.CancellableEvent;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends CancellableEvent {
    private final Screen screen;

    public OpenScreenEvent (Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
