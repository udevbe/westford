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

@AutoFactory(className = "CursorRoleFactory")
public class CursorRole implements Role<WlPointerResource> {

    private final NullRegion nullRegion;

    private final WlSurfaceResource wlSurfaceResource;

    private int hotspotX;
    private int hotspotY;

    CursorRole(@Provided final NullRegion nullRegion,
               final WlSurfaceResource wlSurfaceResource) {
        this.nullRegion = nullRegion;
        this.wlSurfaceResource = wlSurfaceResource;
    }

    public void hotSpot(int x,
                        int y) {
        this.hotspotX = x;
        this.hotspotY = y;
    }

    private void updateCursorPosition(final Point pointerPosition){
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();

        surface.setPosition(pointerPosition.subtract(Point.create(this.hotspotX,
                                                                  this.hotspotY)));
    }

    @Override
    public void assigned(WlPointerResource wlPointerResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();

        final WlPointer wlPointer = (WlPointer) wlPointerResource.getImplementation();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        surface.setState(surface.getState()
                                .toBuilder()
                                .inputRegion(Optional.of(this.nullRegion))
                                .build());
        updateCursorPosition(pointerDevice.getPosition());
        pointerDevice.register(this);

        //if the role object is destroyed, the surface should be 'reset' and become invisible until
        //a new role object of the same type is assigned to it.
        wlPointerResource.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                pointerDevice.unregister(CursorRole.this);
                surface.setPosition(Point.ZERO);
                //TODO hide surface
            }
        });
    }

    @Override
    public void beforeCommit() {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();

        surface.setPendingState(surface.getPendingState()
                                        .toBuilder()
                                        .inputRegion(Optional.of(this.nullRegion))
                                        .build());
    }

    @Subscribe
    public void handle(final Motion motion){
        updateCursorPosition(motion.getPoint());
    }
}
