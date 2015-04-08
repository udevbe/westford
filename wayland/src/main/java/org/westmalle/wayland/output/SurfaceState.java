package org.westmalle.wayland.output;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.westmalle.wayland.output.calc.Mat4;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import java.util.Optional;

@AutoValue
public abstract class SurfaceState {

    static Builder builder() {
        return new AutoValue_SurfaceState.Builder().opaqueRegion(Optional.<WlRegionResource>empty())
                                                   .inputRegion(Optional.<WlRegionResource>empty())
                                                   .damage(Optional.<Region>empty())
                                                   .buffer(Optional.<WlBufferResource>empty())
                                                   .bufferTransform(Mat4.IDENTITY)
                                                   .scale(1);
    }

    @Nonnull
    public abstract Optional<WlRegionResource> getOpaqueRegion();

    @Nonnull
    public abstract Optional<WlRegionResource> getInputRegion();

    @Nonnull
    public abstract Optional<Region> getDamage();

    @Nonnull
    public abstract Optional<WlBufferResource> getBuffer();

    @Nonnull
    public abstract Mat4 getBufferTransform();

    @Nonnegative
    abstract int getScale();

    @AutoValue.Builder
    interface Builder {
        Builder opaqueRegion(Optional<WlRegionResource> wlRegionResource);

        Builder inputRegion(Optional<WlRegionResource> wlRegionResource);

        Builder damage(Optional<Region> damage);

        Builder buffer(Optional<WlBufferResource> wlBufferResource);

        Builder bufferTransform(Mat4 bufferTransform);

        Builder scale(int scale);

        SurfaceState build();
    }

    abstract Builder toBuilder();
}