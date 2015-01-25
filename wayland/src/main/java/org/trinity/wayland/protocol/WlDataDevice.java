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
import org.freedesktop.wayland.server.*;

import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(className = "WlDataDeviceFactory")
public class WlDataDevice extends EventBus implements WlDataDeviceRequests, ProtocolObject<WlDataDeviceResource> {

    private final Set<WlDataDeviceResource> resources = Sets.newHashSet();

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
    public Set<WlDataDeviceResource> getResources() {
        return this.resources;
    }

    @Override
    public WlDataDeviceResource create(final Client client,
                                       final int version,
                                       final int id) {
        return new WlDataDeviceResource(client,
                                        version,
                                        id,
                                        this);
    }
}
