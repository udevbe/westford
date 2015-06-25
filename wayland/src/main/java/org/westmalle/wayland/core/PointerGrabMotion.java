package org.westmalle.wayland.core;

import org.westmalle.wayland.core.events.Motion;

import javax.annotation.Nonnegative;

@FunctionalInterface
public interface PointerGrabMotion {

    void motion(@Nonnegative final Motion motion);
}
