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
package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(className = "CursorFactory")
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
