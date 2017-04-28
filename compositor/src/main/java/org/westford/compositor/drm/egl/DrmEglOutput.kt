/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.drm.egl

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sun.javafx.scene.SceneUtils
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Size
import org.freedesktop.jaccall.Unsigned
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.EventSource
import org.freedesktop.wayland.server.WlBufferResource
import org.westford.compositor.core.Buffer
import org.westford.compositor.core.EglOutput
import org.westford.compositor.core.EglOutputState
import org.westford.compositor.core.Output
import org.westford.compositor.core.OutputMode
import org.westford.compositor.core.Rectangle
import org.westford.compositor.core.Region
import org.westford.compositor.core.Scene
import org.westford.compositor.core.Subscene
import org.westford.compositor.core.Surface
import org.westford.compositor.core.SurfaceView
import org.westford.compositor.drm.DrmOutput
import org.westford.compositor.drm.DrmPageFlipCallback
import org.westford.compositor.gles2.Gles2Painter
import org.westford.compositor.gles2.Gles2PainterFactory
import org.westford.compositor.gles2.Gles2Renderer
import org.westford.compositor.protocol.WlOutput
import org.westford.compositor.protocol.WlSurface
import org.westford.nativ.glibc.Libc
import org.westford.nativ.libdrm.Libdrm
import org.westford.nativ.libgbm.Libgbm
import org.westford.nativ.libgbm.Pointerdestroy_user_data
import java.util.Optional

import org.westford.nativ.libdrm.Libdrm.Companion.DRM_MODE_PAGE_FLIP_EVENT
import org.westford.nativ.libgbm.Libgbm.Companion.GBM_FORMAT_ARGB8888
import org.westford.nativ.libgbm.Libgbm.Companion.GBM_FORMAT_XRGB8888

