package org.westmalle.wayland.drm.egl;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.drm.DrmPlatform;
import org.westmalle.wayland.drm.DrmPlatformFactory;

import javax.inject.Singleton;

@Module
public class GbmEglPlatformModule {

    @Provides
    @Singleton
    DrmPlatform createDrmPlatform(final DrmPlatformFactory drmPlatformFactory) {
        return drmPlatformFactory.create();
    }

    @Provides
    @Singleton
    Platform createPlatform(final GbmEglPlatformFactory gbmEglPlatformFactory) {
        return gbmEglPlatformFactory.create();
    }
}
