package org.westmalle.wayland.core;

import javax.annotation.Nonnull;

public interface EglPlatform extends Platform {
    default void begin() {}

    default void end() {}

    long getEglDisplay();

    long getEglContext();

    @Nonnull
    @Override
    EglConnector[] getConnectors();

    @Nonnull
    String getEglExtensions();
}
