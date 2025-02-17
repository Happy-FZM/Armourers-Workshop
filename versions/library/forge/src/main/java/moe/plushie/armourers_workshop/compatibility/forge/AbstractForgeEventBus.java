package moe.plushie.armourers_workshop.compatibility.forge;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.registry.IEventHandler;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.event.IModBusEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Available("[1.21, )")
public class AbstractForgeEventBus {

    private static final Map<IEventHandler.Priority, EventPriority> CONVERTER = Collections.immutableMap(builder -> {
        builder.put(IEventHandler.Priority.HIGHEST, EventPriority.HIGHEST);
        builder.put(IEventHandler.Priority.HIGH, EventPriority.HIGH);
        builder.put(IEventHandler.Priority.NORMAL, EventPriority.NORMAL);
        builder.put(IEventHandler.Priority.LOW, EventPriority.LOW);
        builder.put(IEventHandler.Priority.LOWEST, EventPriority.LOWEST);
    });

    private static final HashMap<Object, ArrayList<?>> LISTENERS = new HashMap<>();

    public static <E extends Event> void observer(Class<E> eventType, Consumer<E> handler) {
        observer(eventType, EventPriority.NORMAL, false, handler, event -> event);
    }

    public static <E extends Event, T> void observer(Class<E> eventType, EventPriority priority, boolean receiveCancelled, Consumer<T> handler, Function<E, T> transform) {
        var key = Collections.newList(eventType, priority, receiveCancelled);
        var handlers = LISTENERS.computeIfAbsent(key, key1 -> {
            var queue = new ArrayList<Consumer<T>>();
            Consumer<E> listener = event -> queue.forEach(element -> element.accept(transform.apply(event)));
            if (IModBusEvent.class.isAssignableFrom(eventType)) {
                AbstractForgeInitializer.getModEventBus().addListener(priority, receiveCancelled, eventType, listener);
            } else {
                AbstractForgeInitializer.getEventBus().addListener(priority, receiveCancelled, eventType, listener);
            }
            return queue;
        });
        handlers.add(Objects.unsafeCast(handler));
    }

    public static <E extends Event> IEventHandler<E> create(Class<E> eventType) {
        return (priority, receiveCancelled, handler) -> observer(eventType, CONVERTER.getOrDefault(priority, EventPriority.NORMAL), receiveCancelled, handler, event -> event);
    }
}
