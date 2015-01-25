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

package org.trinity.wayland.output;

import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnull;
import javax.media.nativewindow.util.PointImmutable;
import javax.media.nativewindow.util.RectangleImmutable;
import java.util.function.IntConsumer;

public interface SurfaceConfigurable {
    @Nonnull
    SurfaceConfigurable addCallback(IntConsumer callback);

    @Nonnull
    SurfaceConfigurable removeOpaqueRegion();

    @Nonnull
    SurfaceConfigurable setOpaqueRegion(@Nonnull Region opaqueRegion);

    @Nonnull
    SurfaceConfigurable removeInputRegion();

    @Nonnull
    SurfaceConfigurable setInputRegion(@Nonnull Region inputRegion);

    @Nonnull
    SurfaceConfigurable setPosition(@Nonnull PointImmutable position);

    @Nonnull
    SurfaceConfigurable markDestroyed();

    @Nonnull
    SurfaceConfigurable markDamaged(@Nonnull RectangleImmutable damage);

    @Nonnull
    SurfaceConfigurable attachBuffer(@Nonnull WlBufferResource buffer,
                                     @Nonnull Integer relX,
                                     @Nonnull Integer relY);

    @Nonnull
    SurfaceConfigurable setTransform(float[] transform);

    @Nonnull
    SurfaceConfigurable removeTransform();

    @Nonnull
    SurfaceConfigurable detachBuffer();

    @Nonnull
    SurfaceConfigurable commit();
}
