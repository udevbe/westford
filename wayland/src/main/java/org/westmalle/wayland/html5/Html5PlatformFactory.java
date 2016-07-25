//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.html5;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Platform;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//TODO tests tests tests!
public class Html5PlatformFactory {

    private final Html5SocketServletFactory   html5SocketServletFactory;
    private final Html5ConnectorFactory       html5ConnectorFactory;
    private final PrivateHtml5PlatformFactory privateHtml5PlatformFactory;

    @Inject
    Html5PlatformFactory(final Html5SocketServletFactory html5SocketServletFactory,
                         final Html5ConnectorFactory html5ConnectorFactory,
                         final PrivateHtml5PlatformFactory privateHtml5PlatformFactory) {
        this.html5SocketServletFactory = html5SocketServletFactory;
        this.html5ConnectorFactory = html5ConnectorFactory;
        this.privateHtml5PlatformFactory = privateHtml5PlatformFactory;
    }

    public Html5Platform create(final Platform platform) {
        //TODO from configuration

        final Server server = new Server(8080);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/wayland");
        server.setHandler(context);

        final List<Optional<Html5Connector>> html5Connectors = new ArrayList<>();
        for (final Optional<? extends Connector> connectorOptional : platform.getConnectors()) {
            html5Connectors.add(connectorOptional.flatMap(connector -> createHtml5Connector(context,
                                                                                            connector)));
        }

        // Add default servlet (to serve the html/css/js)
        // Figure out where the static files are stored.
        final URL urlStatics = getClass().getResource("/html5/index.html");
        Objects.requireNonNull(urlStatics,
                               "Unable to find index.html in classpath");
        final String urlBase = urlStatics.toExternalForm()
                                         .replaceFirst("/[^/]*$",
                                                       "/");
        final ServletHolder defHolder = new ServletHolder("default",
                                                          new DefaultServlet());
        defHolder.setInitParameter("resourceBase",
                                   urlBase);
        context.addServlet(defHolder,
                           "/");

        try {
            server.start();
        }
        catch (final Exception e) {
            e.printStackTrace();
        }

        return this.privateHtml5PlatformFactory.create(server,
                                                       html5Connectors);
    }

    private Optional<Html5Connector> createHtml5Connector(final ServletContextHandler context,
                                                          final Connector connector) {

        final Html5Connector html5Connector = this.html5ConnectorFactory.create(connector);

        // Add websocket servlet
        final String connectorId = connector.getWlOutput()
                                            .getOutput()
                                            .getName();
        context.addServlet(new ServletHolder(connectorId,
                                             this.html5SocketServletFactory.create(html5Connector)),
                           "/" + connectorId);
        return Optional.of(html5Connector);
    }
}
