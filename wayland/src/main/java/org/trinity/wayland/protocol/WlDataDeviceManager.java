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

import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton//EAGER
public class WlDataDeviceManager extends Global<WlDataDeviceManagerResource> implements WlDataDeviceManagerRequests, ProtocolObject<WlDataDeviceManagerResource> {

    private final Set<WlDataDeviceManagerResource> resources = Sets.newHashSet();

    private final WlDataSourceFactory wlDataSourceFactory;

    @Inject
    WlDataDeviceManager(final Display display,
                        final WlDataSourceFactory wlDataSourceFactory) {
        super(display,
              WlDataDeviceManagerResource.class,
              VERSION);
        this.wlDataSourceFactory = wlDataSourceFactory;
    }

    @Override
    public WlDataDeviceManagerResource onBindClient(final Client client,
                                                    final int version,
                                                    final int id) {
        return add(client,
                   version,
                   id);
    }

    @Override
    public void createDataSource(final WlDataDeviceManagerResource resource,
                                 final int id) {
        this.wlDataSourceFactory.create()
                                .add(resource.getClient(),
                                     resource.getVersion(),
                                     id);
    }

    @Override
    public void getDataDevice(final WlDataDeviceManagerResource requester,
                              final int id,
                              @Nonnull final WlSeatResource seat) {
        final WlSeat wlSeat = (WlSeat) seat.getImplementation();
//        wlSeat.getWlDataDevice()
//              .add(requester.getClient(),
//                   requester.getVersion(),
//                   id);
    }

    @Override
    public Set<WlDataDeviceManagerResource> getResources() {
        return this.resources;
    }

    @Override
    public WlDataDeviceManagerResource create(final Client client,
                                              final int version,
                                              final int id) {
        return new WlDataDeviceManagerResource(client,
                                               version,
                                               id,
                                               this);
    }
}
