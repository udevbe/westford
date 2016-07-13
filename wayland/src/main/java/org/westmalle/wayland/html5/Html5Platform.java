package org.westmalle.wayland.html5;

import com.google.auto.factory.AutoFactory;
import org.eclipse.jetty.server.Server;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5PlatformFactory")
public class Html5Platform implements Platform {

    private final Server                         server;
    private final List<Optional<Html5Connector>> connectors;

    @Inject
    Html5Platform(final Server server,
                  final List<Optional<Html5Connector>> connectors) {
        this.server = server;
        this.connectors = connectors;
    }

    public Server getServer() {
        return this.server;
    }

    @Nonnull
    @Override
    public List<Optional<Html5Connector>> getConnectors() {
        return this.connectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }
}
