package me.xoq.cortex.event;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A simple (static) event bus supporting:
 *  - functional listeners: EventBus.register(MyEvent.class, e -> …)
 *  - object listeners via @EventListener methods
 *  - cancellable events (any event that extends CancellableEvent)
 */
public class EventBus {
    private static final Map<Class<?>, CopyOnWriteArrayList<Subscription>> LISTENERS = new ConcurrentHashMap<>();
    private static final Map<Object, List<Subscription>> SUBSCRIPTIONS = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private EventBus() { }

    private record Subscription(
            Class<?> eventClass,
            Consumer<Object> consumer,
            EventListener.Priority priority
    ) {}

    // Functional registration
    public static <E> void register(Class<E> eventType, Consumer<? super E> listener) {
        @SuppressWarnings("unchecked")
        Subscription sub = new Subscription(
                eventType,
                (Consumer<? super Object>) listener,
                EventListener.Priority.NORMAL  // default
        );

        addSubscription(sub);
    }


    /** Unregister a previously registered functional listener. */
    public static <E> void unregister(Class<E> eventType, Consumer<? super E> listener) {
        @SuppressWarnings("unchecked")
        Consumer<? super Object> raw = (Consumer<? super Object>) listener;
        var list = LISTENERS.get(eventType);
        if (list != null) {
            list.removeIf(sub -> sub.consumer() == raw);
            if (list.isEmpty()) LISTENERS.remove(eventType);
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
            EventListener anno = method.getAnnotation(EventListener.class);
            if (anno == null) continue;

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

            Subscription sub = new Subscription(
                    eventType,
                    consumer,
                    anno.priority()
            );

            addSubscription(sub);
            subs.add(sub);
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
            if (list != null) {
                list.remove(sub);
                if (list.isEmpty()) {
                    LISTENERS.remove(sub.eventClass());
                }
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

        for (Subscription sub : list) {
            ((Consumer<E>) sub.consumer()).accept(event);
            if (event instanceof CancellableEvent ce && ce.isCancelled()) {
                break;
            }
        }
    }

    // Internal helper
    private static void addSubscription(Subscription sub) {
        var list = LISTENERS.computeIfAbsent(sub.eventClass(), k -> new CopyOnWriteArrayList<>());
        list.add(sub);
        list.sort(Comparator.comparing(Subscription::priority));
    }
}
