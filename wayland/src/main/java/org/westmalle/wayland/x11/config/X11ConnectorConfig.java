package org.westmalle.wayland.x11.config;


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface X11ConnectorConfig {

    @Nonnull
    String getName();

    @Nonnegative
    int getWidth();

    @Nonnegative
    int getHeight();

    int getX();

    int getY();
}
