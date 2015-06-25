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
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;
import org.westmalle.wayland.wlshell.ShellSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlShellFactory")
public class WlShell extends Global<WlShellResource> implements WlShellRequests, ProtocolObject<WlShellResource> {

    private final Set<WlShellResource> resources = Sets.newSetFromMap(new WeakHashMap<>());

    private final Display                                           display;
    private final WlShellSurfaceFactory                             wlShellSurfaceFactory;
    private final org.westmalle.wayland.wlshell.ShellSurfaceFactory shellSurfaceFactory;
    private final WlCompositor                                      wlCompositor;

    WlShell(@Provided final Display display,
            @Provided final WlShellSurfaceFactory wlShellSurfaceFactory,
            @Provided final org.westmalle.wayland.wlshell.ShellSurfaceFactory shellSurfaceFactory,
            @Nonnull final WlCompositor wlCompositor) {
        super(display,
              WlShellResource.class,
              VERSION);
        this.display = display;
        this.wlShellSurfaceFactory = wlShellSurfaceFactory;
        this.shellSurfaceFactory = shellSurfaceFactory;
        this.wlCompositor = wlCompositor;
    }

    @Override
    public void getShellSurface(final WlShellResource requester,
                                final int id,
                                @Nonnull final WlSurfaceResource wlSurfaceResource) {
        //TODO check if the given wlSurfaceResource doesn't have a role assigned to it already.

        final int pingSerial = this.display.nextSerial();
        final ShellSurface shellSurface = this.shellSurfaceFactory.create(this.wlCompositor,
                                                                          pingSerial);
        final WlShellSurface wlShellSurface = this.wlShellSurfaceFactory.create(shellSurface,
                                                                                wlSurfaceResource);
        final WlShellSurfaceResource shellSurfaceResource = wlShellSurface.add(requester.getClient(),
                                                                               requester.getVersion(),
                                                                               id);
        wlSurfaceResource.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                remove();
                shellSurfaceResource.destroy();
            }
        });

        shellSurface.pong(shellSurfaceResource,
                          pingSerial);
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

    @Nonnull
    @Override
    public Set<WlShellResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlShellResource create(@Nonnull final Client client,
                                  @Nonnegative final int version,
                                  final int id) {
        return new WlShellResource(client,
                                   version,
                                   id,
                                   this);
    }
}
