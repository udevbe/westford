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
package org.westmalle.compositor.drm;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.compositor.drm.PrivateDrmEventBusFactory;
import org.westmalle.nativ.libdrm.DrmEventContext;
import org.westmalle.nativ.libdrm.Libdrm;
import org.westmalle.nativ.libdrm.Pointerpage_flip_handler;
import org.westmalle.nativ.libdrm.Pointervblank_handler;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DrmEventBusFactory {

    @Nonnull
    private final PrivateDrmEventBusFactory privateDrmEventBusFactory;

    @Inject
    DrmEventBusFactory(@Nonnull final PrivateDrmEventBusFactory privateDrmEventBusFactory) {
        this.privateDrmEventBusFactory = privateDrmEventBusFactory;
    }

    public DrmEventBus create(final int drmFd) {
        final Pointer<DrmEventContext> drmEventContextP = Pointer.malloc(DrmEventContext.SIZE,
                                                                         DrmEventContext.class);

        final DrmEventBus drmEventBus = this.privateDrmEventBusFactory.create(drmFd,
                                                                              drmEventContextP.address);

        final DrmEventContext drmEventContext = drmEventContextP.dref();
        drmEventContext.version(Libdrm.DRM_EVENT_CONTEXT_VERSION);
        drmEventContext.page_flip_handler(Pointerpage_flip_handler.nref(drmEventBus::pageFlipHandler));
        drmEventContext.vblank_handler(Pointervblank_handler.nref(drmEventBus::vblankHandler));

        return drmEventBus;
    }
}
