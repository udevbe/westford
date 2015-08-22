package org.westmalle.wayland.core.events;


import java.util.HashSet;
import java.util.Set;

public class Signal<U, T extends Slot<U>> {

    private final Set<T> slots = new HashSet<>();

    public void add(T slot) {
        this.slots.add(slot);
    }

    public void remove(T slot) {
        this.slots.remove(slot);
    }

    public void emit(U event) {
        new HashSet<>(this.slots).forEach(slot -> slot.handle(event));
    }
}
