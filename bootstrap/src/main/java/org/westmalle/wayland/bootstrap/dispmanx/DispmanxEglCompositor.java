package org.westmalle.wayland.bootstrap.dispmanx;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.dispmanx.egl.DispmanxEglPlatformModule;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.input.LibinputSeatFactory;
import org.westmalle.wayland.tty.TtyModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      DispmanxEglPlatformModule.class,
                      TtyModule.class})
public interface DispmanxEglCompositor {
    LifeCycle lifeCycle();

    LibinputSeatFactory seatFactory();
}
