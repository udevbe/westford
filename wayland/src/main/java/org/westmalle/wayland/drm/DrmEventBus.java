package org.westmalle.wayland.drm;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.nativ.libdrm.Libdrm;

import javax.annotation.Nonnull;


@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmEventBusFactory")
public class DrmEventBus implements EventLoop.FileDescriptorEventHandler {


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
        try (final Pointer<DrmPageFlipCallback> drmPageFlipCallbackPointer = Pointer.wrap(DrmPageFlipCallback.class,
                                                                                          user_data)) {
            drmPageFlipCallbackPointer
                    .dref()
                    .onPageFlip(sequence,
                                tv_sec,
                                tv_usec);
        }
    }

    public void vblankHandler(final int fd,
                              @Unsigned final int sequence,
                              @Unsigned final int tv_sec,
                              @Unsigned final int tv_usec,
                              @Ptr final long user_data) {
        try (final Pointer<DrmPageFlipCallback> drmPageFlipCallbackPointer = Pointer.wrap(DrmPageFlipCallback.class,
                                                                                          user_data)) {
            drmPageFlipCallbackPointer
                    .dref()
                    .onVBlank(sequence,
                              tv_sec,
                              tv_usec);
        }
    }
}
