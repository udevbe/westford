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
package org.westmalle.compositor.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlDataDeviceRequestsV3;
import org.freedesktop.wayland.server.WlDataDeviceResource;
import org.freedesktop.wayland.server.WlDataSourceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class WlDataDevice implements WlDataDeviceRequestsV3, ProtocolObject<WlDataDeviceResource> {

    private final Set<WlDataDeviceResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    @Inject
    WlDataDevice() {}

    @Override
    public void startDrag(final WlDataDeviceResource requester,
                          final WlDataSourceResource source,
                          @Nonnull final WlSurfaceResource origin,
                          final WlSurfaceResource icon,
                          final int serial) {

    }

    @Override
    public void setSelection(final WlDataDeviceResource requester,
                             final WlDataSourceResource source,
                             final int serial) {

    }

    @Override
    public void release(final WlDataDeviceResource requester) {

    }

    @Nonnull
    @Override
    public WlDataDeviceResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlDataDeviceResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlDataDeviceResource> getResources() {
        return this.resources;
    }
}
