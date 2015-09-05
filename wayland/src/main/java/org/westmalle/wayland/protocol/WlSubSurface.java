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
import org.freedesktop.wayland.server.WlSubsurfaceRequests;
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.Subsurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlSubSurfaceFactory")
public class WlSubSurface implements WlSubsurfaceRequests, ProtocolObject<WlSubsurfaceResource> {

    private final Set<WlSubsurfaceResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    @Nonnull
    private final Subsurface subsurface;
    @Nonnull
    private final WlSurfaceResource surface;
    @Nonnull
    private final WlSurfaceResource parent;

    WlSubSurface(@Nonnull final Subsurface subsurface,
                 @Nonnull final WlSurfaceResource surface,
                 @Nonnull final WlSurfaceResource parent) {
        this.subsurface = subsurface;
        this.surface = surface;
        this.parent = parent;
    }

    @Nonnull
    @Override
    public WlSubsurfaceResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlSubsurfaceResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlSubsurfaceResource> getResources() {
        return this.resources;
    }

    @Override
    public void destroy(final WlSubsurfaceResource resource) {
        resource.destroy();
    }

    @Override
    public void setPosition(final WlSubsurfaceResource requester,
                            final int x,
                            final int y) {

    }

    @Override
    public void placeAbove(final WlSubsurfaceResource requester,
                           @Nonnull final WlSurfaceResource sibling) {

    }

    @Override
    public void placeBelow(final WlSubsurfaceResource requester,
                           @Nonnull final WlSurfaceResource sibling) {

    }

    @Override
    public void setSync(final WlSubsurfaceResource requester) {

    }

    @Override
    public void setDesync(final WlSubsurfaceResource requester) {

    }
}
