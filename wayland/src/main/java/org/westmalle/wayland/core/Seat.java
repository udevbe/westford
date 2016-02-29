package org.westmalle.wayland.core;


import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.shared.WlSeatCapability;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

@AutoFactory(className = "SeatFactory",
             allowSubclasses = true)
public class Seat {

    @Nonnull
    private final Object platformImplementation;
    @Nonnull
    private EnumSet<WlSeatCapability> capabilities = EnumSet.noneOf(WlSeatCapability.class);

    Seat(@Nonnull final Object platformImplementation) {
        this.platformImplementation = platformImplementation;
    }

    public void emitCapabilities(@Nonnull final Set<WlSeatResource> wlSeatResources) {
        final int capabilitiesFlag = capabilitiesFlag();
        wlSeatResources.forEach(wlSeatResource -> wlSeatResource.capabilities(capabilitiesFlag));
    }

    private int capabilitiesFlag() {
        int flag = 0;
        for (final WlSeatCapability capability : this.capabilities) {
            flag |= capability.value;
        }
        return flag;
    }

    public void setCapabilities(@Nonnull final EnumSet<WlSeatCapability> capability) {
        this.capabilities = capability;
    }

    @Nonnull
    public Object getPlatformImplementation() {
        return this.platformImplementation;
    }
}
