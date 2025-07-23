package me.xoq.cortex.event;

public class CancellableEvent {
    private boolean cancelled = false;

    /**
     * Mark this event as cancelled or not.
     * @param cancelled true to cancel, false to uncancel
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Convenience method to cancel the event.
     */
    public void cancel() {
        setCancelled(true);
    }

    /**
     * @return true if this event has been cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
}