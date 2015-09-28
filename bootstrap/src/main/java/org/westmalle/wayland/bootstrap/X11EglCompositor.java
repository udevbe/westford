package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.CoreCompositor;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.egl.EglModule;
import org.westmalle.wayland.x11.X11Subcomponent;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      EglModule.class})
public interface X11EglCompositor extends CoreCompositor {

    //render implementations
    X11Subcomponent x11();
}
