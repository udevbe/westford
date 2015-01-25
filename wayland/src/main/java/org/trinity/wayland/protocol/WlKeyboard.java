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
import org.freedesktop.wayland.server.WlKeyboardRequestsV3;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.trinity.wayland.output.Keyboard;

import java.util.Set;

@AutoFactory(className = "WlKeyboardFactory")
public class WlKeyboard extends EventBus implements WlKeyboardRequestsV3, ProtocolObject<WlKeyboardResource> {

    private final Set<WlKeyboardResource> resources = Sets.newHashSet();
    private final Keyboard keyboard;

    WlKeyboard(final Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void release(final WlKeyboardResource resource) {

    }

    @Override
    public Set<WlKeyboardResource> getResources() {
        return this.resources;
    }

    @Override
    public WlKeyboardResource create(final Client client,
                                     final int version,
                                     final int id) {
        return new WlKeyboardResource(client,
                                      version,
                                      id,
                                      this);
    }

    public Keyboard getKeyboard() {
        return this.keyboard;
    }
}
