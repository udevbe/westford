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
package org.westford.compositor.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class ProtocolObjectDummy implements ProtocolObject<Resource<?>> {

    private final Set<Resource<?>> resources = new HashSet<>();

    @Nonnull
    @Override
    public Resource<?> create(@Nonnull final Client client,
                              @Nonnegative final int version,
                              final int id) {
        return mock(Resource.class);
    }

    @Nonnull
    @Override
    public Set<Resource<?>> getResources() {
        return this.resources;
    }
}
