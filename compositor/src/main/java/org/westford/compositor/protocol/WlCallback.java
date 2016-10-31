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
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlCallbackRequests;
import org.freedesktop.wayland.server.WlCallbackResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlCallbackFactory",
             allowSubclasses = true)
public class WlCallback implements WlCallbackRequests, ProtocolObject<WlCallbackResource> {

    private final Set<WlCallbackResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    @Inject
    WlCallback() {
    }

    @Nonnull
    @Override
    public WlCallbackResource create(@Nonnull final Client client,
                                     @Nonnegative final int version,
                                     final int id) {
        return new WlCallbackResource(client,
                                      version,
                                      id,
                                      this);
    }

    @Nonnull
    @Override
    public Set<WlCallbackResource> getResources() {
        return this.resources;
    }
}
