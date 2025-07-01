package me.xoq.cortex.event;

public interface ICancellable {
    // Mark this event as cancelled
    void setCancelled(boolean cancelled);

    // Cancels the event
    default void cancel() {setCancelled(true);}

    // Return true if this event was cancelled
    boolean isCancelled();
}
