/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.compositor.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlShellRequests;
import org.freedesktop.wayland.server.WlShellResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellError;
import org.westmalle.compositor.core.Role;
import org.westmalle.compositor.core.Surface;
import org.westmalle.compositor.protocol.WlShellSurfaceFactory;
import org.westmalle.compositor.wlshell.ShellSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

@Singleton
public class WlShell extends Global<WlShellResource> implements WlShellRequests,
                                                                ProtocolObject<WlShellResource> {

    private final Set<WlShellResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final Display                                              display;
    private final WlShellSurfaceFactory                                wlShellSurfaceFactory;
    private final org.westmalle.compositor.wlshell.ShellSurfaceFactory shellSurfaceFactory;

    private final Set<ShellSurface> activeShellSurfaceRoles = new HashSet<>();

    @Inject
    WlShell(@Nonnull final Display display,
            @Nonnull final WlShellSurfaceFactory wlShellSurfaceFactory,
            @Nonnull final org.westmalle.compositor.wlshell.ShellSurfaceFactory shellSurfaceFactory) {
        super(display,
              WlShellResource.class,
              VERSION);
        this.display = display;
        this.wlShellSurfaceFactory = wlShellSurfaceFactory;
        this.shellSurfaceFactory = shellSurfaceFactory;
    }

    @Override
    public void getShellSurface(final WlShellResource requester,
                                final int id,
                                @Nonnull final WlSurfaceResource wlSurfaceResource) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final int pingSerial = this.display.nextSerial();
        final Role role = surface.getRole()
                                 .orElseGet(() -> this.shellSurfaceFactory.create(pingSerial));

        if (role instanceof ShellSurface &&
            !this.activeShellSurfaceRoles.contains(role)) {

            surface.setRole(role);

            final ShellSurface shellSurface = (ShellSurface) role;
            final WlShellSurface wlShellSurface = this.wlShellSurfaceFactory.create(shellSurface,
                                                                                    wlSurfaceResource);
            final WlShellSurfaceResource wlShellSurfaceResource = wlShellSurface.add(requester.getClient(),
                                                                                     requester.getVersion(),
                                                                                     id);
            this.activeShellSurfaceRoles.add(shellSurface);

            wlShellSurfaceResource.register(() -> this.activeShellSurfaceRoles.remove(shellSurface));
            wlSurfaceResource.register(wlShellSurfaceResource::destroy);

            shellSurface.pong(wlShellSurfaceResource,
                              pingSerial);
        }
        else {
            requester.getClient()
                     .getObject(Display.OBJECT_ID)
                     .postError(WlShellError.ROLE.value,
                                String.format("Desired shell surface already has another role (%s)",
                                              role.getClass()
                                                  .getSimpleName()));
        }
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
    public WlShellResource create(@Nonnull final Client client,
                                  @Nonnegative final int version,
                                  final int id) {
        return new WlShellResource(client,
                                   version,
                                   id,
                                   this);
    }

    @Nonnull
    @Override
    public Set<WlShellResource> getResources() {
        return this.resources;
    }
}
