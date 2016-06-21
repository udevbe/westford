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
import org.freedesktop.wayland.server.WlDataSourceRequests;
import org.freedesktop.wayland.server.WlDataSourceRequestsV3;
import org.freedesktop.wayland.server.WlDataSourceResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlDataSourceFactory",
             allowSubclasses = true)
public class WlDataSource implements WlDataSourceRequestsV3, ProtocolObject<WlDataSourceResource> {

    private final Set<WlDataSourceResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    private final List<String>              mimeTypes = new ArrayList<>();

    WlDataSource() {
    }

    @Override
    public void offer(final WlDataSourceResource resource,
                      @Nonnull final String mimeType) {
        this.mimeTypes.add(mimeType);
    }

    @Override
    public void destroy(final WlDataSourceResource resource) {
        resource.destroy();
    }

    @Override
    public void setActions(final WlDataSourceResource requester,
                           final int dndActions) {
        //TODO
    }

    @Nonnull
    @Override
    public WlDataSourceResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlDataSourceResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlDataSourceResource> getResources() {
        return this.resources;
    }
}
