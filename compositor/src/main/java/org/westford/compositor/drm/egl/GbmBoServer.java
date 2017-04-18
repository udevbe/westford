package org.westford.compositor.drm.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westford.nativ.libgbm.Libgbm;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateGbmBoServerFactory")
public class GbmBoServer implements GbmBo {
    @Nonnull
    private final Libgbm libgbm;
    private final long   gbmSurface;
    private final long   gbmBo;

    GbmBoServer(@Provided @Nonnull final Libgbm libgbm,
                long gbmSurface,
                long gbmBo) {
        this.libgbm = libgbm;
        this.gbmSurface = gbmSurface;
        this.gbmBo = gbmBo;
    }

    @Override
    public long getGbmBo() {
        return this.gbmBo;
    }

    @Override
    public void close() {
        this.libgbm.gbm_surface_release_buffer(this.gbmSurface,
                                               this.gbmBo);
    }
}
