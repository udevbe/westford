package org.westmalle.wayland.nativ;

import dagger.Module;
import dagger.Provides;

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

    @Singleton
    @Provides
    Libegl provideLibegl(){
        return new Libegl();
    }

    @Singleton
    @Provides
    Libgles2 provideLibgles2(){
        return new Libgles2();
    }
}
