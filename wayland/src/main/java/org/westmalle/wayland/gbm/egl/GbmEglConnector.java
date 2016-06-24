package org.westmalle.wayland.gbm.egl;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.gbm.GbmConnector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory
public class GbmEglConnector implements EglConnector {

    @Nonnull
    private final GbmConnector gbmConnector;

    GbmEglConnector(@Nonnull final GbmConnector gbmConnector) {
        this.gbmConnector = gbmConnector;
    }

    @Override
    public long getEglSurface() {
        return 0;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.gbmConnector.getWlOutput();
    }

    @Nonnull
    public GbmConnector getGbmConnector() {
        return this.gbmConnector;
    }
}
