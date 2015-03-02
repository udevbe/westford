package org.westmalle.wayland.output;

import org.westmalle.wayland.output.events.Motion;

@FunctionalInterface
public interface PointerGrabMotion {

    void motion(final Motion motion);
}
