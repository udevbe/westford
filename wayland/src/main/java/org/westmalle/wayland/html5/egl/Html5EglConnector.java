package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
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
    private final LibGLESv2      libGLESv2;
    private final Html5Connector html5Connector;
    private final EglConnector   eglConnector;

    Html5EglConnector(@Provided @Nonnull final LibGLESv2 libGLESv2,
                      @Nonnull final Html5Connector html5Connector,
                      @Nonnull final EglConnector eglConnector) {
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

        final OutputMode mode = getWlOutput().getOutput()
                                             .getMode();
        final int width  = mode.getWidth();
        final int height = mode.getHeight();

        try (final Pointer<Byte> frameBuffer = malloc(width * height * Size.sizeof((Integer) null),
                                                      Byte.class)) {
            this.libGLESv2.glReadPixels(0,
                                        0,
                                        width,
                                        height,
                                        GL_RGBA,
                                        GL_UNSIGNED_BYTE,
                                        frameBuffer.address);

            this.html5Connector.commitFrame(frameBuffer,
                                            width,
                                            height);
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
        renderer.visit(this);
    }
}
