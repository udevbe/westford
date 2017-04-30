package org.westford.compositor.core

import java.util.*
import javax.inject.Inject

class SceneLayer @Inject constructor() {
    val surfaceViews = LinkedList<SurfaceView>()
}
