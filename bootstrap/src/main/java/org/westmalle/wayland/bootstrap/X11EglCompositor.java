package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.egl.EglModule;
import org.westmalle.wayland.x11.X11Subcomponent;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      /*
                       * CoreModule requires a module which provides a RenderEngine.
                       * EglModule provides an Egl GlESv2 RenderEngine.
                       */
                      EglModule.class})
public interface X11EglCompositor {

    LifeCycle lifeCycle();

    /*
     * Define a platform to output pixels & handle user input.
     */
    X11Subcomponent x11();
}
