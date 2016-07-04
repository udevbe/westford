package org.westmalle.wayland.bootstrap;


import org.westmalle.wayland.x11.config.X11ConnectorConfig;
import org.westmalle.wayland.x11.config.X11PlatformConfig;

import java.util.Collections;
import java.util.List;

public class X11EglCompositorConfig implements X11PlatformConfig {

    X11EglCompositorConfig() {
    }

    @Override
    public String getDisplay() {
        return ":0";
    }

    @Override
    public List<X11ConnectorConfig> getX11ConnectorConfigs() {
        return Collections.singletonList(new X11ConnectorConfig() {
            @Override
            public int getWidth() {
                return 1024;
            }

            @Override
            public int getHeight() {
                return 768;
            }

            @Override
            public int getX() {
                return 0;
            }

            @Override
            public int getY() {
                return 0;
            }
        });
    }
}
