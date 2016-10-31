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
package org.westford.compositor.dispmanx;


import com.google.auto.factory.AutoFactory;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.RenderPlatform;
import org.westford.compositor.core.events.RenderOutputDestroyed;
import org.westford.compositor.core.events.RenderOutputNew;
import org.westford.nativ.libbcm_host.DISPMANX_MODEINFO_T;

import javax.annotation.Nonnull;
import java.util.List;

@AutoFactory(className = "PrivateDispmanxPlatformFactory",
             allowSubclasses = true)
public class DispmanxPlatform implements RenderPlatform {

    @Nonnull
    private final DISPMANX_MODEINFO_T modeinfo;

    @Nonnull
    private final List<DispmanxOutput> dispmanxOutputs;
    private final Signal<RenderOutputNew, Slot<RenderOutputNew>>             renderOutputNewSignal       = new Signal<>();
    private final Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> renderOutputDestroyedSignal = new Signal<>();

    DispmanxPlatform(@Nonnull final DISPMANX_MODEINFO_T modeinfo,
                     @Nonnull final List<DispmanxOutput> dispmanxOutputs) {
        this.modeinfo = modeinfo;
        this.dispmanxOutputs = dispmanxOutputs;
    }

    @Nonnull
    public DISPMANX_MODEINFO_T getModeinfo() {
        return this.modeinfo;
    }

    @Nonnull
    public List<DispmanxOutput> getRenderOutputs() {
        return this.dispmanxOutputs;
    }

    @Override
    public Signal<RenderOutputNew, Slot<RenderOutputNew>> getRenderOutputNewSignal() {
        return this.renderOutputNewSignal;
    }

    @Override
    public Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> getRenderOutputDestroyedSignal() {
        return this.renderOutputDestroyedSignal;
    }
}
