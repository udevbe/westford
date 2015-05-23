package org.westmalle.wayland.platform;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.platform.c.Libc;
import org.westmalle.wayland.platform.pixman1.Libpixman1;

import javax.inject.Singleton;

@Module
public class NativeModule {

    @Singleton
    @Provides
    Libpixman1 provideLibpixman1() {
        return new Libpixman1();
    }

    @Singleton
    @Provides
    Libc provideLibc() {
        return new Libc();
    }
}
