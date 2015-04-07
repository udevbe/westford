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
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlOutputFactory")
public class WlOutput extends Global<WlOutputResource> implements WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    private final Set<WlOutputResource> resources = Sets.newSetFromMap(new WeakHashMap<>());

    @Inject
    WlOutput(final Display display) {
        super(display,
              WlOutputResource.class,
              VERSION);
    }

    @Override
    public WlOutputResource onBindClient(final Client client,
                                         final int version,
                                         final int id) {
        return add(client,
                   version,
                   id);
    }

    @Nonnull
    @Override
    public Set<WlOutputResource> getResources() {
        return this.resources;
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
}