@AutoFactory(allowSubclasses = true,
             className = "DrmEglOutputFactory") class DrmEglOutput(@param:Provided private val libc: Libc,
                                                                   @param:Provided private val libgbm: Libgbm,
                                                                   @param:Provided private val libdrm: Libdrm,
                                                                   @param:Provided private val display: Display,
                                                                   @Provided gles2PainterFactory: org.westford.compositor.gles2.Gles2PainterFactory,
                                                                   @param:Provided private val gles2Renderer: Gles2Renderer,
                                                                   @param:Provided private val scene: Scene,
                                                                   @param:Provided private val gbmBoFactory: GbmBoFactory,
                                                                   private val drmFd: Int,
                                                                   private val gbmDevice: Long,
                                                                   private var gbmBo: GbmBo,
                                                                   private val gbmSurface: Long,
                                                                   val drmOutput: DrmOutput,
                                                                   override val eglSurface: Long,
                                                                   override val eglContext: Long,
                                                                   override val eglDisplay: Long) : EglOutput, DrmPageFlipCallback {

    private val gles2PainterFactory: Gles2PainterFactory

    private var nextGbmBo: GbmBo

    private var renderPending = false
    private var pageFlipPending = false
    private var afterPageFlipRender: (() -> Unit)? = null
    private var onIdleEventSource: EventSource? = null
    private var enabled: Boolean = false

    init {
        this.gles2PainterFactory = gles2PainterFactory
        this.nextGbmBo = gbmBo
    }

    private fun schedulePageFlip() {
        this.libdrm.drmModePageFlip(this.drmFd,
                                    this.drmOutput.crtcId,
                                    getFbId(this.nextGbmBo),
                                    DRM_MODE_PAGE_FLIP_EVENT,
                                    Pointer.from(this).address)
        this.pageFlipPending = true
    }

    private fun getFbId(gbmBo: GbmBo): Int {
        val fbIdP = this.libgbm.gbm_bo_get_user_data(gbmBo.gbmBo)
        if (fbIdP != 0L) {
            return Pointer.wrap<Int>(Int::class.java,
                                     fbIdP).dref()
        }

        val fb = Pointer.calloc<Int>(1,
                                     Size.sizeof(null as Int),
                                     Int::class.java)
        val gbmBoPtr = this.gbmBo.gbmBo
        val format = this.libgbm.gbm_bo_get_format(gbmBoPtr)
        val width = this.libgbm.gbm_bo_get_width(gbmBoPtr)
        val height = this.libgbm.gbm_bo_get_height(gbmBoPtr)
        val stride = this.libgbm.gbm_bo_get_stride(gbmBoPtr)
        val handle = this.libgbm.gbm_bo_get_handle(gbmBoPtr).toInt()

        val handles = Pointer.nref(handle,
                                   0,
                                   0,
                                   0)
        val pitches = Pointer.nref(stride,
                                   0,
                                   0,
                                   0)
        val offsets = Pointer.nref(*IntArray(4))

        val ret = this.libdrm.drmModeAddFB2(this.drmFd,
                                            width,
                                            height,
                                            format,
                                            handles.address,
                                            pitches.address,
                                            offsets.address,
                                            fb.address,
                                            0)
        if (ret != 0) {
            throw RuntimeException("failed to create fb")
        }

        this.libgbm.gbm_bo_set_user_data(gbmBoPtr,
                                         fb.address,
                                         Pointerdestroy_user_data.nref(???({ bo, data ->
            this.destroyUserData(bo,
                                 data)
        })).address)

        return fb.dref()
    }

    override fun onPageFlip(@Unsigned sequence: Int,
                            @Unsigned tv_sec: Int,
                            @Unsigned tv_usec: Int) {
        this.gbmBo.close()

        this.gbmBo = this.nextGbmBo
        this.pageFlipPending = false

        this.afterPageFlipRender?.invoke()
        this.afterPageFlipRender = null
    }

    private fun destroyUserData(@Ptr bo: Long,
                                @Ptr data: Long) {
        val fbIdP = Pointer.wrap<Int>(Int::class.java,
                                      data)
        val fbId = fbIdP.dref()
        this.libdrm.drmModeRmFB(this.drmFd,
                                fbId)
        fbIdP.close()
    }

    private fun doRender(wlOutput: WlOutput) {
        this.onIdleEventSource = null
        val painter = this.gles2PainterFactory.create(this,
                                                      wlOutput)
        val subscene = this.scene.subsection(wlOutput.output.region)

        val cursorPlane = toCursorPlane(wlOutput,
                                        subscene.geCursorViews())
        //If we can't offload to some kind of cursor plane then we are forced to put it on the primary plane.
        //This means we can't really offload anything to other planes as hey would be shown on top of the cursor
        //primary plane
        if (cursorPlane!!.isPresent) {
            //continue offloading to overlay planes
            val lockViewsPlane = toOverlayPlane(wlOutput,
                                                subscene.lockViews)
            if (lockViewsPlane!!.isPresent) {
                return
            }

            val fullscreenPlane = toPrimaryPlane(wlOutput,
                                                 subscene.fullscreenView)
            if (fullscreenPlane!!.isPresent) {
                return
            }

            val overPlane = toOverlayPlane(wlOutput,
                                           subscene.overViews)
            if (!overPlane!!.isPresent) {
                toPrimaryPlane(wlOutput,
                               subscene.backgroundView,
                               subscene.underViews,
                               subscene.applicationViews,
                               subscene.overViews)
            }

            val applicationsPlane = toOverlayPlane(wlOutput,
                                                   subscene.applicationViews)
            if (!applicationsPlane!!.isPresent) {
                toPrimaryPlane(wlOutput,
                               subscene.backgroundView,
                               subscene.underViews,
                               subscene.applicationViews)
            }

            val underPlane = toOverlayPlane(wlOutput,
                                            subscene.underViews)
            if (!underPlane!!.isPresent) {
                toPrimaryPlane(wlOutput,
                               subscene.backgroundView,
                               subscene.underViews)
            }

            toPrimaryPlane(wlOutput,
                           subscene.backgroundView)
        }
        else {
            //put everything on the primary plane
            toPrimaryPlane(wlOutput,
                           subscene)
        }


        paint(wlOutput,
              painter,
              subscene)

        //TODO paint cursors on separate overlay
        subscene.geCursorViews().forEach(Consumer<SurfaceView> { painter.paint(it) })

        //FIXME how to compose different gbm_bos?
        if (painter.commit()) {
            this.nextGbmBo = this.gbmBoFactory.create(this.gbmSurface)
        }
        schedulePageFlip()

        this.display.flushClients()
        this.renderPending = false
    }

    private fun toPrimaryPlane(wlOutput: WlOutput,
                               subscene: Subscene): DrmPlane? {
        return null
    }

    private fun toPrimaryPlane(wlOutput: WlOutput,
                               view: SurfaceView?,
                               vararg views: List<SurfaceView>): DrmPlane? {
        return null
    }

    private fun toOverlayPlane(wlOutput: WlOutput,
                               views: List<SurfaceView>): DrmPlane? {
        return null
    }

    private fun toCursorPlane(wlOutput: WlOutput,
                              surfaceViews: List<SurfaceView>): DrmPlane? {
        return null
    }

    private fun paint(wlOutput: WlOutput,
                      gles2Painter: Gles2Painter,
                      subscene: Subscene) {

        //naive generic single pass, bottom to top overdraw rendering.
        val lockViews = subscene.lockViews
        val fullscreenView = subscene.fullscreenView

        if (!lockViews.isEmpty()) {
            lockViews.forEach {
                gles2Painter.paint(it)
            }
            //lockscreen(s) hide(s) all other screens.
            return
        }

        if (fullscreenView != null && paintFullscreen(gles2Painter,
                                                      wlOutput,
                                                      fullscreenView)) {
            //fullscreen view painted, don't bother painting underlying views
            return
        }

        subscene.backgroundView?.let { gles2Painter.paint(it) }
        subscene.underViews.forEach { gles2Painter.paint(it) }
        subscene.applicationViews.forEach { gles2Painter.paint(it) }
        subscene.overViews.forEach { gles2Painter.paint(it) }
    }

    override fun disable() {
        this.afterPageFlipRender = null
        this.onIdleEventSource?.remove()
        this.enabled = false
    }

    override fun enable(wlOutput: WlOutput) {
        this.enabled = true
        render(wlOutput)
    }

    override fun render(wlOutput: WlOutput) {
        if (this.enabled) {
            scheduleRender(wlOutput)
        }
    }

    private fun scheduleRender(wlOutput: WlOutput) {
        //TODO unit test 3 cases here: schedule idle, no-op when already scheduled, delayed render when pageflip pending

        //schedule a new render as soon as the pageflip ends, but only if we haven't scheduled one already
        if (this.pageFlipPending) {
            if (this.afterPageFlipRender == null) {
                this.afterPageFlipRender = { whenIdleDoRender(wlOutput) }
            }
        }
        else if (!this.renderPending) {
            whenIdleDoRender(wlOutput)
        }//schedule a new render but only if we haven't scheduled one already.
    }

    private fun whenIdleDoRender(wlOutput: WlOutput) {
        this.renderPending = true
        this.onIdleEventSource = this.display.eventLoop.addIdle { doRender(wlOutput) }
    }

    fun setDefaultMode() {
        val fbId = getFbId(this.gbmBo)

        val error = this.libdrm.drmModeSetCrtc(this.drmFd,
                                               this.drmOutput.crtcId,
                                               fbId,
                                               0,
                                               0,
                                               Pointer.nref(this.drmOutput.drmModeConnector.connector_id()).address,
                                               1,
                                               Pointer.ref(this.drmOutput.mode).address)
        if (error != 0) {
            throw RuntimeException(String.format("failed to drmModeSetCrtc. [%d]",
                                                 this.libc.errno))
        }
    }

    private fun paintFullscreen(gles2Painter: Gles2Painter,
                                wlOutput: WlOutput,
                                surfaceView: SurfaceView): Boolean {

        if (!surfaceView.isEnabled || !surfaceView.isDrawable) {
            return false
        }

        val output = wlOutput.output

        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val wlBufferResource = surface.state.buffer
        //the null check of wlBufferResource is done in surfaceView.isDrawable
        val buffer = this.gles2Renderer.queryBuffer(wlBufferResource!!)
        val mode = output.mode


        if (buffer.width == mode.width && buffer.height == mode.height) {

            val gbmBo = this.gbmBoFactory.create(this.gbmDevice,
                                                 wlBufferResource)
            if (gbmBo.gbmBo == 0L) {
                //buffer import failed, fallback to painter
                return gles2Painter.paint(surfaceView)
            }

            val format = getScanoutFormat(gbmBo,
                                          mode,
                                          surface)
            if (format == 0) {
                //no suitable scanout pixel format, fallback to gles2
                gbmBo.close()
                return gles2Painter.paint(surfaceView)
            }

            this.nextGbmBo = gbmBo
            return true
        }
        else {
            //no direct scanout possible, fallback to gles2
            return gles2Painter.paint(surfaceView)
        }
    }

    private fun getScanoutFormat(clientGbmBo: GbmBo,
                                 mode: OutputMode,
                                 surface: Surface): Int {
        if (clientGbmBo.gbmBo == 0L) {
            return 0
        }

        var clientFormat = this.libgbm.gbm_bo_get_format(clientGbmBo.gbmBo)

        if (clientFormat == GBM_FORMAT_ARGB8888 && surface.state.opaqueRegion != null) {
            val opaqueCopy = surface.state.opaqueRegion?.copy()
            opaqueCopy.subtract(Rectangle.create(0,
                                                 0,
                                                 mode.width,
                                                 mode.height))
            if (opaqueCopy.isEmpty) {
                clientFormat = GBM_FORMAT_XRGB8888
            }
        }

        val format = this.libgbm.gbm_bo_get_format(this.gbmBo.gbmBo)
        if (format == clientFormat) {
            return clientFormat
        }

        return 0
    }
}
