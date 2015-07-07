package org.westmalle.wayland.wlshell;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.GrabSemantics;
import org.westmalle.wayland.core.Point;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.Rectangle;
import org.westmalle.wayland.core.Role;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.Transforms;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.core.calc.Vec4;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import javax.annotation.Nonnull;

@AutoFactory(className = "ShellSurfaceFactory")
public class ShellSurface implements Role {

    @Nonnull
    private final WlCompositor wlCompositor;
    private final int pingSerial;
    @Nonnull
    private final EventSource timerEventSource;

    private boolean active = true;
    @Nonnull
    private Optional<String> clazz = Optional.empty();
    @Nonnull
    private Optional<String> title = Optional.empty();

    ShellSurface(@Provided @Nonnull final Display display,
                 @Nonnull final WlCompositor wlCompositor,
                 final int pingSerial) {
        this.wlCompositor = wlCompositor;
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
        this.wlCompositor.getCompositor()
                .requestRender();
    }

    @Nonnull
    public Optional<String> getTitle() {
        return this.title;
    }

    public void setTitle(@Nonnull final Optional<String> title) {
        this.title = title;
        this.wlCompositor.getCompositor()
                .requestRender();
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
        final Surface surface = wlSurface.getSurface();

        final WlPointer wlPointer = (WlPointer) wlPointerResource.getImplementation();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        final Point pointerPosition = pointerDevice.getPosition();
        final Point surfacePosition = surface.global(Point.create(0,
                                                                  0));
        final Point pointerOffset = pointerPosition.subtract(surfacePosition);
        pointerDevice.grabMotion(wlSurfaceResource,
                                 grabSerial,
                                 (motion) -> surface.setPosition(motion.getPoint()
                                                                         .subtract(pointerOffset)));
    }

    public void resize(@Nonnull final WlShellSurfaceResource wlShellSurfaceResource,
                       @Nonnull final WlSurfaceResource wlSurfaceResource,
                       @Nonnull final WlPointerResource wlPointerResource,
                       final int serial,
                       final int edges) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();
        final WlPointer wlPointer = (WlPointer) wlPointerResource.getImplementation();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();
        final Point pointerStartPos = pointerDevice.getPosition();

        final Point local = surface.local(pointerStartPos);
        final Rectangle size = surface.getSize();

        final WlShellSurfaceResize quadrant = quadrant(edges);
        final Mat4 transform = transform(quadrant,
                                         size,
                                         local);

        final Mat4 inverseTransform = surface.getInverseTransform();
        pointerDevice.grabMotion(wlSurfaceResource,
                                 serial,
                                 motion -> {
                                     final Vec4 motionLocal = inverseTransform.multiply(motion.getPoint()
                                                                                                .toVec4());
                                     final Vec4 resize = transform.multiply(motionLocal);
                                     final int width = (int) resize.getX();
                                     final int height = (int) resize.getY();
                                     wlShellSurfaceResource.configure(quadrant.getValue(),
                                                                      width < 1 ? 1 : width,
                                                                      height < 1 ? 1 : height);
                                 },
                                 new GrabSemantics() {
                                     @Override
                                     public void grab() {
                                         pointerDevice.reportLeave(Collections.singleton(wlPointerResource),
                                                                   wlSurfaceResource);
                                     }

                                     @Override
                                     public void ungrab() {
                                         pointerDevice.reportEnter(Collections.singleton(wlPointerResource),
                                                                   wlSurfaceResource);
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
        final int width = size.getWidth();
        final int height = size.getHeight();

        final Mat4.Builder transformationBuilder;
        final Mat4 transformation;
        final float pointerdx;
        final float pointerdy;
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

    public void toFront(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        final Compositor compositor = this.wlCompositor.getCompositor();
        final LinkedList<WlSurfaceResource> surfacesStack = compositor.getSurfacesStack();
        if (surfacesStack.remove(wlSurfaceResource)) {
            surfacesStack.addLast(wlSurfaceResource);
            compositor.requestRender();
        }
    }

    public void setTransient(@Nonnull final WlSurfaceResource wlSurfaceResource,
                             @Nonnull final WlSurfaceResource parent,
                             final int x,
                             final int y,
                             final int flags) {
        //TODO interprete flags (for keyboard focus)
        final WlSurface parentWlSurface = (WlSurface) parent.getImplementation();
        final Point surfacePosition = parentWlSurface.getSurface()
                .global(Point.create(x,
                                     y));
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        wlSurface.getSurface()
                .setPosition(surfacePosition);
    }
}
