/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.wlshell;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.freedesktop.wayland.shared.WlShellSurfaceTransient;
import org.freedesktop.wayland.util.Fixed;
import org.westford.Slot;
import org.westford.compositor.core.Compositor;
import org.westford.compositor.core.KeyboardDevice;
import org.westford.compositor.core.Point;
import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.core.Rectangle;
import org.westford.compositor.core.Role;
import org.westford.compositor.core.RoleVisitor;
import org.westford.compositor.core.Scene;
import org.westford.compositor.core.Sibling;
import org.westford.compositor.core.Surface;
import org.westford.compositor.core.SurfaceView;
import org.westford.compositor.core.Transforms;
import org.westford.compositor.core.calc.Mat4;
import org.westford.compositor.core.calc.Vec4;
import org.westford.compositor.core.events.KeyboardFocusGained;
import org.westford.compositor.core.events.PointerGrab;
import org.westford.compositor.protocol.WlKeyboard;
import org.westford.compositor.protocol.WlPointer;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "PrivateShellSurfaceFactory",
             allowSubclasses = true)
public class ShellSurface implements Role {

    @Nonnull
    private final Compositor  compositor;
    @Nonnull
    private final Scene       scene;
    @Nonnull
    private final SurfaceView surfaceView;
    private final int         pingSerial;
    @Nonnull
    private final EventSource timerEventSource;

    private Optional<Slot<KeyboardFocusGained>> keyboardFocusListener = Optional.empty();
    private boolean                             active                = true;

    @Nonnull
    private Optional<String> clazz = Optional.empty();
    @Nonnull
    private Optional<String> title = Optional.empty();

    ShellSurface(@Provided @Nonnull final Display display,
                 @Provided @Nonnull final Compositor compositor,
                 @Provided @Nonnull final Scene scene,
                 final @Nonnull SurfaceView surfaceView,
                 final int pingSerial) {
        this.compositor = compositor;
        this.scene = scene;
        this.surfaceView = surfaceView;
        this.pingSerial = pingSerial;
        this.timerEventSource = display.getEventLoop()
                                       .addTimer(() -> {
                                           this.active = false;
                                           return 0;
                                       });
    }

    @Nonnull
    public Optional<String> getClazz() {
        return this.clazz;
    }

    public void setClazz(@Nonnull final Optional<String> clazz) {
        this.clazz = clazz;
        this.compositor.requestRender();
    }

    @Nonnull
    public Optional<String> getTitle() {
        return this.title;
    }

    public void setTitle(@Nonnull final Optional<String> title) {
        this.title = title;
        this.compositor.requestRender();
    }

