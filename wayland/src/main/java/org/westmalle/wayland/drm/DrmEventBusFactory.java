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
package org.westmalle.wayland.drm;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.nativ.libdrm.DrmEventContext;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libdrm.Pointerpage_flip_handler;
import org.westmalle.wayland.nativ.libdrm.Pointervblank_handler;

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
