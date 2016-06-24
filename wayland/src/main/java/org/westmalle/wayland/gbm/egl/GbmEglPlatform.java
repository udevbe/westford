package org.westmalle.wayland.gbm.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;

@AutoFactory
public class GbmEglPlatform implements EglPlatform {


    GbmEglPlatform() {
    }

    @Override
    public void begin() {

    }

    @Override
    public void end() {

    }

    @Override
    public long getEglDisplay() {
        return 0;
    }

    @Override
    public long getEglContext() {
        return 0;
    }

    @Nonnull
    @Override
    public GbmEglConnector[] getConnectors() {
        return null;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {

    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return null;
    }
}
