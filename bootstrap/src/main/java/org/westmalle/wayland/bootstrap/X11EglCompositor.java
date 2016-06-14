package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.x11.X11EglPlatformModule;
import org.westmalle.wayland.x11.X11SeatFactory;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      X11EglPlatformModule.class})
public interface X11EglCompositor {

    LifeCycle lifeCycle();

    /*
     * Define a platform to output pixels & handle user input.
     */
    X11SeatFactory seatFactory();
}
