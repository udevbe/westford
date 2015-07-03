package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.Subscribe;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.events.Motion;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Optional;

@AutoFactory(className = "CursorFactory")
public class Cursor {

    private final NullRegion        nullRegion;
    private final WlPointerResource wlPointerResource;
    private final WlSurfaceResource wlSurfaceResource;
    private       Point             hotspot;

    Cursor(@Provided final NullRegion nullRegion,
           final WlPointerResource wlPointerResource,
           final WlSurfaceResource wlSurfaceResource) {
        this.nullRegion = nullRegion;
        this.wlPointerResource = wlPointerResource;
        this.wlSurfaceResource = wlSurfaceResource;
    }

    public WlPointerResource getWlPointerResource() {
        return this.wlPointerResource;
    }

    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    public void assigned() {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final WlPointer     wlPointer     = (WlPointer) this.wlPointerResource.getImplementation();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        surface.setState(surface.getState()
                                .toBuilder()
                                .inputRegion(Optional.of(this.nullRegion))
                                .build());
        updateCursorPosition(pointerDevice.getPosition());

        //if the role object is destroyed, the surface should be 'reset' and become invisible until
        //a new role object of the same type is assigned to it.
        this.wlPointerResource.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                surface.setPosition(Point.ZERO);
                //TODO hide surface
            }
        });
    }

    public void updateCursorPosition(final Point pointerPosition) {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.setPosition(pointerPosition.subtract(getHotspot()));
    }

    public Point getHotspot() {
        return this.hotspot;
    }

    public void setHotspot(final Point hotspot) {
        this.hotspot = hotspot;
    }

    public void beforeCommit() {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.setPendingState(surface.getPendingState()
                                       .toBuilder()
                                       .inputRegion(Optional.of(this.nullRegion))
                                       .build());
    }

    @Subscribe
    public void handle(final Motion motion) {
        updateCursorPosition(motion.getPoint());
    }
}
