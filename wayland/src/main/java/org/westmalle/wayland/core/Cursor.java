package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Optional;

@AutoFactory(className = "CursorFactory")
public class Cursor {

    private final NullRegion        nullRegion;
    private       WlSurfaceResource wlSurfaceResource;
    private       Point             hotspot;
    private       boolean           hidden;

    Cursor(@Provided final NullRegion nullRegion) {
        this.nullRegion = nullRegion;
    }

    public void updatePosition(final Point pointerPosition) {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        surface.setPosition(pointerPosition.subtract(getHotspot()));
    }

    public Point getHotspot() {
        return this.hotspot;
    }

    public void update(final WlSurfaceResource wlSurfaceResource,
                       final Point hotspot) {
        this.wlSurfaceResource = wlSurfaceResource;
        this.hotspot = hotspot;

        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        surface.setState(surface.getState()
                                .toBuilder()
                                .inputRegion(Optional.of(this.nullRegion))
                                .build());
        surface.setPosition(Point.ZERO);
    }

    public void hide() {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.setState(surface.getState()
                                .toBuilder()
                                .buffer(Optional.<WlBufferResource>empty())
                                .build());
        surface.setPosition(Point.ZERO);

        this.hidden = true;
    }

    public void show() {
        this.hidden = false;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }
}
