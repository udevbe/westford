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
import org.freedesktop.wayland.server.WlTouchRequestsV5;
import org.freedesktop.wayland.server.WlTouchResource;
import org.westmalle.compositor.core.TouchDevice;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class WlTouch implements WlTouchRequestsV5, ProtocolObject<WlTouchResource> {

    private final Set<WlTouchResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    private final TouchDevice touchDevice;

    @Inject
    WlTouch(final TouchDevice touchDevice) {
        this.touchDevice = touchDevice;
    }

    @Override
    public void release(final WlTouchResource resource) {
        resource.destroy();
    }

    @Nonnull
    @Override
    public WlTouchResource create(@Nonnull final Client client,
                                  @Nonnegative final int version,
                                  final int id) {
        return new WlTouchResource(client,
                                   version,
                                   id,
                                   this);
    }

    @Nonnull
    @Override
    public Set<WlTouchResource> getResources() {
        return this.resources;
    }

    public TouchDevice getTouchDevice() {
        return this.touchDevice;
    }
}
