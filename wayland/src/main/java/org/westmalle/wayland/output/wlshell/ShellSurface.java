package org.westmalle.wayland.output.wlshell;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.output.calc.Mat4;
import org.westmalle.wayland.output.calc.Vec4;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Optional;

@AutoFactory(className = "ShellSurfaceFactory")
public class ShellSurface {

    @Nonnull
    private final WlCompositor wlCompositor;
    private final int          pingSerial;
    private final EventSource  timerEventSource;

    private boolean          active = true;
    private Optional<String> clazz  = Optional.empty();
    private Optional<String> title  = Optional.empty();

    public ShellSurface(@Provided @Nonnull final Display display,
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

    public Optional<String> getClazz() {
        return this.clazz;
    }

    public void setClazz(final Optional<String> clazz) {
        this.clazz = clazz;
        this.wlCompositor.getCompositor()
                         .requestRender();
    }

    public Optional<String> getTitle() {
        return this.title;
    }

    public void setTitle(final Optional<String> title) {
        this.title = title;
        this.wlCompositor.getCompositor()
                         .requestRender();
    }

    public void pong(final WlShellSurfaceResource wlShellSurfaceResource,
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

    public void move(final WlSurfaceResource wlSurfaceResource,
                     final WlPointer wlPointer,
                     final int grabSerial) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();
        final Point pointerPosition = wlPointer.getPointerDevice()
                                               .getPosition();
        final Point surfacePosition = surface.getPosition();
        final Point pointerOffset = pointerPosition.subtract(surfacePosition);
        wlPointer.getPointerDevice()
                 .grabMotion(wlSurfaceResource,
                             grabSerial,
                             (motion) -> surface.setPosition(motion.getPoint()
                                                                   .subtract(pointerOffset)));
    }

    public void resize(final WlShellSurfaceResource wlShellSurfaceResource,
                       final WlSurfaceResource wlSurfaceResource,
                       @Nonnull final WlPointer wlPointer,
                       final int serial,
                       final int edges) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();
        final Point pointerStartPos = pointerDevice.getPosition();

        final Point local = surface.local(pointerStartPos);
        final Rectangle size = surface.getSize();

        final WlShellSurfaceResize quadrant = quadrant(edges);
        final Mat4 transform = transform(quadrant,
                                         size,
                                         local);
        pointerDevice.grabMotion(wlSurfaceResource,
                                 serial,
                                 motion -> {
                                     final Vec4 motionLocal = surface.local(motion.getPoint())
                                                                     .toVec4();
                                     final Vec4 resize = transform.multiply(motionLocal);
                                     wlShellSurfaceResource.configure(quadrant.getValue(),
                                                                      Math.max(1,
                                                                               Math.round(resize.getX())),
                                                                      Math.max(1,
                                                                               Math.round(resize.getY())));
                                 });
    }

    private Mat4 transform(final WlShellSurfaceResize quadrant,
                           final Rectangle size,
                           final Point local) {
        final int width = size.getWidth();
        final int height = size.getHeight();

        final Mat4 quadrantTransform;
        final float[] anchorTranslation = new float[16];
        final float[] deltaTranslation = new float[16];
        final Vec4 localTransformed;
        switch (quadrant) {
            case TOP:
                anchorTranslation[0] = 1;
                anchorTranslation[13] = height;
                quadrantTransform = Transforms._180.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width;
                deltaTranslation[13] = height - localTransformed.getY();
                break;
            case TOP_LEFT:
                anchorTranslation[12] = width;
                anchorTranslation[13] = height;
                quadrantTransform = Transforms._180.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width - localTransformed.getX();
                deltaTranslation[13] = height - localTransformed.getY();
                break;
            case LEFT:
                anchorTranslation[5] = -1;
                anchorTranslation[12] = width;
                quadrantTransform = Transforms.FLIPPED.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width - localTransformed.getX();
                deltaTranslation[13] = height;
                break;
            case BOTTOM_LEFT:
                anchorTranslation[12] = width;
                quadrantTransform = Transforms.FLIPPED.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width - localTransformed.getX();
                deltaTranslation[13] = height - localTransformed.getY();
                break;
            case RIGHT:
                anchorTranslation[5] = 1;
                anchorTranslation[13] = height;
                quadrantTransform = Transforms.FLIPPED_180.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width - localTransformed.getX();
                deltaTranslation[13] = 0;
                break;
            case TOP_RIGHT:
                anchorTranslation[13] = height;
                quadrantTransform = Transforms.FLIPPED_180.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width - localTransformed.getX();
                deltaTranslation[13] = height - localTransformed.getY();
                break;
            case BOTTOM:
                anchorTranslation[0] = -1;
                quadrantTransform = Transforms.NORMAL.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width;
                deltaTranslation[13] = height - localTransformed.getY();
                break;
            case BOTTOM_RIGHT:
                quadrantTransform = Transforms.NORMAL.add(Mat4.create(anchorTranslation));

                localTransformed = quadrantTransform.multiply(local.toVec4());
                deltaTranslation[12] = width - localTransformed.getX();
                deltaTranslation[13] = height - localTransformed.getY();
                break;
            default:
                quadrantTransform = Transforms.NORMAL;
        }

        return quadrantTransform.add(Mat4.create(deltaTranslation));
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

    public void toFront(final WlSurfaceResource wlSurfaceResource) {
        final Compositor compositor = this.wlCompositor.getCompositor();
        final LinkedList<WlSurfaceResource> surfacesStack = compositor.getSurfacesStack();
        if (surfacesStack.remove(wlSurfaceResource)) {
            surfacesStack.push(wlSurfaceResource);
            compositor.requestRender();
        }
    }

    public void setTransient(final WlSurfaceResource wlSurfaceResource,
                             final WlSurfaceResource parent,
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
