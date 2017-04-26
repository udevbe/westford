package org.westford.compositor.gles2

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.westford.compositor.core.EglOutput
import org.westford.compositor.core.SurfaceView
import org.westford.compositor.protocol.WlOutput

/**
 * Convenience class to lazily setup, draw and flush using a [Gles2Renderer].
 */
@AutoFactory(allowSubclasses = true,
             className = "Gles2PainterFactory") class Gles2Painter
internal constructor(@param:Provided private val gles2Renderer: Gles2Renderer,
                     private val eglOutput: EglOutput,
                     private val wlOutput: WlOutput) {

    private var painted = false

    /**
     * Paint a surface using a [Gles2Renderer], doing a one time draw setup if needed.

     * @param surfaceView the view to paint.
     * *
     * *
     * @return true if the view was painted, false if not due to eg. an absent buffer.
     */
    fun paint(surfaceView: SurfaceView): Boolean {

        //if surface not visible, don't even bother.
        if (!surfaceView.isEnabled || !surfaceView.isDrawable) {
            return false
        }

        if (!this.painted) {
            this.gles2Renderer.prepareDraw(this.eglOutput,
                                           this.wlOutput)
            this.painted = true
        }

        this.gles2Renderer.drawView(surfaceView)

        return true
    }

    fun commit(): Boolean {
        if (this.painted) {
            this.gles2Renderer.finishDraw(this.eglOutput)
        }

        return this.painted
    }
}
