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
package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlPointerRequestsV3;
import org.freedesktop.wayland.server.WlPointerRequestsV5;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlPointerError;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.Role;
import org.westmalle.wayland.core.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;


public class WlPointer implements WlPointerRequestsV5, ProtocolObject<WlPointerResource> {

    private final Set<WlPointerResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final PointerDevice pointerDevice;

    @Inject
    WlPointer(final PointerDevice pointerDevice) {
        this.pointerDevice = pointerDevice;
    }

    @Override
    public void setCursor(final WlPointerResource wlPointerResource,
                          final int serial,
                          @Nullable final WlSurfaceResource wlSurfaceResource,
                          final int hotspotX,
                          final int hotspotY) {
        if (wlSurfaceResource == null) {
            getPointerDevice().removeCursor(wlPointerResource,
                                            serial);
        }
        else {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Surface   surface   = wlSurface.getSurface();

            final Role role = surface.getRole()
                                     .orElseGet(this::getPointerDevice);

            if (role.equals(getPointerDevice())) {
                final PointerDevice pointerDevice = (PointerDevice) role;
                surface.setRole(pointerDevice);
                pointerDevice.setCursor(wlPointerResource,
                                        serial,
                                        wlSurfaceResource,
                                        hotspotX,
                                        hotspotY);
            }
            else {
                wlPointerResource.getClient()
                                 .getObject(Display.OBJECT_ID)
                                 .postError(WlPointerError.ROLE.value,
                                            String.format("Desired cursor surface already has another role (%s)",
                                                          role.getClass()
                                                              .getSimpleName()));
            }
        }
    }

    public PointerDevice getPointerDevice() {
        return this.pointerDevice;
    }

    @Override
    public void release(final WlPointerResource resource) {
        resource.destroy();
    }

    @Nonnull
    @Override
    public WlPointerResource create(@Nonnull final Client client,
                                    @Nonnegative final int version,
                                    final int id) {
        return new WlPointerResource(client,
                                     version,
                                     id,
                                     this);
    }

    @Nonnull
    @Override
    public Set<WlPointerResource> getResources() {
        return this.resources;
    }
}
