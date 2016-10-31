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
import org.freedesktop.wayland.server.WlDataOfferRequestsV3;
import org.freedesktop.wayland.server.WlDataOfferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlDataOfferFactory",
             allowSubclasses = true)
public class WlDataOffer implements WlDataOfferRequestsV3, ProtocolObject<WlDataOfferResource> {

    private final Set<WlDataOfferResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    WlDataOffer() {
    }

    @Override
    public void accept(final WlDataOfferResource resource,
                       final int serial,
                       final String mimeType) {

    }

    @Override
    public void receive(final WlDataOfferResource resource,
                        @Nonnull final String mimeType,
                        final int fd) {

    }

    @Override
    public void destroy(final WlDataOfferResource resource) {
        resource.destroy();
    }

    @Override
    public void finish(final WlDataOfferResource requester) {
        //TODO
    }

    @Override
    public void setActions(final WlDataOfferResource requester,
                           final int dndActions,
                           final int preferredAction) {
        //TODO
    }

    @Nonnull
    @Override
    public WlDataOfferResource create(@Nonnull final Client client,
                                      @Nonnegative final int version,
                                      final int id) {
        return new WlDataOfferResource(client,
                                       version,
                                       id,
                                       this);
    }

    @Nonnull
    @Override
    public Set<WlDataOfferResource> getResources() {
        return this.resources;
    }
}
