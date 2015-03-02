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
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.westmalle.wayland.output.Point;
import org.westmalle.wayland.output.PointerDevice;
import org.westmalle.wayland.output.Rectangle;
import org.westmalle.wayland.output.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;

import static org.freedesktop.wayland.shared.WlShellSurfaceResize.*;

@AutoFactory(className = "WlShellSurfaceFactory")
public class WlShellSurface extends EventBus implements WlShellSurfaceRequests, ProtocolObject<WlShellSurfaceResource> {

    private final Set<WlShellSurfaceResource> resources = Sets.newHashSet();
    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;

    WlShellSurface(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        this.wlSurfaceResource = wlSurfaceResource;
    }

    @Override
    public void pong(final WlShellSurfaceResource requester,
                     final int serial) {

    }

    @Override
    public void move(final WlShellSurfaceResource requester,
                     @Nonnull final WlSeatResource seat,
                     final int serial) {
        final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
        final Surface surface = wlSurface.getSurface();

        final WlSeat wlSeat = (WlSeat) seat.getImplementation();
        wlSeat.getOptionalWlPointer()
              .ifPresent(wlPointer -> move(wlPointer,
                                           serial,
                                           surface));
    }

    private void move(final WlPointer wlPointer,
                      final int grabSerial,
                      final Surface surface) {
        final Point pointerPosition = wlPointer.getPointerDevice()
                                               .getPosition();
        final Point surfacePosition = surface.getPosition();
        final Point pointerOffset = pointerPosition.subtract(surfacePosition);
        wlPointer.getPointerDevice()
                 .grabMotion(this.wlSurfaceResource,
                             grabSerial,
                             (motion) -> surface.setPosition(motion.getPoint()
                                                                   .subtract(pointerOffset)));
    }

    @Override
    public void resize(final WlShellSurfaceResource requester,
                       @Nonnull final WlSeatResource seat,
                       final int serial,
                       final int edges) {
        final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
        final Surface surface = wlSurface.getSurface();

        final WlSeat wlSeat = (WlSeat) seat.getImplementation();
        wlSeat.getOptionalWlPointer()
              .ifPresent(wlPointer -> {
                  final PointerDevice pointerDevice = wlPointer.getPointerDevice();
                  final Point pointerStartPos = pointerDevice.getPosition();

                  final Point local = surface.local(pointerStartPos);
                  final Rectangle size = surface.getSize();

                  final WlShellSurfaceResize quadrant = quadrant(size,
                                                                 local);
                  final Point delta = delta(quadrant,
                                            size,
                                            local);
                  pointerDevice.grabMotion(this.wlSurfaceResource,
                                           serial,
                                           motion -> {
                                                //TODO support one dimensional resize
                                                final Point motionLocal = surface.local(motion.getPoint());
                                                Point cornerPoint = motionLocal.add(delta);
                                                requester.configure(quadrant.getValue(),
                                                                    cornerPoint.getX(),
                                                                    cornerPoint.getY());
                                           });
              });
    }

    private Point delta(final WlShellSurfaceResize quadrant,
                        final Rectangle size,
                        final Point local) {
        //TODO support one dimensional resize

        switch (quadrant) {
            case TOP_LEFT: {
                return local;
            }
            case TOP_RIGHT: {
                return local.toBuilder()
                            .x(size.getWidth() - local.getX())
                            .build();
            }
            case BOTTOM_RIGHT: {
                return Point.create(size.getWidth() - local.getX(),
                                    size.getHeight() - local.getY());
            }
            case BOTTOM_LEFT: {
                return local.toBuilder()
                            .y(size.getHeight() - local.getY())
                            .build();
            }
            default: {
                return Point.ZERO;
            }
        }
    }

    private WlShellSurfaceResize quadrant(final Rectangle size,
                                          final Point local) {
        //TODO support one dimensional resize

        final boolean left = local.getX() < size.getWidth() / 2;
        final boolean top = local.getY() < size.getHeight() / 2;

        if (top && left) {
            return TOP_LEFT;
        }
        else if (top) {
            return TOP_RIGHT;
        }
        else if (left) {
            return BOTTOM_LEFT;
        }
        else {
            return BOTTOM_RIGHT;
        }
    }

    @Override
    public void setToplevel(final WlShellSurfaceResource requester) {

    }

    @Override
    public void setTransient(final WlShellSurfaceResource requester,
                             @Nonnull final WlSurfaceResource parent,
                             final int x,
                             final int y,
                             final int flags) {

    }

    @Override
    public void setFullscreen(final WlShellSurfaceResource requester,
                              final int method,
                              final int framerate,
                              final WlOutputResource output) {

    }

    @Override
    public void setPopup(final WlShellSurfaceResource requester,
                         @Nonnull final WlSeatResource seat,
                         final int serial,
                         @Nonnull final WlSurfaceResource parent,
                         final int x,
                         final int y,
                         final int flags) {

    }

    @Override
    public void setMaximized(final WlShellSurfaceResource requester,
                             final WlOutputResource output) {

    }

    @Override
    public void setTitle(final WlShellSurfaceResource requester,
                         @Nonnull final String title) {

    }

    @Override
    public void setClass(final WlShellSurfaceResource requester,
                         @Nonnull final String class_) {

    }

    @Nonnull
    @Override
    public Set<WlShellSurfaceResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlShellSurfaceResource create(@Nonnull final Client client,
                                         @Nonnegative final int version,
                                         final int id) {
        return new WlShellSurfaceResource(client,
                                          version,
                                          id,
                                          this);
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }
}
