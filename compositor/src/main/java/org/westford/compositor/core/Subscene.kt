package org.westford.compositor.core

import com.google.auto.value.AutoValue
import java.util.Optional

/**
 * A subsection of a [Scene]. Has no relation with [Subsurface].
 */
@AutoValue abstract class Subscene {

    abstract val backgroundView: SurfaceView?

    abstract val underViews: List<SurfaceView>

    abstract val applicationViews: List<SurfaceView>

    abstract val overViews: List<SurfaceView>

    abstract val fullscreenView: SurfaceView?

    abstract val lockViews: List<SurfaceView>

    abstract fun geCursorViews(): List<SurfaceView>

    companion object {
        fun create(backgroundView: SurfaceView?,
                   underViews: List<SurfaceView>,
                   applicationViews: List<SurfaceView>,
                   overViews: List<SurfaceView>,
                   fullscreenView: SurfaceView?,
                   lockViews: List<SurfaceView>,
                   cursorViews: List<SurfaceView>): Subscene {
            return AutoValue_Subscene(backgroundView,
                                      underViews,
                                      applicationViews,
                                      overViews,
                                      fullscreenView,
                                      lockViews,
                                      cursorViews)
        }
    }

}
