package org.westmalle.wayland.xdg.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.common.collect.Sets;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.XdgPopupRequests;
import org.freedesktop.wayland.server.XdgPopupResource;
import org.westmalle.wayland.protocol.ProtocolObject;
import org.westmalle.wayland.xdg.output.Popup;

import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoFactory(className = "XdgPopupFactory")
public class XdgPopup implements XdgPopupRequests,
                                 ProtocolObject<XdgPopupResource> {

    private final Set<XdgPopupResource> resources = Sets.newSetFromMap(new WeakHashMap<>());
    private final Popup popup;

    XdgPopup(final Popup popup) {
        this.popup = popup;
    }

    @Override
    public void destroy(final XdgPopupResource requester) {

    }

    @Nonnull
    @Override
    public Set<XdgPopupResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public XdgPopupResource create(@Nonnull final Client client,
                                   @Nonnegative final int version,
                                   final int id) {
        return new XdgPopupResource(client,
                                    version,
                                    id,
                                    this);
    }
}
