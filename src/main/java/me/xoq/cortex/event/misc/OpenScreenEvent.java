package me.xoq.cortex.event.misc;

import me.xoq.cortex.event.CancellableEvent;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;


/**
 * Fired around screen-open calls.
 *  - Pre is
 */
public class OpenScreenEvent {
    public static class Pre extends CancellableEvent {
        private final Screen screen;

        public Pre(Screen screen) {
            super();
            this.screen = screen;
        }

        /** The screen that is about to be opened. */
        public Screen getScreen() {
            return screen;
        }
    }

    /** After the screen has been opened—always fires. */
    public static class Post {
        private final Screen newScreen;
        private final Screen previousScreen;

        public Post(Screen newScreen, Screen previousScreen) {
            this.newScreen = newScreen;
            this.previousScreen = previousScreen;
        }

        public Screen getNewScreen() {
            return newScreen;
        }

        @Nullable
        public Screen getPreviousScreen() {
            return previousScreen;
        }
    }
}
