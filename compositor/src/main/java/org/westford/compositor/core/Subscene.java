package org.westford.compositor.core;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * A subsection of a {@link Scene}. Has no relation with {@link Subsurface}.
 */
@AutoValue
public abstract class Subscene {
    public static Subscene create(@Nonnull final Optional<SurfaceView> backgroundView,
                                  @Nonnull final List<SurfaceView> underViews,
                                  @Nonnull final List<SurfaceView> applicationViews,
                                  @Nonnull final List<SurfaceView> overViews,
                                  @Nonnull final Optional<SurfaceView> fullscreenView,
                                  @Nonnull final List<SurfaceView> lockViews,
                                  @Nonnull final List<SurfaceView> cursorViews) {
        return new AutoValue_Subscene(backgroundView,
                                      underViews,
                                      applicationViews,
                                      overViews,
                                      fullscreenView,
                                      lockViews,
                                      cursorViews);
    }

    @Nonnull
    public abstract Optional<SurfaceView> getBackgroundView();

    @Nonnull
    public abstract List<SurfaceView> getUnderViews();

    @Nonnull
    public abstract List<SurfaceView> getApplicationViews();

    @Nonnull
    public abstract List<SurfaceView> getOverViews();

    @Nonnull
    public abstract Optional<SurfaceView> getFullscreenView();

    @Nonnull
    public abstract List<SurfaceView> getLockViews();

    @Nonnull
    public abstract List<SurfaceView> geCursorViews();

}
