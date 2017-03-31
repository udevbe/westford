package org.westford.compositor.core;


import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

public class SingleViewLayer {
    @Nonnull
    private Optional<SurfaceView> surfaceView = Optional.empty();

    @Inject
    SingleViewLayer() {}

    @Nonnull
    public Optional<SurfaceView> getSurfaceView() {
        return this.surfaceView;
    }

    public void setSurfaceView(@Nonnull final SurfaceView surfaceView) {
        this.surfaceView = Optional.of(surfaceView);
    }

    public void removeSurfaceView() {
        this.surfaceView = Optional.empty();
    }

    public void removeIfEqualTo(@Nonnull final SurfaceView surfaceView) {
        this.surfaceView.ifPresent(backgroundView -> {
            if (backgroundView.equals(surfaceView)) {
                removeSurfaceView();
            }
        });
    }
}
