package org.westmalle.wayland.bootstrap;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.html5.egl.Html5EglPlatformModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      Html5EglPlatformModule.class})
public interface Html5EglCompositor {

    //TODO
    //LifeCycle lifeCycle();
}
