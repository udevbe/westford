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
import org.freedesktop.wayland.server.WlRegionRequests;
import org.freedesktop.wayland.server.WlRegionResource;
import org.westmalle.wayland.core.Rectangle;
import org.westmalle.wayland.core.Region;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlRegionFactory")
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
