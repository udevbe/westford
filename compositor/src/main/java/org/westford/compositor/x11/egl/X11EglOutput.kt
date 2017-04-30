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
package org.westford.compositor.x11.egl

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.Display
import org.westford.compositor.core.EglOutput
import org.westford.compositor.core.EglOutputState
import org.westford.compositor.core.Scene
import org.westford.compositor.protocol.WlOutput
import org.westford.compositor.x11.X11Output

@AutoFactory(allowSubclasses = true,
             className = "X11EglOutputFactory") class X11EglOutput(@param:Provided private val display: Display,
                                                                   @Provided gles2PainterFactory: org.westford.compositor.gles2.Gles2PainterFactory,
                                                                   @param:Provided private val scene: Scene,
                                                                   val x11Output: X11Output,
                                                                   override val eglSurface: Long,
                                                                   override val eglContext: Long,
                                                                   override val eglDisplay: Long) : EglOutput {

    private val gles2PainterFactory: Gles2PainterFactory

    private var renderScheduled = false

    override var state: EglOutputState? = null

    init {
        this.gles2PainterFactory = gles2PainterFactory
    }

    override fun render(wlOutput: WlOutput) {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        whenIdleDoRender(wlOutput)
    }

    private fun whenIdleDoRender(wlOutput: WlOutput) {
        if (!this.renderScheduled) {
            this.renderScheduled = true
            this.display.eventLoop.addIdle { doRender(wlOutput) }
        }
    }

    private fun doRender(wlOutput: WlOutput) {
        paint(wlOutput)
        this.display.flushClients()
        this.renderScheduled = false
    }

    private fun paint(wlOutput: WlOutput) {

        val subscene = this.scene.subsection(wlOutput.output.region)

        val gles2Painter = this.gles2PainterFactory.create(this,
                                                           wlOutput)

        //naive generic single pass, bottom to top overdraw rendering.
        val lockViews = subscene.lockViews
        val fullscreenView = subscene.fullscreenView

        //lockscreen(s) hide all other screens.
        if (!lockViews.isEmpty()) {
            lockViews.forEach {
                gles2Painter.paint(it)
            }
        }
        else {
            fullscreenView?.let { fullscreenSurfaceView ->
                //try painting fullscreen view
                if (!gles2Painter.paint(fullscreenSurfaceView)) {
                    //fullscreen view not visible, paint the rest of the subscene.
                    subscene.backgroundView?.let {
                        gles2Painter.paint(it)
                    }
                    subscene.underViews.forEach {
                        gles2Painter.paint(it)
                    }
                    subscene.applicationViews.forEach {
                        gles2Painter.paint(it)
                    }
                    subscene.overViews.forEach {
                        gles2Painter.paint(it)
                    }
                }
            }
        }

        subscene.geCursorViews().forEach {
            gles2Painter.paint(it)
        }
        gles2Painter.commit()
    }
}
