package org.westmalle.wayland.drm;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;

//TODO drm platform, remove all gbm dependencies
@AutoFactory(allowSubclasses = true,
             className = "PrivateGbmPlatformFactory")
public class DrmPlatform implements Platform {

    private final long           drmDevice;
    private final int            drmFd;
    private final long           gbmDevice;
    private final DrmConnector[] drmConnectors;

    DrmPlatform(final long drmDevice,
                final int drmFd,
                final long gbmDevice,
                final DrmConnector[] drmConnectors) {
        this.drmDevice = drmDevice;
        this.drmFd = drmFd;
        this.gbmDevice = gbmDevice;
        this.drmConnectors = drmConnectors;
    }

    @Nonnull
    @Override
    public DrmConnector[] getConnectors() {
        return this.drmConnectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }

    public long getDrmDevice() {
        return this.drmDevice;
    }

    public int getDrmFd() {
        return this.drmFd;
    }

    public long getGbmDevice() {
        return this.gbmDevice;
    }
}
