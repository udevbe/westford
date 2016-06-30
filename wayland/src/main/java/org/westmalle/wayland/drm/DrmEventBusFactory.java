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
