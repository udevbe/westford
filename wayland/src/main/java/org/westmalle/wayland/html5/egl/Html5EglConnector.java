package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "Html5EglConnectorFactory")
public class Html5EglConnector implements EglConnector {

    private final Html5Connector html5Connector;
    private final EglConnector   eglConnector;

    Html5EglConnector(final Html5Connector html5Connector,
                      final EglConnector eglConnector) {
        this.html5Connector = html5Connector;
        this.eglConnector = eglConnector;
    }

    @Override
    public long getEglSurface() {
        return this.eglConnector.getEglSurface();
    }

    @Override
    public long getEglContext() {
        return this.eglConnector.getEglContext();
    }

    @Override
    public long getEglDisplay() {
        return this.eglConnector.getEglDisplay();
    }

    @Override
    public void begin() {
        this.eglConnector.begin();
    }

    @Override
    public void end() {
        this.eglConnector.end();

        //TODO read pixels from screen

        this.html5Connector.getHtml5Sockets()
                           .forEach(html5Socket ->
                                            html5Socket.getSession()
                                                       .ifPresent(session -> {
                                                           //TODO send frame blob
                                                       }));
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.eglConnector.getWlOutput();
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this.eglConnector);
    }
}