    public void pong(@Nonnull final WlShellSurfaceResource wlShellSurfaceResource,
                     final int pingSerial) {
        if (this.pingSerial == pingSerial) {
            this.active = true;
            wlShellSurfaceResource.ping(pingSerial);
            this.timerEventSource.updateTimer(5000);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void move(@Nonnull final WlSurfaceResource wlSurfaceResource,
                     @Nonnull final WlPointerResource wlPointerResource,
                     final int grabSerial) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final WlPointer     wlPointer     = (WlPointer) wlPointerResource.getImplementation();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        final Point pointerPosition = pointerDevice.getPosition();

        pointerDevice.getGrab()
                     .filter(grabSurfaceView -> surface.getViews()
                                                       .contains(grabSurfaceView))
                     .ifPresent(grabSurfaceView -> {
                         final Point surfacePosition = grabSurfaceView.global(Point.create(0,
                                                                                           0));
                         final Point pointerOffset = pointerPosition.subtract(surfacePosition);

                         //FIXME pick a surface view based on the pointer position
                         pointerDevice.grabMotion(wlSurfaceResource,
                                                  grabSerial,
                                                  (motion) -> grabSurfaceView.setPosition(motion.getPoint()
                                                                                                .subtract(pointerOffset)));
                     });
    }

    public void resize(@Nonnull final WlShellSurfaceResource wlShellSurfaceResource,
                       @Nonnull final WlSurfaceResource wlSurfaceResource,
                       @Nonnull final WlPointerResource wlPointerResource,
                       final int buttonPressSerial,
                       final int edges) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final WlPointer     wlPointer       = (WlPointer) wlPointerResource.getImplementation();
        final PointerDevice pointerDevice   = wlPointer.getPointerDevice();
        final Point         pointerStartPos = pointerDevice.getPosition();

        pointerDevice.getGrab()
                     .filter(grabSurfaceView -> surface.getViews()
                                                       .contains(grabSurfaceView))
                     .ifPresent(grabSurfaceView -> {
                         final Point     local = grabSurfaceView.local(pointerStartPos);
                         final Rectangle size  = surface.getSize();

                         final WlShellSurfaceResize quadrant = quadrant(edges);
                         final Mat4 transform = transform(quadrant,
                                                          size,
                                                          local);

                         final Mat4 inverseTransform = grabSurfaceView.getInverseTransform();

                         final boolean grabMotionSuccess = pointerDevice.grabMotion(wlSurfaceResource,
                                                                                    buttonPressSerial,
                                                                                    motion -> {
                                                                                        final Vec4 motionLocal = inverseTransform.multiply(motion.getPoint()
                                                                                                                                                 .toVec4());
                                                                                        final Vec4 resize = transform.multiply(motionLocal);
                                                                                        final int  width  = (int) resize.getX();
                                                                                        final int  height = (int) resize.getY();
                                                                                        wlShellSurfaceResource.configure(quadrant.value,
                                                                                                                         width < 1 ? 1 : width,
                                                                                                                         height < 1 ? 1 : height);
                                                                                    });

                         if (grabMotionSuccess) {
                             wlPointerResource.leave(pointerDevice.nextLeaveSerial(),
                                                     wlSurfaceResource);
                             pointerDevice.getPointerGrabSignal()
                                          .connect(new Slot<PointerGrab>() {
                                              @Override
                                              public void handle(@Nonnull final PointerGrab event) {
                                                  if (!pointerDevice.getGrab()
                                                                    .isPresent()) {
                                                      pointerDevice.getPointerGrabSignal()
                                                                   .disconnect(this);
                                                      wlPointerResource.enter(pointerDevice.nextEnterSerial(),
                                                                              wlSurfaceResource,
                                                                              Fixed.create(local.getX()),
                                                                              Fixed.create(local.getY()));
                                                  }
                                              }
                                          });
                         }
                     });
    }

    private WlShellSurfaceResize quadrant(final int edges) {
        switch (edges) {
            case 0:
                return WlShellSurfaceResize.NONE;
            case 1:
                return WlShellSurfaceResize.TOP;
            case 2:
                return WlShellSurfaceResize.BOTTOM;
            case 4:
                return WlShellSurfaceResize.LEFT;
            case 5:
                return WlShellSurfaceResize.TOP_LEFT;
            case 6:
                return WlShellSurfaceResize.BOTTOM_LEFT;
            case 8:
                return WlShellSurfaceResize.RIGHT;
            case 9:
                return WlShellSurfaceResize.TOP_RIGHT;
            case 10:
                return WlShellSurfaceResize.BOTTOM_RIGHT;
            default:
                return WlShellSurfaceResize.NONE;
        }
    }

