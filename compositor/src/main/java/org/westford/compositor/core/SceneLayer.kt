package org.westford.compositor.core


import javax.inject.Inject
import java.util.LinkedList

class SceneLayer @Inject
constructor() {
    val surfaceViews = LinkedList<SurfaceView>()
}
