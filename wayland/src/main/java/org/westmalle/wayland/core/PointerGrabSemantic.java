package org.westmalle.wayland.core;


public interface PointerGrabSemantic {
    default void hasGrab(){}
    default void grabLost(){}
}
