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
package org.westmalle.wayland.nativ;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.glibc.Libpthread_Symbols;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libEGL.LibEGL_Symbols;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2_Symbols;
import org.westmalle.wayland.nativ.libX11.LibX11;
import org.westmalle.wayland.nativ.libX11.LibX11_Symbols;
import org.westmalle.wayland.nativ.libX11xcb.LibX11xcb;
import org.westmalle.wayland.nativ.libX11xcb.LibX11xcb_Symbols;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host_Symbols;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libc_Symbols;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libdrm.Libdrm_Symbols;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.nativ.libgbm.Libgbm_Symbols;
import org.westmalle.wayland.nativ.libinput.Libinput;
import org.westmalle.wayland.nativ.libinput.Libinput_Symbols;
import org.westmalle.wayland.nativ.libpixman1.Libpixman1;
import org.westmalle.wayland.nativ.libpixman1.Libpixman1_Symbols;
import org.westmalle.wayland.nativ.libpng.Libpng;
import org.westmalle.wayland.nativ.libpng.Libpng_Symbols;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.nativ.libudev.Libudev_Symbols;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.Libxcb_Symbols;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon_Symbols;
import org.westmalle.wayland.nativ.libxkbcommonx11.Libxkbcommonx11;
import org.westmalle.wayland.nativ.libxkbcommonx11.Libxkbcommonx11_Symbols;

import javax.inject.Singleton;

@Module
public class NativeModule {

    @Singleton
    @Provides
    Libpthread provideLibpthread() {
        new Libpthread_Symbols().link();
        return new Libpthread();
    }

    @Singleton
    @Provides
    Libpng provideLibpng() {
        new Libpng_Symbols().link();
        return new Libpng();
    }

    @Singleton
    @Provides
    Libinput provideLibinput() {
        new Libinput_Symbols().link();
        return new Libinput();
    }

    @Singleton
    @Provides
    Libudev provideLibudev() {
        new Libudev_Symbols().link();
        return new Libudev();
    }

    @Singleton
    @Provides
    Libdrm provideLibdrm() {
        new Libdrm_Symbols().link();
        return new Libdrm();
    }

    @Singleton
    @Provides
    Libgbm provideLibgbm() {
        new Libgbm_Symbols().link();
        return new Libgbm();
    }

    @Singleton
    @Provides
    Libbcm_host provideLibbcm_host() {
        new Libbcm_host_Symbols().link();
        final Libbcm_host libbcm_host = new Libbcm_host();
        libbcm_host.bcm_host_init();
        return libbcm_host;
    }

    @Singleton
    @Provides
    Libpixman1 provideLibpixman1() {
        new Libpixman1_Symbols().link();
        return new Libpixman1();
    }

    @Singleton
    @Provides
    Libc provideLibc() {
        new Libc_Symbols().link();
        return new Libc();
    }

    @Singleton
    @Provides
    LibEGL provideLibegl() {
        new LibEGL_Symbols().link();
        return new LibEGL();
    }

    @Singleton
    @Provides
    LibGLESv2 provideLibgles2() {
        new LibGLESv2_Symbols().link();
        return new LibGLESv2();
    }

    @Singleton
    @Provides
    LibX11 provideLibX11() {
        new LibX11_Symbols().link();
        return new LibX11();
    }

    @Singleton
    @Provides
    Libxcb provideLibxcb() {
        new Libxcb_Symbols().link();
        return new Libxcb();
    }

    @Singleton
    @Provides
    LibX11xcb provideLibX11xcb() {
        new LibX11xcb_Symbols().link();
        return new LibX11xcb();
    }

    @Singleton
    @Provides
    Libxkbcommon provideLibxkbcommon() {
        new Libxkbcommon_Symbols().link();
        return new Libxkbcommon();
    }

    @Singleton
    @Provides
    Libxkbcommonx11 provideLibxkbcommonx11() {
        new Libxkbcommonx11_Symbols().link();
        return new Libxkbcommonx11();
    }

    @Singleton
    @Provides
    NativeFileFactory provideNativeFileFactory(final Libc libc) {
        return new NativeFileFactory(libc);
    }
}
