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
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Vec4;
import com.hackoeur.jglm.support.FastMath;
import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.westmalle.wayland.output.*;

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
                  final Mat4 transform = transform(quadrant,
                                                   size,
                                                   local);
                  pointerDevice.grabMotion(this.wlSurfaceResource,
                                           serial,
                                           motion -> {
                                               final Vec4 motionLocal = surface.local(motion.getPoint())
                                                                               .toVec4();
                                               final Vec4 resize = transform.multiply(motionLocal);
                                               requester.configure(quadrant.getValue(),
                                                                   FastMath.round(resize.getX()),
                                                                   FastMath.round(resize.getY()));
                                           });
              });
    }

    private Mat4 transform(final WlShellSurfaceResize quadrant,
                           final Rectangle size,
                           final Point local) {
        //TODO support one dimensional resize (TOP, BOTTOM, LEFT, RIGHT)

        final int width = size.getWidth();
        final int height = size.getHeight();

        final Mat4 quadrantTransform;
        switch (quadrant) {
            case TOP_LEFT: {
                final float[] anchorTranslation = new float[16];
                anchorTranslation[12] = width;
                anchorTranslation[13] = height;
                quadrantTransform = Transforms._180.add(new Mat4(anchorTranslation));
                break;
            }
            case TOP_RIGHT: {
                final float[] anchorTranslation = new float[16];
                anchorTranslation[13] = height;
                quadrantTransform = Transforms.FLIPPED_180.add(new Mat4(anchorTranslation));
                break;
            }
            case BOTTOM_LEFT: {
                final float[] anchorTranslation = new float[16];
                anchorTranslation[12] = width;
                quadrantTransform = Transforms.FLIPPED.add(new Mat4(anchorTranslation));
                break;
            }
            default: {
                quadrantTransform = Mat4.MAT4_IDENTITY;
            }
        }

        final Vec4 localTransformed = quadrantTransform.multiply(local.toVec4());
        final float[] deltaTranslation = new float[16];
        deltaTranslation[12] = width - localTransformed.getX();
        deltaTranslation[13] = height - localTransformed.getY();

        return quadrantTransform.add(new Mat4(deltaTranslation));
    }


    private WlShellSurfaceResize quadrant(final Rectangle size,
                                          final Point local) {
        //TODO support one dimensional resize (TOP, BOTTOM, LEFT, RIGHT)

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
