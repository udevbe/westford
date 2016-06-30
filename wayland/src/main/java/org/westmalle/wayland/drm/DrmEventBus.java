package org.westmalle.wayland.drm;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.drm.events.DrmPageFlip;
import org.westmalle.wayland.drm.events.DrmVBlank;
import org.westmalle.wayland.nativ.libdrm.Libdrm;

import javax.annotation.Nonnull;


@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmEventBusFactory")
public class DrmEventBus implements EventLoop.FileDescriptorEventHandler {

    private final Signal<DrmPageFlip, Slot<DrmPageFlip>> pageFlipSignal = new Signal<>();
    private final Signal<DrmVBlank, Slot<DrmVBlank>>     vBlankSignal   = new Signal<>();

    @Nonnull
    private final Libdrm libdrm;
    private final int    drmFd;
    private final long   drmEventContext;

    DrmEventBus(@Provided @Nonnull final Libdrm libdrm,
                final int drmFd,
                final long drmEventContext) {
        this.libdrm = libdrm;
        this.drmFd = drmFd;
        this.drmEventContext = drmEventContext;
    }

    @Override
    public int handle(final int fd,
                      final int mask) {
        this.libdrm.drmHandleEvent(this.drmFd,
                                   this.drmEventContext);
        return 0;
    }

    public void pageFlipHandler(final int fd,
                                @Unsigned final int sequence,
                                @Unsigned final int tv_sec,
                                @Unsigned final int tv_usec,
                                @Ptr final long user_data) {
        this.pageFlipSignal.emit(DrmPageFlip.create(sequence,
                                                    tv_sec,
                                                    tv_usec,
                                                    user_data));
    }

    public void vblankHandler(final int fd,
                              @Unsigned final int sequence,
                              @Unsigned final int tv_sec,
                              @Unsigned final int tv_usec,
                              @Ptr final long user_data) {
        this.vBlankSignal.emit(DrmVBlank.create(sequence,
                                                tv_sec,
                                                tv_usec,
                                                user_data));
    }

    public Signal<DrmPageFlip, Slot<DrmPageFlip>> getPageFlipSignal() {
        return this.pageFlipSignal;
    }

    public Signal<DrmVBlank, Slot<DrmVBlank>> getVBlankSignal() {
        return this.vBlankSignal;
    }
}
