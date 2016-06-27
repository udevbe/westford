package org.westmalle.wayland.gbm;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateGbmPlatformFactory")
public class GbmPlatform implements Platform {

    private final long           drmDevice;
    private final int            drmFd;
    private final long           gbmDevice;
    private final GbmConnector[] gbmConnectors;

    GbmPlatform(final long drmDevice,
                final int drmFd,
                final long gbmDevice,
                final GbmConnector[] gbmConnectors) {
        this.drmDevice = drmDevice;
        this.drmFd = drmFd;
        this.gbmDevice = gbmDevice;
        this.gbmConnectors = gbmConnectors;
    }

    @Nonnull
    @Override
    public GbmConnector[] getConnectors() {
        return this.gbmConnectors;
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
