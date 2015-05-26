package org.westmalle.wayland.output;

import com.google.common.util.concurrent.Service;
import dagger.Component;
import org.westmalle.wayland.jogl.JoglComponent;
import org.westmalle.wayland.protocol.WlCompositorFactory;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlShellFactory;

import javax.inject.Singleton;
import java.util.Set;

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

    //running
    Set<Service> services();

    //render implementations
    JoglComponent newJoglComponent();
}
