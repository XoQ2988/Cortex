package me.xoq.cortex.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * A simple (static) event bus supporting:
 *  - functional listeners: EventBus.register(MyEvent.class, e -> …)
 *  - object listeners via @EventListener methods
 *  - cancellable events (any event that extends CancellableEvent)
 */
public class EventBus {
    private EventBus() { }

    // Map from event type -> list of listeners for exactly that type
    private static final Map<Class<?>, CopyOnWriteArrayList<Consumer<? super Object>>> LISTENERS =
            new ConcurrentHashMap<>();

    //T rack object-based subscriptions so we can unregister later
    private static final Map<Object, List<Subscription>> SUBSCRIPTIONS = new ConcurrentHashMap<>();

    private record Subscription(Class<?> eventClass, Consumer<?> consumer) {}

    // Functional registration

    /**
     * Register a functional listener for exactly {@code eventType}
     */
    public static <E> void register(Class<E> eventType, Consumer<? super E> listener) {
        // Store as Consumer<? super Object> to sidestep generic array issues
        @SuppressWarnings("unchecked")
        Consumer<? super Object> raw = (Consumer<? super Object>) listener;
        LISTENERS
            .computeIfAbsent(eventType, cls -> new CopyOnWriteArrayList<>())
            .add(raw);
    }


    /** Unregister a previously registered functional listener. */
    public static <E> void unregister(Class<E> eventClass, Consumer<? super E> listener) {
        @SuppressWarnings("unchecked")
        Consumer<? super Object> raw = (Consumer<? super Object>) listener;
        var list = LISTENERS.get(eventClass);
        if (list != null && list.remove(raw) && list.isEmpty()) {
            LISTENERS.remove(eventClass);
        }
    }

    // Object based registration via @EventListener

    /**
     * Scan all @EventListener methods on listenerObj and register them.
     * Methods must be:
     *  - void return
     *  - exactly one parameter (the event type)
     */
    public static void register(Object listenerObj) {
        List<Subscription> subs = new ArrayList<>();
        for (Method method : listenerObj.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventListener.class)) continue;
            if (method.getReturnType() != void.class || method.getParameterCount() != 1) {
                throw new IllegalArgumentException("@" + EventListener.class.getSimpleName()
                        + " on " + method + " must be void with exactly one param");
            }

            @SuppressWarnings("unchecked")
            Class<Object> eventType = (Class<Object>) method.getParameterTypes()[0];
            method.setAccessible(true);

            Consumer<Object> consumer = event -> {
                try {
                    method.invoke(listenerObj, event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to invoke event listener " + method, e);
                }
            };

            // use the functional register
            register(eventType, consumer);
            subs.add(new Subscription(eventType, consumer));
        }
        if (!subs.isEmpty()) {
            SUBSCRIPTIONS.put(listenerObj, subs);
        }
    }

    //** Unregister all @EventListener methods on the given object */
    public static void unregister(Object listenerObj) {
        var subs = SUBSCRIPTIONS.remove(listenerObj);
        if (subs == null) return;

        for (Subscription sub : subs) {
            var list = LISTENERS.get(sub.eventClass());
            if (list != null && list.remove(sub.consumer()) && list.isEmpty()) {
                LISTENERS.remove(sub.eventClass());
            }
        }
    }

    // Event dispatch

    /**
     * Fire an event to ALL listeners registered on the event's class, its superclass and its interfaces.
     * If the event is a CancellableEvent and one listener calls cancel, we stop dispatching further.
     */
    @SuppressWarnings("unchecked")
    public static <E> void fire(E event) {
        var list = LISTENERS.get(event.getClass());
        if (list == null) return;

        for (Consumer<?> raw : list) {
            ((Consumer<E>) raw).accept(event);

            if (event instanceof CancellableEvent cancellable && cancellable.isCancelled()) {
                break;
            }
        }
    }
}
