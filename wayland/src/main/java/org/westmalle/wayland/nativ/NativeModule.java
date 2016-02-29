//Copyright 2015 Erik De Rijcke
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
package org.westmalle.wayland.nativ;

import com.github.zubnix.jaccall.Linker;
import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.nativ.libX11.LibX11;
import org.westmalle.wayland.nativ.libX11xcb.LibX11xcb;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libpixman1.Libpixman1;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.wayland.nativ.libxkbcommonx11.Libxkbcommonx11;

import javax.inject.Singleton;

@Module
public class NativeModule {

    @Singleton
    @Provides
    Libbcm_host provideLibbcm_host() {
        Linker.link(Libbcm_host.class);
        final Libbcm_host libbcm_host = new Libbcm_host();
        libbcm_host.bcm_host_init();
        return libbcm_host;
    }

    @Singleton
    @Provides
    Libpixman1 provideLibpixman1() {
        Linker.link(Libpixman1.class);
        return new Libpixman1();
    }

    @Singleton
    @Provides
    Libc provideLibc() {
        Linker.link(Libc.class);
        return new Libc();
    }

    @Singleton
    @Provides
    LibEGL provideLibegl() {
        Linker.link(LibEGL.class);
        return new LibEGL();
    }

    @Singleton
    @Provides
    LibGLESv2 provideLibgles2() {
        Linker.link(LibGLESv2.class);
        return new LibGLESv2();
    }

    @Singleton
    @Provides
    LibX11 provideLibX11() {
        Linker.link(LibX11.class);
        return new LibX11();
    }

    @Singleton
    @Provides
    Libxcb provideLibxcb() {
        Linker.link(Libxcb.class);
        return new Libxcb();
    }

    @Singleton
    @Provides
    LibX11xcb provideLibX11xcb() {
        Linker.link(LibX11xcb.class);
        return new LibX11xcb();
    }

    @Singleton
    @Provides
    Libxkbcommon provideLibxkbcommon() {
        Linker.link(Libxkbcommon.class);
        return new Libxkbcommon();
    }

    @Singleton
    @Provides
    Libxkbcommonx11 provideLibxkbcommonx11() {
        Linker.link(Libxkbcommonx11.class);
        return new Libxkbcommonx11();
    }

    @Singleton
    @Provides
    NativeFileFactory provideNativeFileFactory(final Libc libc) {
        return new NativeFileFactory(libc);
    }
}
