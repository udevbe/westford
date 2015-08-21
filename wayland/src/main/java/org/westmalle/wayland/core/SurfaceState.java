//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlBufferResource;
import org.westmalle.wayland.core.calc.Mat4;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Optional;

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

    abstract Builder toBuilder();

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
}