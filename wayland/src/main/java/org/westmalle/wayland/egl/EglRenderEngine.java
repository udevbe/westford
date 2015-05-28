package org.westmalle.wayland.egl;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.output.RenderEngine;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class EglRenderEngine implements RenderEngine {

    @Nonnull
    private final ExecutorService renderThread;

    EglRenderEngine(@Nonnull final ExecutorService renderThread) {
        this.renderThread = renderThread;
    }

    @Override
    public void draw(@Nonnull final WlSurfaceResource surfaceResource,
                     @Nonnull final WlBufferResource buffer) {
        this.renderThread.submit(() -> {
            doDraw(surfaceResource,
                   buffer);
            return null;
        });
    }

    private void doDraw(@Nonnull final WlSurfaceResource surfaceResource,
                        @Nonnull final WlBufferResource buffer) {

    }

    @Override
    public void begin(@Nonnull final Object outputImplementation) {
        EglOutput eglOutput = (EglOutput) outputImplementation;
        this.renderThread.submit(eglOutput::begin);
    }

    @Nonnull
    @Override
    public Future<?> end(@Nonnull final Object outputImplementation) {
        EglOutput eglOutput = (EglOutput) outputImplementation;
        return this.renderThread.submit(eglOutput::end);
    }
}
