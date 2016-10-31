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
import org.freedesktop.wayland.server.Resource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;

public interface ProtocolObject<T extends Resource<?>> {

    /**
     * Associate a new resource object with this protocol object.
     *
     * @param client  The client owning the newly created resource.
     * @param version The version desired by the client for the new resource.
     * @param id      The id for the new resource, as provided by the client
     *
     * @return the newly created resource.
     */
    default T add(final Client client,
                  final int version,
                  final int id) {
        //FIXME check if version is supported by compositor.

        final T resource = create(client,
                                  version,
                                  id);
        resource.register(() -> getResources().remove(resource));
        getResources().add(resource);
        return resource;
    }

    /**
     * Create a resource.
     *
     * @param client  The client owning the newly created resource.
     * @param version The version desired by the client for the new resource.
     * @param id      The id for the new resource, as provided by the client
     *
     * @return the newly created resource.
     */
    @Nonnull
    T create(@Nonnull final Client client,
             @Nonnegative final int version,
             final int id);

    /**
     * Get all resources currently associated with this protocol object.
     *
     * @return All associated resources.
     */
    @Nonnull
    Set<T> getResources();
}
