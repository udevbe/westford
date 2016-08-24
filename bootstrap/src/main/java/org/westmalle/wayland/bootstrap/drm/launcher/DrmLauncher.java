package org.westmalle.wayland.bootstrap.drm.launcher;


import dagger.Component;
import org.westmalle.nativ.NativeModule;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.glibc.Libpthread;
import org.westmalle.tty.Tty;
import org.westmalle.tty.TtyModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {NativeModule.class,
                      TtyModule.class})
public interface DrmLauncher {
    Libc libc();

    Libpthread libpthread();

    Tty tty();

}
