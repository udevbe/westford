package org.westmalle.wayland.dispmanx.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.dispmanx.DispmanxConnector;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(className = "DispmanxEglConnectorFactory",
             allowSubclasses = true)
public class DispmanxEglConnector implements EglConnector {

    @Nonnull
    private final DispmanxConnector     dispmanxConnector;
    @Nonnull
    private final EGL_DISPMANX_WINDOW_T eglDispmanxWindow;
    private final long                  eglSurface;

    DispmanxEglConnector(@Nonnull final DispmanxConnector dispmanxConnector,
                         @Nonnull final EGL_DISPMANX_WINDOW_T eglDispmanxWindow,
                         final long eglSurface) {
        this.dispmanxConnector = dispmanxConnector;
        this.eglDispmanxWindow = eglDispmanxWindow;
        this.eglSurface = eglSurface;
    }

    @Nonnull
    public DispmanxConnector getDispmanxConnector() {
        return this.dispmanxConnector;
    }

    @Override
    public long getEglSurface() {
        return this.eglSurface;
    }

    @Nonnull
    public EGL_DISPMANX_WINDOW_T getEglDispmanxWindow() {
        return this.eglDispmanxWindow;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.dispmanxConnector.getWlOutput();
    }
}
