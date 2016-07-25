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
}
