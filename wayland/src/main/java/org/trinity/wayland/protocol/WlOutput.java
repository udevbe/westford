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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton//Eager
public class WlOutput extends Global<WlOutputResource> implements WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    private final Set<WlOutputResource> resources = Sets.newHashSet();

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

    @Override
    public Set<WlOutputResource> getResources() {
        return this.resources;
    }

    @Override
    public WlOutputResource create(final Client client,
                                   final int version,
                                   final int id) {
        return new WlOutputResource(client,
                                    version,
                                    id,
                                    this);
    }
}