    private Mat4 transform(@Nonnull final WlShellSurfaceResize quadrant,
                           @Nonnull final Rectangle size,
                           @Nonnull final Point pointerLocal) {
        final int width  = size.getWidth();
        final int height = size.getHeight();

        final Mat4.Builder transformationBuilder;
        final Mat4         transformation;
        final float        pointerdx;
        final float        pointerdy;
        switch (quadrant) {
            case TOP: {
                transformationBuilder = Transforms._180.toBuilder()
                                                       .m00(0)
                                                       .m30(width);
                transformation = transformationBuilder.build();
                final Vec4 pointerLocalTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = 0;
                pointerdy = height - pointerLocalTransformed.getY();
                break;
            }
            case TOP_LEFT: {
                transformationBuilder = Transforms._180.toBuilder()
                                                       .m30(width)
                                                       .m31(height);
                transformation = transformationBuilder.build();
                final Vec4 localTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = width - localTransformed.getX();
                pointerdy = height - localTransformed.getY();
                break;
            }
            case LEFT: {
                transformationBuilder = Transforms.FLIPPED.toBuilder()
                                                          .m11(0f)
                                                          .m31(height);
                transformation = transformationBuilder.build();
                final Vec4 localTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = width - localTransformed.getX();
                pointerdy = 0f;
                break;
            }
            case BOTTOM_LEFT: {
                transformationBuilder = Transforms.FLIPPED.toBuilder()
                                                          .m30(width);
                transformation = transformationBuilder.build();
                final Vec4 localTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = width - localTransformed.getX();
                pointerdy = height - localTransformed.getY();
                break;
            }
            case RIGHT: {
                transformationBuilder = Transforms.NORMAL.toBuilder()
                                                         .m11(0f)
                                                         .m31(height);
                transformation = transformationBuilder.build();
                final Vec4 localTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = width - localTransformed.getX();
                pointerdy = 0f;
                break;
            }
            case TOP_RIGHT: {
                transformationBuilder = Transforms.FLIPPED_180.toBuilder()
                                                              .m31(height);
                transformation = transformationBuilder.build();
                final Vec4 localTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = width - localTransformed.getX();
                pointerdy = height - localTransformed.getY();
                break;
            }
            case BOTTOM: {
                transformationBuilder = Transforms.NORMAL.toBuilder()
                                                         .m00(0)
                                                         .m30(width);
                transformation = transformationBuilder.build();
                final Vec4 pointerLocalTransformed = transformation.multiply(pointerLocal.toVec4());
                pointerdx = 0;
                pointerdy = height - pointerLocalTransformed.getY();
                break;
            }
            case BOTTOM_RIGHT: {
                transformationBuilder = Transforms.NORMAL.toBuilder();
                transformation = transformationBuilder.build();
                final Vec4 localTransformed = pointerLocal.toVec4();
                pointerdx = width - localTransformed.getX();
                pointerdy = height - localTransformed.getY();
                break;
            }
            default: {
                transformationBuilder = Transforms.NORMAL.toBuilder();
                transformation = transformationBuilder.build();
                pointerdx = 0f;
                pointerdy = 0f;
            }
        }

        return transformationBuilder.m30(transformation.getM30() + pointerdx)
                                    .m31(transformation.getM31() + pointerdy)
                                    .build();
    }

    public void setTransient(@Nonnull final WlSurfaceResource wlSurfaceResource,
                             @Nonnull final WlSurfaceResource parent,
                             final int x,
                             final int y,
                             final EnumSet<WlShellSurfaceTransient> flags) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        this.keyboardFocusListener.ifPresent(slot -> surface.getKeyboardFocusGainedSignal()
                                                            .disconnect(slot));

        if (flags.contains(WlShellSurfaceTransient.INACTIVE)) {
            final Slot<KeyboardFocusGained> slot = keyboardFocusGained -> {
                //clean collection of focuses, so they don't get notify of keyboard related events
                surface.getKeyboardFocuses()
                       .clear();
            };
            surface.getKeyboardFocusGainedSignal()
                   .connect(slot);

            //first time focus clearing, also send out leave events
            final Set<WlKeyboardResource> keyboardFocuses = surface.getKeyboardFocuses();
            keyboardFocuses.forEach(wlKeyboardResource -> {
                final WlKeyboard     wlKeyboard     = (WlKeyboard) wlKeyboardResource.getImplementation();
                final KeyboardDevice keyboardDevice = wlKeyboard.getKeyboardDevice();
                wlKeyboardResource.leave(keyboardDevice.nextKeyboardSerial(),
                                         wlSurfaceResource);
            });
            keyboardFocuses.clear();

            this.keyboardFocusListener = Optional.of(slot);
        }

        final WlSurface parentWlSurface = (WlSurface) parent.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();

        this.scene.removeView(this.surfaceView);
        parentSurface.getViews()
                     .forEach(this.surfaceView::setParent);

        parentSurface.addSibling(Sibling.create(wlSurfaceResource,
                                                Point.create(x,
                                                             y)));
    }

    @Override
    public void accept(@Nonnull final RoleVisitor roleVisitor) {
        roleVisitor.visit(this);
    }

    public void setTopLevel(final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.getViews()
               .forEach(surfaceView ->
                                surfaceView.getParent()
                                           .ifPresent(parentSurfaceView -> {
                                               final WlSurfaceResource parentWlSurfaceResource = parentSurfaceView.getWlSurfaceResource();
                                               final WlSurface         parentWlSurface         = (WlSurface) parentWlSurfaceResource.getImplementation();
                                               final Surface           parentSurface           = parentWlSurface.getSurface();
                                               parentSurface.removeSibling(Sibling.create(wlSurfaceResource));
                                           }));

        this.scene.removeView(this.surfaceView);
        this.scene.getApplicationLayer()
                  .getSurfaceViews()
                  .add(this.surfaceView);
    }
}
