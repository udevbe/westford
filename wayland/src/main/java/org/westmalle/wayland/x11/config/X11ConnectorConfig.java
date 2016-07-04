package org.westmalle.wayland.x11.config;


import javax.annotation.Nonnegative;

public interface X11ConnectorConfig {
    @Nonnegative
    int getWidth();

    @Nonnegative
    int getHeight();

    int getX();

    int getY();
}
