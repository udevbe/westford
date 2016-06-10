package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.dispmanx.Dispmanx;
import org.westmalle.wayland.egl.EglModule;
import org.westmalle.wayland.input.LibinputSeatFactory;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      /*
                       * CoreModule requires a module which provides a RenderEngine.
                       * EglModule provides an Egl GlESv2 Renderer.
                       */
                      EglModule.class})
public interface DispmanxEglCompositor {
    LifeCycle lifeCycle();

    /*
     * Define a platform to output pixels
     */
    Dispmanx dispmanx();

    /*
     * Sorry no input yet. Need a Libinput seat implementation for that.
     */
    LibinputSeatFactory seatFactory();
}
