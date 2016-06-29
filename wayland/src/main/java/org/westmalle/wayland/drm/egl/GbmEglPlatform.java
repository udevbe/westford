package org.westmalle.wayland.drm.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;

//TODO put all gbm/egl specifics here
@AutoFactory
public class GbmEglPlatform implements EglPlatform {


    GbmEglPlatform() {
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
