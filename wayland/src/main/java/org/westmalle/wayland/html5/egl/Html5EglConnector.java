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

import java.util.concurrent.atomic.AtomicBoolean;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_RGBA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;

@AutoFactory(allowSubclasses = true,
             className = "Html5EglConnectorFactory")
public class Html5EglConnector implements EglConnector {

    @Nonnull
    private final Display        display;
    @Nonnull
    private final LibGLESv2      libGLESv2;
    private final Html5Connector html5Connector;
    private final EglConnector   eglConnector;

    private final AtomicBoolean rendererAvailable = new AtomicBoolean(true);
    private final AtomicBoolean publishPending    = new AtomicBoolean(false);

    Html5EglConnector(@Nonnull @Provided final Display display,
                      @Provided @Nonnull final LibGLESv2 libGLESv2,
                      @Nonnull final Html5Connector html5Connector,
                      @Nonnull final EglConnector eglConnector) {
        this.display = display;
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

        if (this.html5Connector.getCommitBusy() &&
            this.publishPending.compareAndSet(false,
                                              true)) {
            this.display.getEventLoop()
                        .addIdle(() -> {
                            this.publishPending.set(false);
                            publishFrame();
                        });
        }
        else {
            publishFrame();
        }
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

        final boolean committed = this.html5Connector.commitFrame(frameBuffer,
                                                                  true,
                                                                  width,
                                                                  height);
        if (!committed) {
            frameBuffer.close();
        }
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
    public void accept(@Nonnull final Renderer renderer) {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        whenIdle(() -> renderOn(renderer));
    }

    private void whenIdle(final EventLoop.IdleHandler idleHandler) {
        if (this.rendererAvailable.compareAndSet(true,
                                                 false)) {
            this.display.getEventLoop()
                        .addIdle(idleHandler);
        }
    }

    private void renderOn(@Nonnull final Renderer renderer) {
        renderer.visit(this);
        this.display.flushClients();
        this.rendererAvailable.set(true);
    }
}
