package org.westmalle.wayland.dispmanx.egl;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.dispmanx.DispmanxPlatform;
import org.westmalle.wayland.dispmanx.DispmanxPlatformFactory;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

@Module
public class DispmanxEglPlatformModule {

    @Provides
    @Singleton
    DispmanxPlatform createDispmanxPlatform(final DispmanxPlatformFactory dispmanxPlatformFactory) {
        //FIXME from config
        return dispmanxPlatformFactory.create(DISPMANX_ID_HDMI);
    }

    @Provides
    @Singleton
    Platform createPlatform(@Nonnull final DispmanxEglPlatformFactory dispmanxEglPlatformFactory) {
        return dispmanxEglPlatformFactory.create();
    }
}
