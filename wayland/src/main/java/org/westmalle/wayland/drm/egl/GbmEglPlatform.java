package org.westmalle.wayland.drm.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;


@AutoFactory(allowSubclasses = true,
             className = "PrivateGbmEglPlatformFactory")
public class GbmEglPlatform implements EglPlatform {


    private final long              gbmDevice;
    private final long              eglDisplay;
    private final long              eglContext;
    private final String            eglExtensions;
    @Nonnull
    private final GbmEglConnector[] gbmEglConnectors;

    GbmEglPlatform(final long gbmDevice,
                   final long eglDisplay,
                   final long eglContext,
                   final String eglExtensions,
                   @Nonnull final GbmEglConnector[] gbmEglConnectors) {
        this.gbmDevice = gbmDevice;
        this.eglDisplay = eglDisplay;
        this.eglContext = eglContext;
        this.eglExtensions = eglExtensions;
        this.gbmEglConnectors = gbmEglConnectors;
    }

    @Override
    public long getEglDisplay() {
        return this.eglDisplay;
    }

    @Override
    public long getEglContext() {
        return this.eglContext;
    }

    @Nonnull
    @Override
    public GbmEglConnector[] getConnectors() {
        return this.gbmEglConnectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglExtensions;
    }

    public long getGbmDevice() {
        return this.gbmDevice;
    }
}
