package org.westmalle.wayland.bootstrap;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.x11.config.X11PlatformConfig;

@Module
public class ConfigModule {

    @Provides
    X11PlatformConfig provideX11PlatformConfig() {
        return new X11EglCompositorConfig();
    }
}
