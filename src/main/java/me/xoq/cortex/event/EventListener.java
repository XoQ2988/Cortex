package me.xoq.cortex.event;


import java.lang.annotation.*;

/**
 * Marks a method as an event-listener for the EventBus.
 * The method must:
 *  - Be non-static
 *  - Return void
 *  - Take exactly one parameter: The event type to listen for
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener { }
