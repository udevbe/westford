package org.westford.compositor.gles2;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westford.compositor.core.EglOutput;
import org.westford.compositor.core.SurfaceView;
import org.westford.compositor.protocol.WlOutput;

import javax.annotation.Nonnull;

/**
 * Convenience class to lazily setup, draw and flush using a {@link Gles2Renderer}. A Painter instance can only be used once after it has been closed.
 */
@AutoFactory(allowSubclasses = true,
             className = "Gles2PainterFactory")
public class Gles2Painter implements AutoCloseable {


    @Nonnull
    private final Gles2Renderer gles2Renderer;
    @Nonnull
    private final EglOutput     eglOutput;
    @Nonnull
    private final WlOutput      wlOutput;

    private boolean painted = false;

    Gles2Painter(@Provided @Nonnull final Gles2Renderer gles2Renderer,
                 @Nonnull final EglOutput eglOutput,
                 @Nonnull final WlOutput wlOutput) {
        this.gles2Renderer = gles2Renderer;
        this.eglOutput = eglOutput;
        this.wlOutput = wlOutput;
    }

    /**
     * Paint a surface using a {@link Gles2Renderer}, preparing the one time draw setup if needed.
     *
     * @param surfaceView the view to paint.
     *
     * @return true if the view was painted, false if not due to eg. an absent buffer.
     */
    public boolean paint(@Nonnull final SurfaceView surfaceView) {

        //if surface not visible, don't even bother.
        if (!surfaceView.isEnabled() || !surfaceView.isDrawable()) { return false; }

        if (!this.painted) {
            this.gles2Renderer.prepareDraw(this.eglOutput,
                                           this.wlOutput);
            this.painted = true;
        }

        this.gles2Renderer.drawView(surfaceView);

        return true;
    }

    @Override
    public void close() {
        if (this.painted) {
            this.gles2Renderer.finishDraw(this.eglOutput);
        }
    }

    public boolean hasPainted() {
        return this.painted;
    }
}
