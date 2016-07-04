package org.westmalle.wayland.x11.config;


import java.util.List;

public interface X11PlatformConfig {

    String getDisplay();

    List<X11ConnectorConfig> getX11ConnectorConfigs();
}
