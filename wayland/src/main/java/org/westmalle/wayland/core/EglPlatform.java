package org.westmalle.wayland.core;

import javax.annotation.Nonnull;

public interface EglPlatform extends Platform {
    default void begin() {}

    default void end() {}

    long getEglDisplay();

    long getEglSurface();

    long getEglContext();

    @Nonnull
    String getEglExtensions();
}
