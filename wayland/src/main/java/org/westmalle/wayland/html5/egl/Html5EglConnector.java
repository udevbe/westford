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
package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5Connector;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_RGBA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;

@AutoFactory(allowSubclasses = true,
             className = "Html5EglConnectorFactory")
public class Html5EglConnector implements EglConnector {

    @Nonnull
    private final Display        display;
    @Nonnull
    private final Renderer       renderer;
    @Nonnull
    private final LibGLESv2      libGLESv2;
    private final Html5Connector html5Connector;
    private final EglConnector   eglConnector;

    private       boolean               renderScheduled = false;
    private final EventLoop.IdleHandler doRender        = this::doRender;

    Html5EglConnector(@Nonnull @Provided final Display display,
                      @Nonnull @Provided final Renderer renderer,
                      @Provided @Nonnull final LibGLESv2 libGLESv2,
                      @Nonnull final Html5Connector html5Connector,
                      @Nonnull final EglConnector eglConnector) {
        this.display = display;
        this.renderer = renderer;
        this.libGLESv2 = libGLESv2;
        this.html5Connector = html5Connector;
        this.eglConnector = eglConnector;
    }

    @Override
    public long getEglSurface() {
        return this.eglConnector.getEglSurface();
    }

    @Override
    public long getEglContext() {
        return this.eglConnector.getEglContext();
    }

    @Override
    public long getEglDisplay() {
        return this.eglConnector.getEglDisplay();
    }

    @Override
    public void renderBegin() {
        this.eglConnector.renderBegin();
    }

    @Override
    public void renderEndBeforeSwap() {
        this.eglConnector.renderEndBeforeSwap();

        //TODO we can optimize by checking if all html5 clients are busy and don't do any work until at least one client is available again.
        //for this to work, the html5connector needs to trigger a publishFrame as soon as it sees one of it's clients is available again.

        //TODO we should not publish a new frame if the html5 connector is still busy processing the previous frame. the html5 connector
        //should therefore signal us when it is finished.

        publishFrame();
    }

    private void publishFrame() {

        final OutputMode mode = getWlOutput().getOutput()
                                             .getMode();
        final int width  = mode.getWidth();
        final int height = mode.getHeight();

        final Pointer<Byte> frameBuffer = malloc(width * height * Size.sizeof((Integer) null),
                                                 Byte.class);
        this.libGLESv2.glReadPixels(0,
                                    0,
                                    width,
                                    height,
                                    GL_RGBA,
                                    GL_UNSIGNED_BYTE,
                                    frameBuffer.address);

        this.html5Connector.commitFrame(frameBuffer,
                                        true,
                                        width,
                                        height);
    }

    @Override
    public void renderEndAfterSwap() {
        this.eglConnector.renderEndAfterSwap();
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.eglConnector.getWlOutput();
    }

    @Override
    public void render() {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        whenIdleDoRender();
    }

    private void whenIdleDoRender() {
        if (!this.renderScheduled) {
            this.renderScheduled = true;
            this.display.getEventLoop()
                        .addIdle(this.doRender);
        }
    }

    private void doRender() {
        this.renderer.visit(this);
        this.display.flushClients();
        this.renderScheduled = false;
    }
}
