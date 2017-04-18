package org.westford.compositor.drm.egl;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westford.nativ.libgbm.Libgbm;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateGbmBoClientFactory")
public class GbmBoClient implements GbmBo {

    @Nonnull
    private final Libgbm libgbm;
    private final long   gbmBo;

    GbmBoClient(@Provided @Nonnull final Libgbm libgbm,
                long gbmBo) {
        this.libgbm = libgbm;
        this.gbmBo = gbmBo;
    }

    @Override
    public long getGbmBo() {
        return this.gbmBo;
    }

    @Override
    public void close() {
        this.libgbm.gbm_bo_destroy(this.gbmBo);
    }
}
