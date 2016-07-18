package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5Platform;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5EglPlatformFactory")
public class Html5EglPlatform implements EglPlatform {

    @Nonnull
    private final EglPlatform                       eglPlatform;
    @Nonnull
    private final List<Optional<Html5EglConnector>> eglConnectors;

    @Inject
    Html5EglPlatform(@Nonnull final EglPlatform eglPlatform,
                     @Nonnull final List<Optional<Html5EglConnector>> eglConnectors) {
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

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglPlatform.getEglExtensions();
    }
}
