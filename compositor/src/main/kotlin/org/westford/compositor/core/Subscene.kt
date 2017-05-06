package org.westford.compositor.core

data class Subscene(val backgroundView: SurfaceView?,
                    val underViews: List<SurfaceView>,
                    val applicationViews: List<SurfaceView>,
                    val overViews: List<SurfaceView>,
                    val fullscreenView: SurfaceView?,
                    val lockViews: List<SurfaceView>,
                    val cursorViews: List<SurfaceView>)
