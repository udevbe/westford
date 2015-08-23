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
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlOutputRequestsV2;
import org.freedesktop.wayland.server.WlOutputResource;
import org.westmalle.wayland.core.Output;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlOutputFactory",
             allowSubclasses = true)
public class WlOutput extends Global<WlOutputResource> implements WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    private final Set<WlOutputResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    @Nonnull
    private final Output output;

    WlOutput(@Provided final Display display,
             @Nonnull final Output output) {
        super(display,
              WlOutputResource.class,
              VERSION);
        this.output = output;
    }

    @Override
    public WlOutputResource onBindClient(final Client client,
                                         final int version,
                                         final int id) {
        final WlOutputResource wlOutputResource = add(client,
                                                      version,
                                                      id);
        this.output.notifyGeometry(wlOutputResource)
                   .notifyMode(wlOutputResource);
        wlOutputResource.done();
        return wlOutputResource;
    }

    @Nonnull
    @Override
    public WlOutputResource create(@Nonnull final Client client,
                                   @Nonnegative final int version,
                                   final int id) {
        return new WlOutputResource(client,
                                    version,
                                    id,
                                    this);
    }

    @Nonnull
    @Override
    public Set<WlOutputResource> getResources() {
        return this.resources;
    }

    @Nonnull
    public Output getOutput() {
        return this.output;
    }
}
