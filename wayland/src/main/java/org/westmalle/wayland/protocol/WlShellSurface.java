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
import org.westmalle.wayland.output.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;

import java.util.Set;

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
                      final Surface surface){
        final PointImmutable pointerPosition = wlPointer.getPointerDevice()
                                                        .getPosition();
        final PointImmutable surfacePosition = surface.getPosition();
        final Point surfacePositionOffset = new Point(pointerPosition.getX() - surfacePosition.getX(),
                                                      pointerPosition.getY() - surfacePosition.getY());
        wlPointer.getPointerDevice()
                 .grabMotion(this.wlSurfaceResource,
                             grabSerial,
                             (pointerDevice,
                              motion) -> surface.setPosition(new Point(motion.getX() - surfacePositionOffset.getX(),
                                                                       motion.getY() - surfacePositionOffset.getY())));
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
                    final PointImmutable globalGrabStart = wlPointer.getPointerDevice().getPosition();

                    wlPointer.getPointerDevice()
                            .grabMotion(this.wlSurfaceResource,
                                        serial,
                                        (pointerDevice,
                                         motion) -> {
                                            //TODO calculate size & width.
                                            requester.configure(0,
                                                                123,
                                                                456);
                                        });
                });
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
