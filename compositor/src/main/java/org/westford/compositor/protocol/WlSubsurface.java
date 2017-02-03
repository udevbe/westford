/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlSubsurfaceRequests;
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlSubsurfaceError;
import org.westford.compositor.core.Point;
import org.westford.compositor.core.Scene;
import org.westford.compositor.core.Subsurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlSubsurfaceFactory")
public class WlSubsurface implements WlSubsurfaceRequests,
                                     ProtocolObject<WlSubsurfaceResource> {

    private final Set<WlSubsurfaceResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    @Nonnull
    private final Subsurface subsurface;
    @Nonnull
    private final Scene      scene;

    WlSubsurface(@Nonnull @Provided final Scene scene,
                 @Nonnull final Subsurface subsurface) {
        this.subsurface = subsurface;
        this.scene = scene;
    }

    @Nonnull
    @Override
    public WlSubsurfaceResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlSubsurfaceResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlSubsurfaceResource> getResources() {
        return this.resources;
    }

    @Override
    public void destroy(final WlSubsurfaceResource resource) {
        resource.destroy();
    }

    @Override
    public void setPosition(final WlSubsurfaceResource wlSubsurfaceResource,
                            final int x,
                            final int y) {
        getSubsurface().getSibling()
                       .setPosition(Point.create(x,
                                                 y));
    }

    @Nonnull
    public Subsurface getSubsurface() {
        return this.subsurface;
    }

    @Override
    public void placeAbove(final WlSubsurfaceResource requester,
                           @Nonnull final WlSurfaceResource sibling) {
        //TODO unit test
        if (isValid(requester,
                    sibling)) {
            getSubsurface().above(sibling);
        }
        else {
            requester.postError(WlSubsurfaceError.BAD_SURFACE.value,
                                "placeAbove request failed. wl_surface is not a sibling or the parent");
        }
    }

    private boolean isValid(final WlSubsurfaceResource requester,
                            final WlSurfaceResource sibling) {
        final Subsurface subsurface = getSubsurface();
        if (subsurface.isInert()) {
            /*
             * we return true here as a the docs say that a subsurface with a destroyed parent should become inert
             * ie we don't care what the sibling argument is, as the request will be ignored anyway.
             */
            return true;
        }

        final WlSurface wlSurface = (WlSurface) requester.getImplementation();

        return wlSurface.getSurface()
                        .getSiblings()
                        .contains(sibling);
    }

    @Override
    public void placeBelow(final WlSubsurfaceResource requester,
                           @Nonnull final WlSurfaceResource sibling) {
        //TODO unit test
        if (isValid(requester,
                    sibling)) {
            getSubsurface().below(sibling);
        }
        else {
            requester.postError(WlSubsurfaceError.BAD_SURFACE.value,
                                "placeBelow request failed. wl_surface is not a sibling or the parent");
        }
    }

    @Override
    public void setSync(final WlSubsurfaceResource requester) {
        getSubsurface().setSync(true);
    }

    @Override
    public void setDesync(final WlSubsurfaceResource requester) {
        getSubsurface().setSync(false);
    }
}
