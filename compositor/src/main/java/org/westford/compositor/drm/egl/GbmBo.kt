package org.westford.compositor.drm.egl

interface GbmBo : AutoCloseable {
    val gbmBo: Long

    override fun close()
}
