package org.westmalle.wayland.bootstrap.drm.launcher;


import dagger.Component;
import org.westmalle.wayland.nativ.NativeModule;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.tty.Tty;
import org.westmalle.wayland.tty.TtyModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {NativeModule.class,
                      TtyModule.class})
public interface DrmLauncher {
    Libc libc();

    Libpthread libpthread();

    Tty tty();

}
