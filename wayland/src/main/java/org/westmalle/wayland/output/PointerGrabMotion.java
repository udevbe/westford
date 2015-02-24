package org.westmalle.wayland.output;

import org.westmalle.wayland.output.events.Motion;

@FunctionalInterface
public interface PointerGrabMotion {

    void motion(final PointerDevice pointerDevice,
                final Motion motion);
}
