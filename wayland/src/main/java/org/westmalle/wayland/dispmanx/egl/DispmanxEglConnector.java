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
package org.westmalle.wayland.dispmanx.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.core.events.RenderBegin;
import org.westmalle.wayland.core.events.RenderEndAfterSwap;
import org.westmalle.wayland.core.events.RenderEndBeforeSwap;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.dispmanx.DispmanxConnector;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(className = "DispmanxEglConnectorFactory",
             allowSubclasses = true)
public class DispmanxEglConnector implements EglConnector {

    private final Signal<RenderBegin, Slot<RenderBegin>>                 renderBeginSignal         = new Signal<>();
    private final Signal<RenderEndBeforeSwap, Slot<RenderEndBeforeSwap>> renderEndBeforeSwapSignal = new Signal<>();
    private final Signal<RenderEndAfterSwap, Slot<RenderEndAfterSwap>>   renderEndAfterSwapSignal  = new Signal<>();

    @Nonnull
    private final Display               display;
    @Nonnull
    private final DispmanxConnector     dispmanxConnector;
    @Nonnull
    private final EGL_DISPMANX_WINDOW_T eglDispmanxWindow;

    private Optional<EventSource> renderJobEvent = Optional.empty();

    private final long eglSurface;
    private final long eglContext;
    private final long eglDisplay;

    DispmanxEglConnector(@Nonnull @Provided final Display display,
                         @Nonnull final DispmanxConnector dispmanxConnector,
                         @Nonnull final EGL_DISPMANX_WINDOW_T eglDispmanxWindow,
                         final long eglSurface,
                         final long eglContext,
                         final long eglDisplay) {
        this.display = display;
        this.dispmanxConnector = dispmanxConnector;
        this.eglDispmanxWindow = eglDispmanxWindow;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
        this.eglDisplay = eglDisplay;
    }

    @Nonnull
    public DispmanxConnector getDispmanxConnector() {
        return this.dispmanxConnector;
    }

    @Override
    public long getEglSurface() {
        return this.eglSurface;
    }

    @Override
    public long getEglContext() {
        return this.eglContext;
    }

    @Override
    public long getEglDisplay() {
        return this.eglDisplay;
    }

    @Nonnull
    public EGL_DISPMANX_WINDOW_T getEglDispmanxWindow() {
        return this.eglDispmanxWindow;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.dispmanxConnector.getWlOutput();
    }

    @Override
    public Signal<RenderBegin, Slot<RenderBegin>> getRenderBeginSignal() {
        return this.renderBeginSignal;
    }

    @Override
    public Signal<RenderEndBeforeSwap, Slot<RenderEndBeforeSwap>> getRenderEndBeforeSwapSignal() {
        return this.renderEndBeforeSwapSignal;
    }

    @Override
    public Signal<RenderEndAfterSwap, Slot<RenderEndAfterSwap>> getRenderEndAfterSwapSignal() {
        return this.renderEndAfterSwapSignal;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        if (!this.renderJobEvent.isPresent()) {
            whenIdle(() -> renderOn(renderer));
        }
    }

    private void whenIdle(final EventLoop.IdleHandler idleHandler) {
        this.renderJobEvent = Optional.of(this.display.getEventLoop()
                                                      .addIdle(idleHandler));
    }

    private void renderOn(@Nonnull final Renderer renderer) {
        this.renderJobEvent.get()
                           .remove();
        this.renderJobEvent = Optional.empty();
        renderer.visit(this);
        this.display.flushClients();
    }
}
