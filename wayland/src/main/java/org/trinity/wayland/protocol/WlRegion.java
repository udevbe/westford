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
package org.trinity.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlRegionRequests;
import org.freedesktop.wayland.server.WlRegionResource;
import org.trinity.wayland.output.Region;

import javax.annotation.Nonnegative;
import javax.media.nativewindow.util.Rectangle;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@AutoFactory(className = "WlRegionFactory")
public class WlRegion extends EventBus implements WlRegionRequests, ProtocolObject<WlRegionResource> {

    private final Set<WlRegionResource> resources = Sets.newHashSet();

    private final Region region;

    WlRegion(final Region region) {
        this.region = region;
    }

    @Override
    public Set<WlRegionResource> getResources() {
        return this.resources;
    }

    @Override
    public WlRegionResource create(final Client client,
                                   final int version,
                                   final int id) {
        return new WlRegionResource(client,
                                    version,
                                    id,
                                    this);
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
        checkArgument(width > 0);
        checkArgument(height > 0);

        this.region.add(new Rectangle(x,
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
        checkArgument(width > 0);
        checkArgument(height > 0);

        this.region.subtract(new Rectangle(x,
                                           y,
                                           width,
                                           height));
    }

    public Region getRegion() {
        return this.region;
    }
}
