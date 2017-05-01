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
package org.westford.compositor.drm

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Unsigned
import org.freedesktop.wayland.server.EventLoop
import org.westford.nativ.libdrm.Libdrm

@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmEventBusFactory") class DrmEventBus(@param:Provided private val libdrm: Libdrm,
                                                                        private val drmFd: Int,
                                                                        private val drmEventContext: Long) : EventLoop.FileDescriptorEventHandler {

    override fun handle(fd: Int,
                        mask: Int): Int {
        this.libdrm.drmHandleEvent(this.drmFd,
                                   this.drmEventContext)
        return 0
    }

    fun pageFlipHandler(fd: Int,
                        @Unsigned sequence: Int,
                        @Unsigned tv_sec: Int,
                        @Unsigned tv_usec: Int,
                        @Ptr user_data: Long) {
        val drmPageFlipCallbackPointer = Pointer.wrap<Any>(Any::class.java,
                                                           user_data)
        val drmPageFlipCallback = drmPageFlipCallbackPointer.get() as DrmPageFlipCallback
        drmPageFlipCallback.onPageFlip(sequence,
                                       tv_sec,
                                       tv_usec)
        drmPageFlipCallbackPointer.close()
    }

    fun vblankHandler(fd: Int,
                      @Unsigned sequence: Int,
                      @Unsigned tv_sec: Int,
                      @Unsigned tv_usec: Int,
                      @Ptr user_data: Long) {
        val drmPageFlipCallbackPointer = Pointer.wrap<Any>(Any::class.java,
                                                           user_data)
        val drmPageFlipCallback = drmPageFlipCallbackPointer.get() as DrmPageFlipCallback
        drmPageFlipCallback.onVBlank(sequence,
                                     tv_sec,
                                     tv_usec)
        drmPageFlipCallbackPointer.close()
    }
}
