package org.westmalle.wayland.bootstrap.drm.indirect;


import dagger.Component;
import org.westmalle.launch.indirect.IndirectModule;
import org.westmalle.tty.Tty;
import org.westmalle.tty.TtyModule;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.KeyBindingFactory;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.drm.egl.DrmEglPlatformModule;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.input.LibinputSeatFactory;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      DrmEglPlatformModule.class,
                      TtyModule.class,
                      IndirectModule.class})
public interface IndirectDrmEglCompositor {
    LifeCycle lifeCycle();

    LibinputSeatFactory seatFactory();

    KeyBindingFactory keyBindingFactory();

    Tty tty();
}
