//Copyright 2016 Erik De Rijcke
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
