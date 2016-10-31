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
package org.westford.compositor.core;

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
    Seat() {}

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
}
