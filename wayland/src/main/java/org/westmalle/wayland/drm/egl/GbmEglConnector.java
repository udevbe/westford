package org.westmalle.wayland.drm.egl;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.drm.GbmConnector;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory
public class GbmEglConnector implements EglConnector {

    @Nonnull
    private final Libgbm       libgbm;
    @Nonnull
    private final Libdrm       libdrm;
    @Nonnull
    private final GbmConnector gbmConnector;

    GbmEglConnector(@Nonnull @Provided
                    final Libgbm libgbm,
                    @Nonnull @Provided
                    final Libdrm libdrm,
                    @Nonnull final GbmConnector gbmConnector) {
        this.libgbm = libgbm;
        this.libdrm = libdrm;
        this.gbmConnector = gbmConnector;
    }

    @Override
    public void end() {
        final long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(this.gbmConnector.getGbmSurface());

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
