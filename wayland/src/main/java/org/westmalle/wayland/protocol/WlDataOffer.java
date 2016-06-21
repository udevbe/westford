//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlDataOfferRequests;
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
