package org.westford.compositor.core

import com.google.auto.value.AutoValue
import java.util.Optional

/**
 * A subsection of a [Scene]. Has no relation with [Subsurface].
 */
@AutoValue
abstract class Subscene {

    abstract val backgroundView: Optional<SurfaceView>

    abstract val underViews: List<SurfaceView>

    abstract val applicationViews: List<SurfaceView>

    abstract val overViews: List<SurfaceView>

    abstract val fullscreenView: Optional<SurfaceView>

    abstract val lockViews: List<SurfaceView>

    abstract fun geCursorViews(): List<SurfaceView>

    companion object {
        fun create(backgroundView: Optional<SurfaceView>,
                   underViews: List<SurfaceView>,
                   applicationViews: List<SurfaceView>,
                   overViews: List<SurfaceView>,
                   fullscreenView: Optional<SurfaceView>,
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
