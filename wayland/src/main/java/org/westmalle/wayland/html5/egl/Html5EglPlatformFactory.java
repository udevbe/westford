package org.westmalle.wayland.html5.egl;


import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.html5.Html5Connector;
import org.westmalle.wayland.html5.Html5Platform;
import org.westmalle.wayland.html5.Html5PlatformFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Html5EglPlatformFactory {

    @Nonnull
    private final Html5PlatformFactory           html5PlatformFactory;
    @Nonnull
    private final PrivateHtml5EglPlatformFactory privateHtml5EglPlatformFactory;
    @Nonnull
    private final Html5EglConnectorFactory       html5EglConnectorFactory;

    @Inject
    Html5EglPlatformFactory(@Nonnull final Html5PlatformFactory html5PlatformFactory,
                            @Nonnull final PrivateHtml5EglPlatformFactory privateHtml5EglPlatformFactory,
                            @Nonnull final Html5EglConnectorFactory html5EglConnectorFactory) {
        this.html5PlatformFactory = html5PlatformFactory;
        this.privateHtml5EglPlatformFactory = privateHtml5EglPlatformFactory;
        this.html5EglConnectorFactory = html5EglConnectorFactory;
    }

    public Html5EglPlatform create(@Nonnull final EglPlatform eglPlatform) {

        final Html5Platform html5Platform = this.html5PlatformFactory.create(eglPlatform);

        final List<? extends Optional<? extends EglConnector>> eglConnectors   = eglPlatform.getConnectors();
        final List<Optional<Html5Connector>>                   html5Connectors = html5Platform.getConnectors();

        final Iterator<? extends Optional<? extends EglConnector>> eglConnectorIterator   = eglConnectors.iterator();
        final Iterator<Optional<Html5Connector>>                   html5ConnectorIterator = html5Connectors.iterator();

        final List<Optional<Html5EglConnector>> html5EglConnectors = new LinkedList<>();

        while (eglConnectorIterator.hasNext() &&
               html5ConnectorIterator.hasNext()) {

            final Optional<? extends EglConnector> eglConnector   = eglConnectorIterator.next();
            final Optional<Html5Connector>         html5Connector = html5ConnectorIterator.next();

            final Optional<Html5EglConnector> html5EglConnector;
            if (eglConnector.isPresent() && html5Connector.isPresent()) {
                html5EglConnector = Optional.of(this.html5EglConnectorFactory.create(html5Connector.get(),
                                                                                     eglConnector.get()));
            }
            else {
                html5EglConnector = Optional.empty();
            }

            html5EglConnectors.add(html5EglConnector);
        }

        return this.privateHtml5EglPlatformFactory.create(eglPlatform,
                                                          html5EglConnectors);
    }
}
