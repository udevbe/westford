package org.westmalle.wayland.core;

public interface EglPlatform extends Platform {
    default void begin() {}

    default void end() {}

    long getEglDisplay();
}
