package org.westmalle.wayland.x11;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "X11EglConnectorFactory")
public class X11EglConnector implements EglConnector {

    @Nonnull
    private final X11Connector x11Connector;
    private final long         eglSurface;


    X11EglConnector(@Nonnull final X11Connector x11Connector,
                    final long eglSurface) {
        this.x11Connector = x11Connector;
        this.eglSurface = eglSurface;
    }

    @Override
    public long getEglSurface() {
        return this.eglSurface;
    }


    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.x11Connector.getWlOutput();
    }

    @Nonnull
    public X11Connector getX11Connector() {
        return this.x11Connector;
    }
}
