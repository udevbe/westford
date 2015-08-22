package org.westmalle.wayland.core.events;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface Slot<T> {
    void handle(@Nonnull T event);
}
