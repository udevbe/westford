package org.westmalle.wayland.xdg.protocol;

import com.google.common.collect.Sets;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.XdgShellRequests;
import org.freedesktop.wayland.server.XdgShellResource;
import org.westmalle.wayland.protocol.ProtocolObject;

import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class XdgShell extends Global<XdgShellResource> implements XdgShellRequests,
                                                                  ProtocolObject<XdgShellResource> {

    private final Set<XdgShellResource> resources = Sets.newSetFromMap(new WeakHashMap<>());

    XdgShell(final Display display) {
        super(display,
              XdgShellResource.class,
              VERSION);
    }

    @Override
    public void destroy(final XdgShellResource requester) {

    }

    @Override
    public void useUnstableVersion(final XdgShellResource requester,
                                   final int version) {

    }

    @Override
    public void getXdgSurface(final XdgShellResource requester,
                              final int id,
                              @Nonnull final WlSurfaceResource surface) {
    }

    @Override
    public void getXdgPopup(final XdgShellResource requester,
                            final int id,
                            @Nonnull final WlSurfaceResource surface,
                            @Nonnull final WlSurfaceResource parent,
                            @Nonnull final
                            WlSeatResource seat,
                            final int serial,
                            final int x,
                            final int y) {

    }

    @Override
    public void pong(final XdgShellResource requester, final int serial) {

    }

    @Nonnull
    @Override
    public Set<XdgShellResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public XdgShellResource create(@Nonnull final Client client, @Nonnegative final int version, final int id) {
        return new XdgShellResource(client,
                                    version,
                                    id,
                                    this);
    }

    @Override
    public XdgShellResource onBindClient(final Client client, final int version, final int id) {
        return null;
    }
}
