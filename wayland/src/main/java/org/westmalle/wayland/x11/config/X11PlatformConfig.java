package org.westmalle.wayland.x11.config;

public interface X11PlatformConfig {

    String getDisplay();

    Iterable<X11ConnectorConfig> getX11ConnectorConfigs();
}
