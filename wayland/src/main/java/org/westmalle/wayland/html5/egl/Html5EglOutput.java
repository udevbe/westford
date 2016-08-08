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
package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.core.EglOutput;
import org.westmalle.wayland.core.EglOutputState;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5RenderOutput;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_RGBA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;

@AutoFactory(allowSubclasses = true,
             className = "Html5EglRenderOutputFactory")
public class Html5EglOutput implements EglOutput {

    @Nonnull
    private final Display           display;
    @Nonnull
    private final Renderer          renderer;
    @Nonnull
    private final LibGLESv2         libGLESv2;
    private final Html5RenderOutput html5RenderOutput;
    private final EglOutput         eglOutput;

    private       boolean               renderScheduled = false;
    private final EventLoop.IdleHandler doRender        = this::doRender;

    Html5EglOutput(@Nonnull @Provided final Display display,
                   @Nonnull @Provided final Renderer renderer,
                   @Provided @Nonnull final LibGLESv2 libGLESv2,
                   @Nonnull final Html5RenderOutput html5RenderOutput,
                   @Nonnull final EglOutput eglOutput) {
        this.display = display;
        this.renderer = renderer;
        this.libGLESv2 = libGLESv2;
        this.html5RenderOutput = html5RenderOutput;
        this.eglOutput = eglOutput;
    }

    @Override
    public long getEglSurface() {
        return this.eglOutput.getEglSurface();
    }

    @Override
    public long getEglContext() {
        return this.eglOutput.getEglContext();
    }

    @Override
    public long getEglDisplay() {
        return this.eglOutput.getEglDisplay();
    }

    @Nonnull
    @Override
    public Optional<EglOutputState> getState() {
        return this.eglOutput.getState();
    }

    @Override
    public void updateState(@Nonnull final EglOutputState eglOutputState) {
        this.eglOutput.updateState(eglOutputState);
    }

    @Override
    public void renderBegin() {
        this.eglOutput.renderBegin();
    }

    @Override
    public void renderEndBeforeSwap() {
        this.eglOutput.renderEndBeforeSwap();

        //TODO we can optimize by checking if all html5 clients are busy and don't do any work until at least one client is available again.
        //for this to work, the html5 render output needs to trigger a publishFrame as soon as it sees one of it's clients is available again.

        //TODO we should not publish a new frame if the html5 render output is still busy processing the previous frame. the html5 render output
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

        this.html5RenderOutput.commitFrame(frameBuffer,
                                           true,
                                           width,
                                           height);
    }

    @Override
    public void renderEndAfterSwap() {
        this.eglOutput.renderEndAfterSwap();
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.eglOutput.getWlOutput();
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
