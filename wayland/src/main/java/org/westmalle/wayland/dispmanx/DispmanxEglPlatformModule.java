package org.westmalle.wayland.dispmanx;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;

import javax.inject.Singleton;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

@Module
public class DispmanxEglPlatformModule {

    @Provides
    @Singleton
    DispmanxPlatform createDispmanxPlatform(DispmanxPlatformFactory dispmanxPlatformFactory) {
        //FIXME from config
        return dispmanxPlatformFactory.create(DISPMANX_ID_HDMI);
    }

    @Provides
    @Singleton
    Platform createPlatform(DispmanxEglPlatformFactory dispmanxEglPlatformFactory) {
        return dispmanxEglPlatformFactory.create();
    }
}
