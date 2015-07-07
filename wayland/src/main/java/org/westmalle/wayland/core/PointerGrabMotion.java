package org.westmalle.wayland.core;

import org.westmalle.wayland.core.events.Motion;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface PointerGrabMotion {

    void motion(@Nonnull final Motion motion);
}
