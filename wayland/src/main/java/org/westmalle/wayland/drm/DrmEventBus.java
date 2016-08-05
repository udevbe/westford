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
        final Pointer<Object> drmPageFlipCallbackPointer = Pointer.wrap(Object.class,
                                                                        user_data);
        final DrmPageFlipCallback drmPageFlipCallback = (DrmPageFlipCallback) drmPageFlipCallbackPointer.dref();
        drmPageFlipCallback.onPageFlip(sequence,
                                       tv_sec,
                                       tv_usec);
        drmPageFlipCallbackPointer.close();
    }

    public void vblankHandler(final int fd,
                              @Unsigned final int sequence,
                              @Unsigned final int tv_sec,
                              @Unsigned final int tv_usec,
                              @Ptr final long user_data) {
        final Pointer<Object> drmPageFlipCallbackPointer = Pointer.wrap(Object.class,
                                                                        user_data);
        final DrmPageFlipCallback drmPageFlipCallback = (DrmPageFlipCallback) drmPageFlipCallbackPointer.dref();
        drmPageFlipCallback.onVBlank(sequence,
                                     tv_sec,
                                     tv_usec);
        drmPageFlipCallbackPointer.close();
    }
}
