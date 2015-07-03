package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.westmalle.wayland.core.calc.Mat4;

import java.util.Optional;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class SurfaceState {

    static Builder builder() {
        return new AutoValue_SurfaceState.Builder().opaqueRegion(Optional.<Region>empty())
                                                   .inputRegion(Optional.<Region>empty())
                                                   .damage(Optional.<Region>empty())
                                                   .buffer(Optional.<WlBufferResource>empty())
                                                   .bufferTransform(Mat4.IDENTITY)
                                                   .positionTransform(Mat4.IDENTITY)
                                                   .scale(1);
    }

    @Nonnull
    public abstract Optional<Region> getOpaqueRegion();

    @Nonnull
    public abstract Optional<Region> getInputRegion();

    @Nonnull
    public abstract Optional<Region> getDamage();

    @Nonnull
    public abstract Optional<WlBufferResource> getBuffer();

    @Nonnull
    public abstract Mat4 getBufferTransform();

    @Nonnull
    public abstract Mat4 getPositionTransform();

    @Nonnegative
    abstract int getScale();

    @AutoValue.Builder
    interface Builder {
        Builder opaqueRegion(Optional<Region> wlRegionResource);

        Builder inputRegion(Optional<Region> wlRegionResource);

        Builder damage(Optional<Region> damage);

        Builder buffer(Optional<WlBufferResource> wlBufferResource);

        Builder bufferTransform(Mat4 bufferTransform);

        Builder positionTransform(Mat4 positionTransform);

        Builder scale(int scale);

        SurfaceState build();
    }

    abstract Builder toBuilder();
}