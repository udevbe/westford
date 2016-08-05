/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.wayland.dispmanx;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@AutoFactory(className = "PrivateDispmanxPlatformFactory",
             allowSubclasses = true)
public class DispmanxPlatform implements Platform {

    @Nonnull
    private final DISPMANX_MODEINFO_T modeinfo;

    @Nonnull
    private final List<Optional<DispmanxConnector>> dispmanxConnectors;

    DispmanxPlatform(@Nonnull final DISPMANX_MODEINFO_T modeinfo,
                     @Nonnull final List<Optional<DispmanxConnector>> dispmanxConnectors) {
        this.modeinfo = modeinfo;
        this.dispmanxConnectors = dispmanxConnectors;
    }

    @Nonnull
    public DISPMANX_MODEINFO_T getModeinfo() {
        return this.modeinfo;
    }

    @Nonnull
    @Override
    public List<Optional<DispmanxConnector>> getConnectors() {
        return this.dispmanxConnectors;
    }
}
