package org.westmalle.wayland.output;

import com.google.auto.value.AutoValue;

import com.hackoeur.jglm.Mat4;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlRegionResource;

import java.util.Optional;

import javax.annotation.Nonnegative;

@AutoValue
public abstract class SurfaceState {

    static Builder builder(){
        return new AutoValue_SurfaceState.Builder().opaqueRegion(Optional.<WlRegionResource>empty())
                .inputRegion(Optional.<WlRegionResource>empty())
                .damage(Optional.<Region>empty())
                .buffer(Optional.<WlBufferResource>empty())
                .bufferTransform(Mat4.MAT4_IDENTITY)
                .scale(1);
    }

    abstract Optional<WlRegionResource> getOpaqueRegion();

    abstract Optional<WlRegionResource> getInputRegion();

    abstract Optional<Region>           getDamage();

    abstract Optional<WlBufferResource> getBuffer();

    abstract Mat4 getBufferTransform();

    @Nonnegative
    abstract int getScale();

    @AutoValue.Builder
    interface Builder {
        Builder opaqueRegion(Optional<WlRegionResource> wlRegionResource);

        Builder inputRegion(Optional<WlRegionResource> wlRegionResource);

        Builder damage(Optional<Region> damage);

        Builder buffer(Optional<WlBufferResource> wlBufferResource);

        Builder bufferTransform(Mat4 bufferTransform);

        Builder scale(@Nonnegative int scale);

        SurfaceState build();
    }

    abstract Builder toBuilder();
}