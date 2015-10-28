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

import com.sun.jna.Native;
import com.sun.jna.Platform;
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
import java.util.logging.Logger;

@Module
public class NativeModule {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public NativeModule() {
        Native.setCallbackExceptionHandler((thread,
                                            throwable) -> {
            LOGGER.severe("Got uncaught exception " + throwable.getMessage());
            throwable.printStackTrace();
        });
    }

    @Singleton
    @Provides
    Libbcm_host provideLibbcm_host() {
        Native.register(Libbcm_host.class,
                        "bcm_host");
        final Libbcm_host libbcm_host = new Libbcm_host();
        libbcm_host.bcm_host_init();
        return libbcm_host;
    }

    @Singleton
    @Provides
    Libpixman1 provideLibpixman1() {
        Native.register(Libpixman1.class,
                        "pixman-1");
        return new Libpixman1();
    }

    @Singleton
    @Provides
    Libc provideLibc() {
        Native.register(Libc.class,
                        Platform.C_LIBRARY_NAME);
        return new Libc();
    }

    @Singleton
    @Provides
    LibEGL provideLibegl() {
        Native.register(LibEGL.class,
                        "EGL");
        return new LibEGL();
    }

    @Singleton
    @Provides
    LibGLESv2 provideLibgles2() {
        Native.register(LibGLESv2.class,
                        "GLESv2");
        return new LibGLESv2();
    }

    @Singleton
    @Provides
    LibX11 provideLibX11() {
        Native.register(LibX11.class,
                        "X11");
        return new LibX11();
    }

    @Singleton
    @Provides
    Libxcb provideLibxcb() {
        Native.register(Libxcb.class,
                        "xcb");
        return new Libxcb();
    }

    @Singleton
    @Provides
    LibX11xcb provideLibX11xcb() {
        Native.register(LibX11xcb.class,
                        "X11-xcb");
        return new LibX11xcb();
    }

    @Singleton
    @Provides
    Libxkbcommon provideLibxkbcommon() {
        Native.register(Libxkbcommon.class,
                        "xkbcommon");
        return new Libxkbcommon();
    }

    @Singleton
    @Provides
    Libxkbcommonx11 provideLibxkbcommonx11() {
        Native.register(Libxkbcommonx11.class,
                        "xkbcommon-x11");
        return new Libxkbcommonx11();
    }

    @Singleton
    @Provides
    NativeFileFactory provideNativeFileFactory(final Libc libc) {
        return new NativeFileFactory(libc);
    }
}
