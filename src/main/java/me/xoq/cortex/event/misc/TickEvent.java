package me.xoq.cortex.event.misc;

public class TickEvent {
    /**
     * Fired before the game processes a tick.
     */
    public static class Pre extends TickEvent { }

    /** Fired after the game has processed a tick. */
    public static class Post extends TickEvent { }
}
