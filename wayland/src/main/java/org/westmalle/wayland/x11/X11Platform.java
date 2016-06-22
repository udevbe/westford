//Copyright 2015 Erik De Rijcke
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
package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AutoFactory(className = "PrivateX11PlatformFactory",
             allowSubclasses = true)
public class X11Platform implements Platform {

    @Nonnull
    private final X11Connector[]       connectors;
    @Nonnull
    private final X11EventBus          x11EventBus;
    private final long                 xcbConnection;
    private final long                 xDisplay;
    @Nonnull
    private final Map<String, Integer> atoms;

    X11Platform(@Nonnull final X11Connector[] connectors,
                @Nonnull final X11EventBus x11EventBus,
                final long xcbConnection,
                final long xDisplay,
                @Nonnull final Map<String, Integer> x11Atoms) {
        this.connectors = connectors;
        this.x11EventBus = x11EventBus;
        this.xcbConnection = xcbConnection;
        this.xDisplay = xDisplay;
        this.atoms = x11Atoms;
    }

    public long getxDisplay() {
        return this.xDisplay;
    }

    public long getXcbConnection() {
        return this.xcbConnection;
    }

    @Nonnull
    public X11EventBus getX11EventBus() {
        return this.x11EventBus;
    }

    @Nonnull
    public Map<String, Integer> getX11Atoms() {
        return this.atoms;
    }

    @Nonnull
    @Override
    public X11Connector[] getConnectors() {
        return this.connectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }
}
