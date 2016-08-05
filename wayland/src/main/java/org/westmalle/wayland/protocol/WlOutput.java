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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlOutputRequestsV2;
import org.freedesktop.wayland.server.WlOutputResource;
import org.westmalle.wayland.core.Output;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlOutputFactory",
             allowSubclasses = true)
public class WlOutput extends Global<WlOutputResource> implements WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    private final Set<WlOutputResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    @Nonnull
    private final Output output;

    WlOutput(@Provided final Display display,
             @Nonnull final Output output) {
        super(display,
              WlOutputResource.class,
              VERSION);
        this.output = output;
    }

    @Override
    public WlOutputResource onBindClient(final Client client,
                                         final int version,
                                         final int id) {
        final WlOutputResource wlOutputResource = add(client,
                                                      version,
                                                      id);
        this.output.notifyGeometry(wlOutputResource)
                   .notifyMode(wlOutputResource);
        if (wlOutputResource.getVersion() >= 2) {
            wlOutputResource.done();
        }
        return wlOutputResource;
    }

    @Nonnull
    @Override
    public WlOutputResource create(@Nonnull final Client client,
                                   @Nonnegative final int version,
                                   final int id) {
        return new WlOutputResource(client,
                                    version,
                                    id,
                                    this);
    }

    @Nonnull
    @Override
    public Set<WlOutputResource> getResources() {
        return this.resources;
    }

    @Nonnull
    public Output getOutput() {
        return this.output;
    }
}
