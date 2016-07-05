package org.westmalle.wayland.x11.config;

import javax.annotation.Nonnull;

public interface X11PlatformConfig {
    @Nonnull
    String getDisplay();

    @Nonnull
    Iterable<X11ConnectorConfig> getX11ConnectorConfigs();
}
