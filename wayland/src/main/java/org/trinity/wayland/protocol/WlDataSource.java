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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlDataSourceRequests;
import org.freedesktop.wayland.server.WlDataSourceResource;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

@AutoFactory(className = "WlDataSourceFactory")
public class WlDataSource extends EventBus implements WlDataSourceRequests, ProtocolObject<WlDataSourceResource> {

    private final Set<WlDataSourceResource> resources = Sets.newHashSet();
    private final List<String>              mimeTypes = Lists.newArrayList();

    WlDataSource() {
    }

    @Override
    public void offer(final WlDataSourceResource resource,
                      @Nonnull final String mimeType) {
        this.mimeTypes.add(mimeType);
    }

    @Override
    public Set<WlDataSourceResource> getResources() {
        return this.resources;
    }

    @Override
    public WlDataSourceResource create(final Client client,
                                       final int version,
                                       final int id) {
        return new WlDataSourceResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Override
    public void destroy(final WlDataSourceResource resource) {
        resource.destroy();
    }
}
