package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.dispmanx.DispmanxEglPlatformModule;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.input.LibinputSeatFactory;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      DispmanxEglPlatformModule.class})
public interface DispmanxEglCompositor {
    LifeCycle lifeCycle();

    LibinputSeatFactory seatFactory();
}
