package org.westmalle.wayland.output;

import org.westmalle.wayland.output.events.Motion;

import javax.media.nativewindow.util.PointImmutable;

@FunctionalInterface
public interface PointerGrabMotion {

    void motion(final PointerDevice pointerDevice,
                final PointImmutable firstRelativePosition,
                final Motion motion);
}
