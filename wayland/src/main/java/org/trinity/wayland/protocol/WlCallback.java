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
import org.freedesktop.wayland.server.WlCallbackRequests;
import org.freedesktop.wayland.server.WlCallbackResource;

import javax.inject.Inject;
import java.util.Set;

@AutoFactory(className = "WlCallbackFactory")
public class WlCallback extends EventBus implements WlCallbackRequests, ProtocolObject<WlCallbackResource> {

    private final Set<WlCallbackResource> resources = Sets.newHashSet();

    @Inject
    WlCallback() {
    }

    @Override
    public Set<WlCallbackResource> getResources() {
        return this.resources;
    }

    @Override
    public WlCallbackResource create(final Client client,
                                     final int version,
                                     final int id) {
        return new WlCallbackResource(client,
                                      version,
                                      id,
                                      this);
    }
}
