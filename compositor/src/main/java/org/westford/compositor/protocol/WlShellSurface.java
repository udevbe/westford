/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.protocol;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlOutputResource;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlShellSurfaceRequests;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellSurfaceTransient;
import org.westford.compositor.wlshell.ShellSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlShellSurfaceFactory",
             allowSubclasses = true)
public class WlShellSurface implements WlShellSurfaceRequests, ProtocolObject<WlShellSurfaceResource> {

    private final Set<WlShellSurfaceResource> resources = Collections.newSetFromMap(new WeakHashMap<>());
    private final ShellSurface      shellSurface;
    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;

    WlShellSurface(@Nonnull final ShellSurface shellSurface,
                   @Nonnull final WlSurfaceResource wlSurfaceResource) {
        this.shellSurface = shellSurface;
        this.wlSurfaceResource = wlSurfaceResource;
    }

    @Override
    public void pong(final WlShellSurfaceResource requester,
                     final int serial) {
        this.shellSurface.pong(requester,
                               serial);
    }

    @Override
    public void move(final WlShellSurfaceResource requester,
                     @Nonnull final WlSeatResource seat,
                     final int serial) {
        final WlSeat wlSeat = (WlSeat) seat.getImplementation();
        wlSeat.getWlPointerResource(seat)
              .ifPresent(wlPointerResource -> getShellSurface().move(getWlSurfaceResource(),
                                                                     wlPointerResource,
                                                                     serial));
    }

    public ShellSurface getShellSurface() {
        return this.shellSurface;
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    @Override
    public void resize(final WlShellSurfaceResource requester,
                       @Nonnull final WlSeatResource seat,
                       final int serial,
                       final int edges) {
        final WlSeat wlSeat = (WlSeat) seat.getImplementation();
        wlSeat.getWlPointerResource(seat)
              .ifPresent(wlPointerResource -> getShellSurface().resize(requester,
                                                                       getWlSurfaceResource(),
                                                                       wlPointerResource,
                                                                       serial,
                                                                       edges));
    }

    @Override
    public void setToplevel(final WlShellSurfaceResource requester) {
        getShellSurface().toFront(getWlSurfaceResource());
    }

    @Override
    public void setTransient(final WlShellSurfaceResource requester,
                             @Nonnull final WlSurfaceResource parent,
                             final int x,
                             final int y,
                             final int flags) {
        final EnumSet<WlShellSurfaceTransient> transientFlags = EnumSet.noneOf(WlShellSurfaceTransient.class);
        for (final WlShellSurfaceTransient wlShellSurfaceTransient : WlShellSurfaceTransient.values()) {
            if ((wlShellSurfaceTransient.value & flags) != 0) {
                transientFlags.add(wlShellSurfaceTransient);
            }
        }

        getShellSurface().setTransient(getWlSurfaceResource(),
                                       parent,
                                       x,
                                       y,
                                       transientFlags);
    }

    @Override
    public void setFullscreen(final WlShellSurfaceResource requester,
                              final int method,
                              final int framerate,
                              final WlOutputResource output) {

    }

    @Override
    public void setPopup(final WlShellSurfaceResource requester,
                         @Nonnull final WlSeatResource seat,
                         final int serial,
                         @Nonnull final WlSurfaceResource parent,
                         final int x,
                         final int y,
                         final int flags) {

    }

    @Override
    public void setMaximized(final WlShellSurfaceResource requester,
                             final WlOutputResource output) {

    }

    @Override
    public void setTitle(final WlShellSurfaceResource requester,
                         @Nonnull final String title) {
        this.shellSurface.setTitle(Optional.of(title));
    }

    @Override
    public void setClass(final WlShellSurfaceResource requester,
                         @Nonnull final String class_) {
        this.shellSurface.setClazz(Optional.of(class_));
    }

    @Nonnull
    @Override
    public WlShellSurfaceResource create(@Nonnull final Client client,
                                         @Nonnegative final int version,
                                         final int id) {
        return new WlShellSurfaceResource(client,
                                          version,
                                          id,
                                          this);
    }

    @Nonnull
    @Override
    public Set<WlShellSurfaceResource> getResources() {
        return this.resources;
    }
}
