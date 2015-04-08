package org.westmalle.wayland.output;

import org.westmalle.wayland.output.events.Motion;

import javax.annotation.Nonnegative;

@FunctionalInterface
public interface PointerGrabMotion {

    void motion(@Nonnegative final Motion motion);
}
