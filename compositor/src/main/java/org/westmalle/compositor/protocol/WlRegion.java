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
import org.freedesktop.wayland.server.WlRegionRequests;
import org.freedesktop.wayland.server.WlRegionResource;
import org.westmalle.compositor.core.Rectangle;
import org.westmalle.compositor.core.Region;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlRegionFactory",
             allowSubclasses = true)
public class WlRegion implements WlRegionRequests, ProtocolObject<WlRegionResource> {

    private final Set<WlRegionResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final Region region;

    WlRegion(final Region region) {
        this.region = region;
    }

    @Nonnull
    @Override
    public WlRegionResource create(@Nonnull final Client client,
                                   @Nonnegative final int version,
                                   final int id) {
        return new WlRegionResource(client,
                                    version,
                                    id,
                                    this);
    }

    @Nonnull
    @Override
    public Set<WlRegionResource> getResources() {
        return this.resources;
    }

    @Override
    public void destroy(final WlRegionResource resource) {
        resource.destroy();
    }

    @Override
    public void add(final WlRegionResource resource,
                    final int x,
                    final int y,
                    @Nonnegative final int width,
                    @Nonnegative final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Got negative width or height");
        }

        this.region.add(Rectangle.create(x,
                                         y,
                                         width,
                                         height));
    }

    @Override
    public void subtract(final WlRegionResource resource,
                         final int x,
                         final int y,
                         @Nonnegative final int width,
                         @Nonnegative final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Got negative width or height");
        }

        this.region.subtract(Rectangle.create(x,
                                              y,
                                              width,
                                              height));
    }

    public Region getRegion() {
        return this.region;
    }
}
