/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.compositor.html5;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.westmalle.compositor.core.RenderOutput;
import org.westmalle.compositor.core.RenderPlatform;
import org.westmalle.compositor.html5.Html5RenderOutputFactory;
import org.westmalle.compositor.html5.Html5SocketServletFactory;
import org.westmalle.compositor.html5.PrivateHtml5PlatformFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO tests tests tests!
public class Html5PlatformFactory {

    private final Html5SocketServletFactory   html5SocketServletFactory;
    private final Html5RenderOutputFactory    html5RenderOutputFactory;
    private final PrivateHtml5PlatformFactory privateHtml5PlatformFactory;

    @Inject
    Html5PlatformFactory(final Html5SocketServletFactory html5SocketServletFactory,
                         final Html5RenderOutputFactory html5RenderOutputFactory,
                         final PrivateHtml5PlatformFactory privateHtml5PlatformFactory) {
        this.html5SocketServletFactory = html5SocketServletFactory;
        this.html5RenderOutputFactory = html5RenderOutputFactory;
        this.privateHtml5PlatformFactory = privateHtml5PlatformFactory;
    }

    public Html5RenderPlatform create(final RenderPlatform renderPlatform) {
        //TODO from configuration

        final Server server = new Server(8080);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/wayland");
        server.setHandler(context);

        final List<Html5RenderOutput> html5RenderOutputs = new ArrayList<>();
        renderPlatform.getRenderOutputs()
                      .forEach(renderOutput ->
                                       html5RenderOutputs.add(createHtml5RenderOutput(context,
                                                                                      renderOutput)));

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
                                                       html5RenderOutputs);
    }

    private Html5RenderOutput createHtml5RenderOutput(final ServletContextHandler context,
                                                      final RenderOutput renderOutput) {

        final Html5RenderOutput html5RenderOutput = this.html5RenderOutputFactory.create(renderOutput);

        // Add websocket servlet
        final String renderOutputName = renderOutput.getWlOutput()
                                                    .getOutput()
                                                    .getName();
        context.addServlet(new ServletHolder(renderOutputName,
                                             this.html5SocketServletFactory.create(html5RenderOutput)),
                           "/" + renderOutputName);
        return html5RenderOutput;
    }
}
