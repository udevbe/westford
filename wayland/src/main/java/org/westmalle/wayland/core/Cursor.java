/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(className = "CursorFactory",
             allowSubclasses = true)
public class Cursor {

    @Nonnull
    private WlSurfaceResource wlSurfaceResource;
    @Nonnull
    private Point             hotspot;
    private boolean           hidden;

    Cursor(@Nonnull final WlSurfaceResource wlSurfaceResource,
           @Nonnull final Point hotspot) {
        this.wlSurfaceResource = wlSurfaceResource;
        this.hotspot = hotspot;
    }

    public void updatePosition(final Point pointerPosition) {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        surface.setPosition(pointerPosition.subtract(getHotspot()));
    }

    @Nonnull
    public Point getHotspot() {
        return this.hotspot;
    }

    public void setHotspot(@Nonnull final Point hotspot) {
        this.hotspot = hotspot;
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

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    public void setWlSurfaceResource(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        this.wlSurfaceResource = wlSurfaceResource;
    }
}
