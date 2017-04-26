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
package org.westford.nativ

import dagger.Module
import dagger.Provides
import org.westford.nativ.glibc.Libc
import org.westford.nativ.glibc.Libc_Symbols
import org.westford.nativ.glibc.Libpthread
import org.westford.nativ.glibc.Libpthread_Symbols
import org.westford.nativ.libEGL.LibEGL
import org.westford.nativ.libEGL.LibEGL_Symbols
import org.westford.nativ.libGLESv2.LibGLESv2
import org.westford.nativ.libGLESv2.LibGLESv2_Symbols
import org.westford.nativ.libX11.LibX11
import org.westford.nativ.libX11.LibX11_Symbols
import org.westford.nativ.libX11xcb.LibX11xcb
import org.westford.nativ.libX11xcb.LibX11xcb_Symbols
import org.westford.nativ.libbcm_host.Libbcm_host
import org.westford.nativ.libbcm_host.Libbcm_host_Symbols
import org.westford.nativ.libdrm.Libdrm
import org.westford.nativ.libdrm.Libdrm_Symbols
import org.westford.nativ.libgbm.Libgbm
import org.westford.nativ.libgbm.Libgbm_Symbols
import org.westford.nativ.libinput.Libinput
import org.westford.nativ.libinput.Libinput_Symbols
import org.westford.nativ.libpixman1.Libpixman1
import org.westford.nativ.libpixman1.Libpixman1_Symbols
import org.westford.nativ.libpng.Libpng
import org.westford.nativ.libpng.Libpng_Symbols
import org.westford.nativ.libudev.Libudev
import org.westford.nativ.libudev.Libudev_Symbols
import org.westford.nativ.libxcb.Libxcb
import org.westford.nativ.libxcb.Libxcb_Symbols
import org.westford.nativ.libxkbcommon.Libxkbcommon
import org.westford.nativ.libxkbcommon.Libxkbcommon_Symbols
import org.westford.nativ.libxkbcommonx11.Libxkbcommonx11
import org.westford.nativ.libxkbcommonx11.Libxkbcommonx11_Symbols

import javax.inject.Singleton

@Module
class NativeModule {

    @Singleton
    @Provides
    internal fun provideLibpthread(): Libpthread {
        Libpthread_Symbols().link()
        return Libpthread()
    }

    @Singleton
    @Provides
    internal fun provideLibpng(): Libpng {
        Libpng_Symbols().link()
        return Libpng()
    }

    @Singleton
    @Provides
    internal fun provideLibinput(): Libinput {
        Libinput_Symbols().link()
        return Libinput()
    }

    @Singleton
    @Provides
    internal fun provideLibudev(): Libudev {
        Libudev_Symbols().link()
        return Libudev()
    }

    @Singleton
    @Provides
    internal fun provideLibdrm(): Libdrm {
        Libdrm_Symbols().link()
        return Libdrm()
    }

    @Singleton
    @Provides
    internal fun provideLibgbm(): Libgbm {
        Libgbm_Symbols().link()
        return Libgbm()
    }

    @Singleton
    @Provides
    internal fun provideLibbcm_host(): Libbcm_host {
        Libbcm_host_Symbols().link()
        val libbcm_host = Libbcm_host()
        libbcm_host.bcm_host_init()
        return libbcm_host
    }

    @Singleton
    @Provides
    internal fun provideLibpixman1(): Libpixman1 {
        Libpixman1_Symbols().link()
        return Libpixman1()
    }

    @Singleton
    @Provides
    internal fun provideLibc(): Libc {
        Libc_Symbols().link()
        return Libc()
    }

    @Singleton
    @Provides
    internal fun provideLibegl(): LibEGL {
        LibEGL_Symbols().link()
        return LibEGL()
    }

    @Singleton
    @Provides
    internal fun provideLibgles2(): LibGLESv2 {
        LibGLESv2_Symbols().link()
        return LibGLESv2()
    }

    @Singleton
    @Provides
    internal fun provideLibX11(): LibX11 {
        LibX11_Symbols().link()
        return LibX11()
    }

    @Singleton
    @Provides
    internal fun provideLibxcb(): Libxcb {
        Libxcb_Symbols().link()
        return Libxcb()
    }

    @Singleton
    @Provides
    internal fun provideLibX11xcb(): LibX11xcb {
        LibX11xcb_Symbols().link()
        return LibX11xcb()
    }

    @Singleton
    @Provides
    internal fun provideLibxkbcommon(): Libxkbcommon {
        Libxkbcommon_Symbols().link()
        return Libxkbcommon()
    }

    @Singleton
    @Provides
    internal fun provideLibxkbcommonx11(): Libxkbcommonx11 {
        Libxkbcommonx11_Symbols().link()
        return Libxkbcommonx11()
    }

    @Singleton
    @Provides
    internal fun provideNativeFileFactory(libc: Libc): NativeFileFactory {
        return NativeFileFactory(libc)
    }
}
