package org.westmalle.wayland.bootstrap;


import org.westmalle.wayland.x11.config.X11ConnectorConfig;
import org.westmalle.wayland.x11.config.X11PlatformConfig;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;

public class X11EglCompositorConfig implements X11PlatformConfig {

    X11EglCompositorConfig() {
    }

    @Nonnull
    @Override
    public String getDisplay() {
        return ":0";
    }

    @Nonnull
    @Override
    public Iterable<X11ConnectorConfig> getX11ConnectorConfigs() {
        return Arrays.asList(new X11ConnectorConfig() {
                                 @Override
                                 public int getWidth() {
                                     return 800;
                                 }

                                 @Override
                                 public int getHeight() {
                                     return 600;
                                 }

                                 @Override
                                 public int getX() {
                                     return 0;
                                 }

                                 @Override
                                 public int getY() {
                                     return 0;
                                 }
                             }
                ,
                             new X11ConnectorConfig() {
                                 @Override
                                 public int getWidth() {
                                     return 800;
                                 }

                                 @Override
                                 public int getHeight() {
                                     return 600;
                                 }

                                 @Override
                                 public int getX() {
                                     return 800;
                                 }

                                 @Override
                                 public int getY() {
                                     return 0;
                                 }
                             }
                            );
    }
}
