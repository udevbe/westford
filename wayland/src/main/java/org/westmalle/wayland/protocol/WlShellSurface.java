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
import org.freedesktop.wayland.server.WlOutputResource;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlShellSurfaceRequests;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellSurfaceTransient;
import org.westmalle.wayland.wlshell.ShellSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlShellSurfaceFactory")
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
            if ((wlShellSurfaceTransient.getValue() & flags) != 0) {
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
