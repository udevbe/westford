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

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlDataSourceRequestsV3;
import org.freedesktop.wayland.server.WlDataSourceResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlDataSourceFactory",
             allowSubclasses = true)
public class WlDataSource implements WlDataSourceRequestsV3, ProtocolObject<WlDataSourceResource> {

    private final Set<WlDataSourceResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    private final List<String>              mimeTypes = new ArrayList<>();

    WlDataSource() {
    }

    @Override
    public void offer(final WlDataSourceResource resource,
                      @Nonnull final String mimeType) {
        this.mimeTypes.add(mimeType);
    }

    @Override
    public void destroy(final WlDataSourceResource resource) {
        resource.destroy();
    }

    @Override
    public void setActions(final WlDataSourceResource requester,
                           final int dndActions) {
        //TODO
    }

    @Nonnull
    @Override
    public WlDataSourceResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlDataSourceResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlDataSourceResource> getResources() {
        return this.resources;
    }
}
