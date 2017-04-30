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
package org.westford.compositor.core

import dagger.Module
import dagger.Provides
import org.freedesktop.jaccall.Pointer
import org.freedesktop.wayland.server.Display
import org.westford.nativ.NativeModule
import org.westford.nativ.glibc.Libc

import javax.inject.Singleton

@Module(includes = arrayOf(NativeModule::class)) class CoreModule {

    @Provides internal fun provideFiniteRegion(finiteRegionFactory: FiniteRegionFactory): FiniteRegion {
        return finiteRegionFactory.create()
    }

    @Provides @Singleton internal fun provideInfiniteRegion(finiteRegionFactory: FiniteRegionFactory): InfiniteRegion {
        return InfiniteRegion(finiteRegionFactory)
    }

    @Provides internal fun providePointerDevice(pointerDeviceFactory: PointerDeviceFactory): PointerDevice {
        return pointerDeviceFactory.create()
    }

    @Provides @Singleton internal fun provideNullRegion(): NullRegion {
        return NullRegion()
    }

    @Provides @Singleton internal fun provideDisplay(): Display {
        return Display.create()
    }

    @Singleton @Provides internal fun provideJobExecutor(display: Display,
                                                         libc: Libc): JobExecutor {
        val pipeFds = Pointer.nref(0,
                                   0)
        libc.pipe(pipeFds.address)

        val readFd = pipeFds.dref(0)
        val writeFd = pipeFds.dref(1)

        val readFlags = libc.fcntl(readFd,
                                   Libc.F_GETFD,
                                   0)
        libc.fcntl(readFd,
                   Libc.F_SETFD,
                   readFlags or Libc.FD_CLOEXEC)

        val writeFlags = libc.fcntl(writeFd,
                                    Libc.F_GETFD,
                                    0)
        libc.fcntl(writeFd,
                   Libc.F_SETFD,
                   writeFlags or Libc.FD_CLOEXEC)

        return JobExecutor(display,
                           readFd,
                           writeFd,
                           libc)
    }
}
