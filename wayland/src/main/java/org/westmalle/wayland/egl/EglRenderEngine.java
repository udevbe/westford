package org.westmalle.wayland.egl;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.output.RenderEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

public class EglRenderEngine implements RenderEngine {

    @Nonnull
    private final ExecutorService renderThread;

    EglRenderEngine(@Nonnull final ExecutorService renderThread) {
        this.renderThread = renderThread;
    }

    @Override
    public void begin(@Nonnull final Object outputImplementation) {
        HasEglOutput hasEglOutput = (HasEglOutput) outputImplementation;
        this.renderThread.submit(()-> hasEglOutput.getEglOutput().begin());
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

    @Nonnull
    @Override
    public Future<?> end(@Nonnull final Object outputImplementation) {
        HasEglOutput hasEglOutput = (HasEglOutput) outputImplementation;
        return this.renderThread.submit(()-> hasEglOutput.getEglOutput().end());
    }
}
