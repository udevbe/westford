package org.westmalle.wayland.xdg.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Sets;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.XdgPopupResource;
import org.freedesktop.wayland.server.XdgShellRequests;
import org.freedesktop.wayland.server.XdgShellResource;
import org.freedesktop.wayland.server.XdgSurfaceResource;
import org.westmalle.wayland.protocol.ProtocolObject;
import org.westmalle.wayland.xdg.output.Popup;
import org.westmalle.wayland.xdg.output.PopupFactory;
import org.westmalle.wayland.xdg.output.Surface;
import org.westmalle.wayland.xdg.output.SurfaceFactory;

import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Singleton
@AutoFactory(className = "XdgShellFactory")
public class XdgShell extends Global<XdgShellResource> implements XdgShellRequests,
                                                                  ProtocolObject<XdgShellResource> {

    private final Set<XdgShellResource> resources = Sets.newSetFromMap(new WeakHashMap<>());
    private final SurfaceFactory    surfaceFactory;
    private final XdgSurfaceFactory xdgSurfaceFactory;
    private final PopupFactory      popupFactory;
    private final XdgPopupFactory   xdgPopupFactory;

    XdgShell(@Provided final Display display,
             final org.westmalle.wayland.xdg.output.SurfaceFactory surfaceFactory,
             final org.westmalle.wayland.xdg.protocol.XdgSurfaceFactory xdgSurfaceFactory,
             final org.westmalle.wayland.xdg.output.PopupFactory popupFactory,
             final org.westmalle.wayland.xdg.protocol.XdgPopupFactory xdgPopupFactory) {
        super(display,
              XdgShellResource.class,
              VERSION);
        this.surfaceFactory = surfaceFactory;
        this.xdgSurfaceFactory = xdgSurfaceFactory;
        this.popupFactory = popupFactory;
        this.xdgPopupFactory = xdgPopupFactory;
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
                              @Nonnull final WlSurfaceResource wlSurfaceResource) {
        final Surface    surface    = this.surfaceFactory.create();
        final XdgSurface xdgSurface = this.xdgSurfaceFactory.create(surface);
        final XdgSurfaceResource xdgSurfaceResource = xdgSurface.add(requester.getClient(),
                                                                     VERSION,
                                                                     id);
        wlSurfaceResource.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                remove();
                xdgSurfaceResource.destroy();
            }
        });
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
        //TODO implement xdg popup semantics of creation and destruction.
        final Popup    popup    = this.popupFactory.create();
        final XdgPopup xdgPopup = this.xdgPopupFactory.create(popup);
        final XdgPopupResource xdgPopupResource = xdgPopup.add(requester.getClient(),
                                                               VERSION,
                                                               id);
        surface.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                remove();
                xdgPopupResource.destroy();
            }
        });
    }

    @Override
    public void pong(final XdgShellResource requester,
                     final int serial) {

    }

    @Nonnull
    @Override
    public Set<XdgShellResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public XdgShellResource create(@Nonnull final Client client,
                                   @Nonnegative final int version,
                                   final int id) {
        return new XdgShellResource(client,
                                    version,
                                    id,
                                    this);
    }

    @Override
    public XdgShellResource onBindClient(final Client client,
                                         final int version,
                                         final int id) {
        return null;
    }
}
