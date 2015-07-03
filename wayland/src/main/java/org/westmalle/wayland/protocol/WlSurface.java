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
package org.westmalle.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.WlSurfaceRequestsV3;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.freedesktop.wayland.shared.WlSurfaceError;
import org.westmalle.wayland.core.Rectangle;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.Transforms;
import org.westmalle.wayland.core.calc.Mat4;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import static com.google.common.base.Preconditions.checkArgument;

@AutoFactory(className = "WlSurfaceFactory")
public class WlSurface implements WlSurfaceRequestsV3, ProtocolObject<WlSurfaceResource> {

    private final Set<WlSurfaceResource> resources       = Sets.newSetFromMap(new WeakHashMap<>());
    private final WlCallbackFactory wlCallbackFactory;
    private final Surface           surface;
    private       Optional<Listener>     destroyListener = Optional.empty();

    WlSurface(@Provided final WlCallbackFactory wlCallbackFactory,
              final Surface surface) {
        this.wlCallbackFactory = wlCallbackFactory;
        this.surface = surface;
    }

    @Nonnull
    @Override
    public Set<WlSurfaceResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlSurfaceResource create(@Nonnull final Client client,
                                    @Nonnegative final int version,
                                    final int id) {
        return new WlSurfaceResource(client,
                                     version,
                                     id,
                                     this);
    }

    @Override
    public void destroy(final WlSurfaceResource resource) {
        resource.destroy();
        getSurface().markDestroyed();
    }

    @Override
    public void attach(final WlSurfaceResource requester,
                       @Nullable final WlBufferResource buffer,
                       final int x,
                       final int y) {
        if (buffer == null) {
            detachBuffer();
        }
        else {
            attachBuffer(buffer,
                         x,
                         y);
        }
    }

    @Override
    public void damage(final WlSurfaceResource resource,
                       final int x,
                       final int y,
                       @Nonnegative final int width,
                       @Nonnegative final int height) {
        checkArgument(width > 0);
        checkArgument(height > 0);

        getSurface().markDamaged(Rectangle.create(x,
                                                  y,
                                                  width,
                                                  height));
    }

    @Override
    public void frame(final WlSurfaceResource resource,
                      final int callbackId) {
        final WlCallbackResource callbackResource = this.wlCallbackFactory.create()
                                                                          .add(resource.getClient(),
                                                                               resource.getVersion(),
                                                                               callbackId);
        getSurface().addCallback(callbackResource);
    }

    @Override
    public void setOpaqueRegion(final WlSurfaceResource requester,
                                final WlRegionResource region) {
        if (region == null) {
            getSurface().removeOpaqueRegion();
        }
        else {
            getSurface().setOpaqueRegion(region);
        }
    }

    @Override
    public void setInputRegion(final WlSurfaceResource requester,
                               @Nullable final WlRegionResource regionResource) {
        if (regionResource == null) {
            getSurface().removeInputRegion();
        }
        else {
            getSurface().setInputRegion(regionResource);
        }
    }

    @Override
    public void commit(final WlSurfaceResource requester) {
        removeBufferDestroyListener();
        final Surface surface = getSurface();
        surface.getSurfaceRole()
               .ifPresent(role -> role.beforeCommit(requester));
        surface.commit();
    }

    @Override
    public void setBufferTransform(final WlSurfaceResource resource,
                                   final int transform) {
        this.surface.setBufferTransform(getMatrix(resource,
                                                  transform));
    }

    @Override
    public void setBufferScale(final WlSurfaceResource resource,
                               @Nonnegative final int scale) {
        if (scale > 0) {
            getSurface().setScale(scale);
        }
        else {
            resource.postError(WlSurfaceError.INVALID_SCALE.getValue(),
                               String.format("Invalid scale %d. Scale must be positive integer.",
                                             scale));
        }
    }

    public Surface getSurface() {
        return this.surface;
    }

    private Mat4 getMatrix(final WlSurfaceResource resource,
                           final int transform) {
        if (WlOutputTransform.NORMAL.getValue() == transform) {
            return Transforms.NORMAL;
        }
        else if (WlOutputTransform._90.getValue() == transform) {
            return Transforms._90;
        }
        else if (WlOutputTransform._180.getValue() == transform) {
            return Transforms._180;
        }
        else if (WlOutputTransform._270.getValue() == transform) {
            return Transforms._270;
        }
        else if (WlOutputTransform.FLIPPED.getValue() == transform) {
            return Transforms.FLIPPED;
        }
        else if (WlOutputTransform.FLIPPED_90.getValue() == transform) {
            return Transforms.FLIPPED_90;
        }
        else if (WlOutputTransform.FLIPPED_180.getValue() == transform) {
            return Transforms.FLIPPED_180;
        }
        else if (WlOutputTransform.FLIPPED_270.getValue() == transform) {
            return Transforms.FLIPPED_270;
        }
        else {
            resource.postError(WlSurfaceError.INVALID_TRANSFORM.getValue(),
                               String.format("Invalid transform %d. Supported values are %s.",
                                             transform,
                                             Arrays.asList(WlOutputTransform.values())));
            return Transforms.NORMAL;
        }
    }

    private void detachBuffer() {
        removeBufferDestroyListener();
        getSurface().detachBuffer();
    }

    private void attachBuffer(final WlBufferResource buffer,
                              final int x,
                              final int y) {

        removeBufferDestroyListener();
        addBufferDestroyListener(buffer);

        getSurface().attachBuffer(buffer,
                                  x,
                                  y);
    }

    private void removeBufferDestroyListener() {
        this.destroyListener.ifPresent(Listener::remove);
        this.destroyListener = Optional.empty();
    }

    private void addBufferDestroyListener(final WlBufferResource buffer) {
        final Listener listener = new Listener() {
            @Override
            public void handle() {
                remove();
                WlSurface.this.detachBuffer();
            }
        };
        this.destroyListener = Optional.of(listener);
        buffer.addDestroyListener(listener);
    }
}
