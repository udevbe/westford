package org.westmalle.wayland.core;

import dagger.Component;
import org.westmalle.wayland.egl.EglComponent;
import org.westmalle.wayland.protocol.WlCompositorFactory;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlShellFactory;
import org.westmalle.wayland.protocol.XdgShellFactory;
import org.westmalle.wayland.x11.X11Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = OutputModule.class)
public interface OutputComponent {
    //generic compositor output
    RendererFactory shmRendererFactory();

    CompositorFactory compositorFactory();

    //protocol
    WlCompositorFactory wlCompositorFactory();

    WlSeatFactory wlSeatFactory();

    WlShellFactory wlShellFactory();

    XdgShellFactory xdgShellFactory();

    //running
    ShellService shellService();

    //render implementations
    X11Component x11Component();

    EglComponent eglComponent();
}
