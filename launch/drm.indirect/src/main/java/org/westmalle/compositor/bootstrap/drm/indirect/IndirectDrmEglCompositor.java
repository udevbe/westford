package org.westmalle.compositor.bootstrap.drm.indirect;


import dagger.Component;
import org.westmalle.compositor.core.CoreModule;
import org.westmalle.compositor.core.KeyBindingFactory;
import org.westmalle.compositor.core.LifeCycle;
import org.westmalle.compositor.drm.egl.DrmEglPlatformModule;
import org.westmalle.compositor.gles2.Gles2RendererModule;
import org.westmalle.compositor.input.LibinputSeatFactory;
import org.westmalle.launch.indirect.IndirectModule;
import org.westmalle.tty.Tty;
import org.westmalle.tty.TtyModule;

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
