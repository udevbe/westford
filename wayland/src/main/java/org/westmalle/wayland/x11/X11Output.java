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
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.egl.HasEglOutput;

import javax.annotation.Nonnull;
import java.util.Map;

@AutoFactory(className = "PrivateX11OutputFactory",
             allowSubclasses = true)
public class X11Output implements HasEglOutput {

    @Nonnull
    private final X11EglOutputFactory  x11EglOutputFactory;
    @Nonnull
    private final X11EventBus          x11EventBus;
    private final long                 xcbConnection;
    private final int                  xWindow;
    private final long                 xDisplay;
    @Nonnull
    private final Map<String, Integer> atoms;

    private X11EglOutput eglOutput;

    X11Output(@Provided @Nonnull final X11EglOutputFactory x11EglOutputFactory,
              @Nonnull final X11EventBus x11EventBus,
              final long xcbConnection,
              final long xDisplay,
              final int xWindow,
              @Nonnull final Map<String, Integer> x11Atoms) {
        this.x11EglOutputFactory = x11EglOutputFactory;
        this.x11EventBus = x11EventBus;
        this.xcbConnection = xcbConnection;
        this.xWindow = xWindow;
        this.xDisplay = xDisplay;
        this.atoms = x11Atoms;
    }

    @Override
    public RenderOutput getEglOutput() {
        if (this.eglOutput == null) {
            this.eglOutput = this.x11EglOutputFactory.create(this.xDisplay,
                                                             this.xWindow);
        }
        return this.eglOutput;
    }

    public int getxWindow() {
        return this.xWindow;
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
}
