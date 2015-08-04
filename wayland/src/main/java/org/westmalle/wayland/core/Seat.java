package org.westmalle.wayland.core;


import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.shared.WlSeatCapability;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Set;

public class Seat {

    @Nonnull
    private EnumSet<WlSeatCapability> capabilities = EnumSet.noneOf(WlSeatCapability.class);

    @Inject
    Seat() {
    }

    public void emitCapabilities(final Set<WlSeatResource> wlSeatResources) {
        final int capabilitiesFlag = capabilitiesFlag();
        wlSeatResources.forEach(wlSeatResource -> wlSeatResource.capabilities(capabilitiesFlag));
    }

    private int capabilitiesFlag() {
        int flag = 0;
        for (final WlSeatCapability capability : this.capabilities) {
            flag |= capability.getValue();
        }
        return flag;
    }

    public void setCapabilities(@Nonnull final EnumSet<WlSeatCapability> capability) {
        this.capabilities = capability;
    }
}
