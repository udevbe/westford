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

import com.google.common.collect.Sets;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlDataDeviceRequestsV2;
import org.freedesktop.wayland.server.WlDataDeviceResource;
import org.freedesktop.wayland.server.WlDataSourceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;

import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;

public class WlDataDevice implements WlDataDeviceRequestsV2, ProtocolObject<WlDataDeviceResource> {

    private final Set<WlDataDeviceResource> resources = Sets.newSetFromMap(new WeakHashMap<>());

    @Inject
    WlDataDevice() {}

    @Override
    public void startDrag(final WlDataDeviceResource requester,
                          final WlDataSourceResource source,
                          @Nonnull final WlSurfaceResource origin,
                          final WlSurfaceResource icon,
                          final int serial) {

    }

    @Override
    public void setSelection(final WlDataDeviceResource requester,
                             final WlDataSourceResource source,
                             final int serial) {

    }

    @Override
    public void release(final WlDataDeviceResource requester) {

    }

    @Nonnull
    @Override
    public Set<WlDataDeviceResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlDataDeviceResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlDataDeviceResource(client,
                                        version,
                                        id,
                                        this);
    }
}
