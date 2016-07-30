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
package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.Point;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "X11ConnectorFactory")
public class X11Connector implements Connector {

    @Nonnull
    private final Renderer renderer;
    private final int      xWindow;
    @Nonnull
    private final WlOutput wlOutput;

    X11Connector(@Nonnull @Provided final Renderer renderer,
                 final int xWindow,
                 @Nonnull final WlOutput wlOutput) {
        this.renderer = renderer;
        this.xWindow = xWindow;
        this.wlOutput = wlOutput;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.wlOutput;
    }

    public int getXWindow() {
        return this.xWindow;
    }

    public Point toGlobal(final int x11WindowX,
                          final int x11WindowY) {
        final OutputGeometry geometry = getWlOutput().getOutput()
                                                     .getGeometry();
        final int globalX = geometry.getX() + x11WindowX;
        final int globalY = geometry.getY() + x11WindowY;

        return Point.create(globalX,
                            globalY);
    }

    @Override
    public void render() {
        this.renderer.visit(this);
    }
}
