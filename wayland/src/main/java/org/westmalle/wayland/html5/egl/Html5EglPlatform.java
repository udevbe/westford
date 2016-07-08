package org.westmalle.wayland.html5.egl;

import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5Platform;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class Html5EglPlatform implements EglPlatform {

    private final EglPlatform                       eglPlatform;
    private final List<Optional<Html5EglConnector>> eglConnectors;

    @Inject
    Html5EglPlatform(final EglPlatform eglPlatform,
                     final List<Optional<Html5EglConnector>> eglConnectors) {
        this.eglPlatform = eglPlatform;
        this.eglConnectors = eglConnectors;
    }

    @Override
    public long getEglDisplay() {
        return this.eglPlatform.getEglDisplay();
    }

    @Override
    public long getEglContext() {
        return this.eglPlatform.getEglContext();
    }

    @Nonnull
    @Override
    public List<Optional<Html5EglConnector>> getConnectors() {
        return this.eglConnectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglPlatform.getEglExtensions();
    }
}
