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
import com.google.auto.factory.Provided;
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;

import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(className = "WlShellFactory")
public class WlShell extends Global<WlShellResource> implements WlShellRequests, ProtocolObject<WlShellResource> {

    private final Set<WlShellResource> resources = Sets.newHashSet();

    private final WlShellSurfaceFactory wlShellSurfaceFactory;

    WlShell(@Provided final Display display,
            @Provided final WlShellSurfaceFactory wlShellSurfaceFactory) {
        super(display,
              WlShellResource.class,
              VERSION);
        this.wlShellSurfaceFactory = wlShellSurfaceFactory;
    }

    @Override
    public void getShellSurface(final WlShellResource requester,
                                final int id,
                                @Nonnull final WlSurfaceResource surface) {
        final WlSurface wlSurface = (WlSurface) surface.getImplementation();
        final WlShellSurface wlShellSurface = this.wlShellSurfaceFactory.create(wlSurface);
        final WlShellSurfaceResource shellSurfaceResource = wlShellSurface.add(requester.getClient(),
                                                                               requester.getVersion(),
                                                                               id);
        surface.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                remove();
                shellSurfaceResource.destroy();
            }
        });
    }

    @Override
    public WlShellResource onBindClient(final Client client,
                                        final int version,
                                        final int id) {
        //FIXME check if we support requested version.
        return add(client,
                   version,
                   id);
    }

    @Override
    public Set<WlShellResource> getResources() {
        return this.resources;
    }

    @Override
    public WlShellResource create(final Client client,
                                  final int version,
                                  final int id) {
        return new WlShellResource(client,
                                   version,
                                   id,
                                   this);
    }
}
