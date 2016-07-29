package org.westmalle.wayland.tty;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class TtyModule {
    @Provides
    @Singleton
    Tty provideTty(final TtyFactory ttyFactory) {
        return ttyFactory.create();
    }
}
