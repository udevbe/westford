package org.westmalle.wayland;

import dagger.Component;
import org.westmalle.wayland.core.CompositorFactory;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.RendererFactory;
import org.westmalle.wayland.core.ShellService;
import org.westmalle.wayland.egl.EglComponent;
import org.westmalle.wayland.protocol.WlCompositorFactory;
import org.westmalle.wayland.protocol.WlDataDeviceManagerFactory;
import org.westmalle.wayland.protocol.WlShellFactory;
import org.westmalle.wayland.x11.X11Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = CoreModule.class)
public interface Application {
    //generic compositor output
    RendererFactory shmRendererFactory();

    CompositorFactory compositorFactory();

    //core protocol
    WlCompositorFactory wlCompositorFactory();

    WlDataDeviceManagerFactory wlDataDeviceManagerFactory();

    WlShellFactory wlShellFactory();

    //running
    ShellService shellService();

    //render implementations
    X11Component x11();

    EglComponent egl();
}