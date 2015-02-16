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
import com.google.common.eventbus.EventBus;
import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.output.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.media.nativewindow.util.Rectangle;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@AutoFactory(className = "WlSurfaceFactory")
public class WlSurface extends EventBus implements WlSurfaceRequestsV3, ProtocolObject<WlSurfaceResource> {

    private final Set<WlSurfaceResource> resources       = Sets.newHashSet();
    private       Optional<Listener>     destroyListener = Optional.empty();

    private final WlCallbackFactory wlCallbackFactory;
    private final Surface           surface;

    private Optional<WlBufferResource> pendingBuffer = Optional.empty();

    WlSurface(@Provided final WlCallbackFactory wlCallbackFactory,
              final Surface surface) {
        this.wlCallbackFactory = wlCallbackFactory;
        this.surface = surface;
    }

    public Surface getSurface() {
        return this.surface;
    }

    @Override
    public void setBufferScale(final WlSurfaceResource resource,
                               final int scale) {

    }

    @Override
    public void setBufferTransform(final WlSurfaceResource resource,
                                   final int transform) {
        //TODO support buffer transform
        this.surface.setTransform(getMatrix(transform));
    }

    private float[] getMatrix(final int transform) {
        if (transform == WlOutputTransform.FLIPPED_270.getValue()) {
            return new float[]{
                    1, 0, 0,
                    0, 1, 0,
                    0, 0, 1
            };
        }
        throw new IllegalArgumentException("Invalid transform");
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

        getSurface().markDamaged(new Rectangle(x,
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
        this.pendingBuffer = Optional.empty();
        this.destroyListener.ifPresent(Listener::remove);
        getSurface().commit(requester);
    }

    private void detachBuffer() {
        this.pendingBuffer.ifPresent(wlShmBuffer -> this.destroyListener.ifPresent(Listener::remove));
        getSurface().detachBuffer();
    }

    private void attachBuffer(final WlBufferResource buffer,
                              final int x,
                              final int y) {
        this.pendingBuffer.ifPresent(wlShmBuffer -> this.destroyListener.ifPresent(Listener::remove));
        this.pendingBuffer = Optional.of(buffer);
        final Listener listener = new Listener() {
            @Override
            public void handle() {
                remove();
                WlSurface.this.detachBuffer();
            }
        };
        this.destroyListener = Optional.of(listener);
        buffer.addDestroyListener(listener);

        getSurface().attachBuffer(buffer,
                                  x,
                                  y);
    }
}
