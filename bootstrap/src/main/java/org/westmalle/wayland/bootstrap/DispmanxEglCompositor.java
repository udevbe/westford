package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.dispmanx.Dispmanx;
import org.westmalle.wayland.egl.EglModule;

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
     * Sorry no input yet. Need a libinput seat implementation for that.
     */
}
