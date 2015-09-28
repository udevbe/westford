package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.CoreCompositor;
import org.westmalle.wayland.core.CoreModule;
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
public interface X11EglCompositor extends CoreCompositor {

    /*
     * We want a platform to view what we composite and to handle user input, else we're running in headless mode.
     */
    X11Subcomponent x11();
}
