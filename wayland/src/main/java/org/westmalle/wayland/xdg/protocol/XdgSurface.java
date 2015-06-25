package org.westmalle.wayland.xdg.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.common.collect.Sets;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlOutputResource;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.XdgSurfaceRequests;
import org.freedesktop.wayland.server.XdgSurfaceResource;
import org.westmalle.wayland.protocol.ProtocolObject;
import org.westmalle.wayland.xdg.output.Surface;

import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoFactory(className = "XdgSurfaceFactory")
public class XdgSurface implements XdgSurfaceRequests,
                                   ProtocolObject<XdgSurfaceResource> {

    private final Set<XdgSurfaceResource> resources = Sets.newSetFromMap(new WeakHashMap<>());
    private final Surface surface;

    XdgSurface(final Surface surface) {
        this.surface = surface;
    }

    @Override
    public void destroy(final XdgSurfaceResource requester) {

    }

    @Override
    public void setParent(final XdgSurfaceResource requester,
                          final XdgSurfaceResource parent) {

    }

    @Override
    public void setTitle(final XdgSurfaceResource requester,
                         @Nonnull final String title) {

    }

    @Override
    public void setAppId(final XdgSurfaceResource requester,
                         @Nonnull final String appId) {

    }

    @Override
    public void showWindowMenu(final XdgSurfaceResource requester,
                               @Nonnull final WlSeatResource seat,
                               final int serial,
                               final int x,
                               final int y) {

    }

    @Override
    public void move(final XdgSurfaceResource requester,
                     @Nonnull final WlSeatResource seat,
                     final int serial) {

    }

    @Override
    public void resize(final XdgSurfaceResource requester,
                       @Nonnull final WlSeatResource seat,
                       final int serial,
                       final int edges) {

    }

    @Override
    public void ackConfigure(final XdgSurfaceResource requester,
                             final int serial) {

    }

    @Override
    public void setWindowGeometry(final XdgSurfaceResource requester,
                                  final int x,
                                  final int y,
                                  final int width,
                                  final int height) {

    }

    @Override
    public void setMaximized(final XdgSurfaceResource requester) {

    }

    @Override
    public void unsetMaximized(final XdgSurfaceResource requester) {

    }

    @Override
    public void setFullscreen(final XdgSurfaceResource requester,
                              final WlOutputResource output) {

    }

    @Override
    public void unsetFullscreen(final XdgSurfaceResource requester) {

    }

    @Override
    public void setMinimized(final XdgSurfaceResource requester) {

    }

    @Nonnull
    @Override
    public Set<XdgSurfaceResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public XdgSurfaceResource create(@Nonnull final Client client,
                                     @Nonnegative final int version,
                                     final int id) {
        return new XdgSurfaceResource(client,
                                      version,
                                      id,
                                      this);
    }
}
