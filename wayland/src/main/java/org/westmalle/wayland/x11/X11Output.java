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

import com.sun.jna.Pointer;
import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.egl.HasEglOutput;

import javax.annotation.Nonnull;

public class X11Output implements HasEglOutput {

    @Nonnull
    private final X11EglOutputFactory x11EglOutputFactory;
    @Nonnull
    private final X11EventBus         x11EventBus;
    @Nonnull
    private final Pointer             xcbConnection;
    private final int                 xWindow;
    @Nonnull
    private final Pointer             xDisplay;

    private X11EglOutput eglOutput;

    X11Output(@Nonnull final X11EglOutputFactory x11EglOutputFactory,
              @Nonnull final X11EventBus x11EventBus,
              @Nonnull final Pointer xcbConnection,
              @Nonnull final Pointer xDisplay,
              final int xWindow) {
        this.x11EglOutputFactory = x11EglOutputFactory;
        this.x11EventBus = x11EventBus;
        this.xcbConnection = xcbConnection;
        this.xWindow = xWindow;
        this.xDisplay = xDisplay;
    }

    @Override
    public EglOutput getEglOutput() {
        if (this.eglOutput == null) {
            this.eglOutput = this.x11EglOutputFactory.create(this.xDisplay,
                                                             this.xWindow);
        }
        return this.eglOutput;
    }

    public int getxWindow() {
        return this.xWindow;
    }

    @Nonnull
    public Pointer getxDisplay() {
        return this.xDisplay;
    }

    @Nonnull
    public Pointer getXcbConnection() {
        return this.xcbConnection;
    }

    @Nonnull
    public X11EventBus getX11EventBus() {
        return this.x11EventBus;
    }
}
