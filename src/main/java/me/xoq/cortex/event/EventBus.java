package me.xoq.cortex.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class EventBus {
    private EventBus() { }

    // underlying functional listeners
    private static final Map<Class<?>, CopyOnWriteArrayList<Consumer>> LISTENERS = new ConcurrentHashMap<>();

    // track object-based subscriptions so we can unregister later
    private static final Map<Object, List<Subscription>> SUBSCRIPTIONS = new ConcurrentHashMap<>();

    private record Subscription(Class<?> eventClass, Consumer<?> consumer) {}


    /** Functional‐style registration. */
    public static <E> void register(Class<E> eventClass, Consumer<? super E> listener) {
        @SuppressWarnings("unchecked")
        Consumer<? super Object> raw = (Consumer<? super Object>) listener;
        LISTENERS
                .computeIfAbsent(eventClass, cls -> new CopyOnWriteArrayList<>())
                .add(raw);
    }


    /** Functional‐style unregistration. */
    public static <E> void unregister(Class<E> eventClass, Consumer<? super E> listener) {
        @SuppressWarnings("unchecked")
        Consumer<? super Object> raw = (Consumer<? super Object>) listener;
        var list = LISTENERS.get(eventClass);
        if (list != null) {
            list.remove(raw);
            if (list.isEmpty()) {
                LISTENERS.remove(eventClass);
            }
        }
    }


    public static void register(Object listenerObj) {
        List<Subscription> subs = new ArrayList<>();
        for (Method m : listenerObj.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(EventListener.class)) continue;
            if (m.getReturnType() != void.class || m.getParameterCount() != 1) {
                throw new IllegalArgumentException("@" + EventListener.class.getSimpleName()
                        + " on " + m + " must be void with exactly one param");
            }
            Class<?> eventClass = m.getParameterTypes()[0];
            m.setAccessible(true);

            Consumer<Object> consumer = event -> {
                try {
                    m.invoke(listenerObj, event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to invoke event listener " + m, e);
                }
            };

            // use the functional register
            register(eventClass, consumer);
            subs.add(new Subscription(eventClass, consumer));
        }
        if (!subs.isEmpty()) {
            SUBSCRIPTIONS.put(listenerObj, subs);
        }
    }

    public static void unregister(Object listenerObj) {
        var subs = SUBSCRIPTIONS.remove(listenerObj);
        if (subs == null) return;

        for (var sub : subs) {
            var list = LISTENERS.get(sub.eventClass());
            if (list != null) {
                list.remove(sub.consumer());
                if (list.isEmpty()) {
                    LISTENERS.remove(sub.eventClass());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> void fire(E event) {
        var list = LISTENERS.get(event.getClass());
        if (list == null) return;

        for (Consumer<?> raw : list) {
            ((Consumer<E>) raw).accept(event);

            if (event instanceof ICancellable cancellable && cancellable.isCancelled()) {
                break;
            }
        }
    }
}
