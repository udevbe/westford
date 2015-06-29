package org.westmalle.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;
import org.westmalle.wayland.xdgshell.Popup;
import org.westmalle.wayland.xdgshell.PopupFactory;
import org.westmalle.wayland.xdgshell.Surface;
import org.westmalle.wayland.xdgshell.SurfaceFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.Set;
import java.util.WeakHashMap;

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
             @Provided final org.westmalle.wayland.xdgshell.SurfaceFactory surfaceFactory,
             @Provided final org.westmalle.wayland.protocol.XdgSurfaceFactory xdgSurfaceFactory,
             @Provided final org.westmalle.wayland.xdgshell.PopupFactory popupFactory,
             @Provided final org.westmalle.wayland.protocol.XdgPopupFactory xdgPopupFactory) {
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
        return add(client,
                   version,
                   id);
    }
}
