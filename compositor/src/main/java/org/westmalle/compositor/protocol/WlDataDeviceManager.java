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
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlDataDeviceManagerRequestsV3;
import org.freedesktop.wayland.server.WlDataDeviceManagerResource;
import org.freedesktop.wayland.server.WlSeatResource;
import org.westmalle.compositor.protocol.WlDataSourceFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Singleton
public class WlDataDeviceManager extends Global<WlDataDeviceManagerResource> implements WlDataDeviceManagerRequestsV3, ProtocolObject<WlDataDeviceManagerResource> {

    private final Set<WlDataDeviceManagerResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final WlDataSourceFactory wlDataSourceFactory;

    @Inject
    WlDataDeviceManager(final Display display,
                        final WlDataSourceFactory wlDataSourceFactory) {
        super(display,
              WlDataDeviceManagerResource.class,
              VERSION);
        this.wlDataSourceFactory = wlDataSourceFactory;
    }

    @Override
    public WlDataDeviceManagerResource onBindClient(final Client client,
                                                    final int version,
                                                    final int id) {
        return add(client,
                   version,
                   id);
    }

    @Override
    public void createDataSource(final WlDataDeviceManagerResource resource,
                                 final int id) {
        this.wlDataSourceFactory.create()
                                .add(resource.getClient(),
                                     resource.getVersion(),
                                     id);
    }

    @Override
    public void getDataDevice(final WlDataDeviceManagerResource requester,
                              final int id,
                              @Nonnull final WlSeatResource seat) {
        final WlSeat wlSeat = (WlSeat) seat.getImplementation();
        wlSeat.getWlDataDevice()
              .add(requester.getClient(),
                   requester.getVersion(),
                   id);
    }

    @Nonnull
    @Override
    public WlDataDeviceManagerResource create(@Nonnull final Client client,
                                              @Nonnegative final int version,
                                              final int id) {
        return new WlDataDeviceManagerResource(client,
                                               version,
                                               id,
                                               this);
    }

    @Nonnull
    @Override
    public Set<WlDataDeviceManagerResource> getResources() {
        return this.resources;
    }
}
