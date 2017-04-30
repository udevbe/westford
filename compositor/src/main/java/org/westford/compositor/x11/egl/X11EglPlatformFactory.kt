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

import org.freedesktop.jaccall.Pointer
import org.freedesktop.wayland.shared.WlOutputTransform
import org.westford.compositor.core.GlRenderer
import org.westford.compositor.core.Output
import org.westford.compositor.core.OutputFactory
import org.westford.compositor.core.OutputGeometry
import org.westford.compositor.core.OutputMode
import org.westford.compositor.core.events.RenderOutputDestroyed
import org.westford.compositor.protocol.WlOutput
import org.westford.compositor.x11.X11Platform
import org.westford.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT
import org.westford.nativ.libEGL.EglGetPlatformDisplayEXT
import org.westford.nativ.libEGL.LibEGL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_BACK_BUFFER
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_CLIENT_APIS
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_CONTEXT_CLIENT_VERSION
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_EXTENSIONS
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NONE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NO_CONTEXT
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NO_DISPLAY
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_PLATFORM_X11_KHR
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_RENDER_BUFFER
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_VENDOR
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_VERSION
import org.westford.nativ.libxcb.Libxcb
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_CLIENT_MESSAGE
import org.westford.nativ.libxcb.xcb_client_message_event_t
import java.lang.String.format
import java.util.logging.Logger
import javax.inject.Inject

class X11EglPlatformFactory @Inject internal constructor(private val libxcb: Libxcb,
                                                         private val libEGL: LibEGL,
                                                         private val privateX11EglPlatformFactory: PrivateX11EglPlatformFactory,
                                                         private val x11Platform: X11Platform,
                                                         private val wlOutputFactory: WlOutputFactory,
                                                         private val outputFactory: OutputFactory,
                                                         private val glRenderer: GlRenderer,
                                                         private val x11EglOutputFactory: X11EglOutputFactory) {

    fun create(): X11EglPlatform {

        val eglDisplay = createEglDisplay(this.x11Platform.getxDisplay())

        val eglExtensions = Pointer.wrap<String>(String::class.java,
                                                 this.libEGL.eglQueryString(eglDisplay,
                                                                            EGL_EXTENSIONS)).dref()
        val eglClientApis = Pointer.wrap<String>(String::class.java,
                                                 this.libEGL.eglQueryString(eglDisplay,
                                                                            EGL_CLIENT_APIS)).dref()
        val eglVendor = Pointer.wrap<String>(String::class.java,
                                             this.libEGL.eglQueryString(eglDisplay,
                                                                        EGL_VENDOR)).dref()
        val eglVersion = Pointer.wrap<String>(String::class.java,
                                              this.libEGL.eglQueryString(eglDisplay,
                                                                         EGL_VERSION)).dref()

        LOGGER.info(format("Creating X11 EGL output:\n" + "\tEGL client apis: %s\n" + "\tEGL vendor: %s\n" + "\tEGL version: %s\n" + "\tEGL extensions: %s",
                           eglClientApis,
                           eglVendor,
                           eglVersion,
                           eglExtensions))

        val eglConfig = this.glRenderer.eglConfig(eglDisplay,
                                                  eglExtensions)
        val eglContext = createEglContext(eglDisplay,
                                          eglConfig)

        val x11Outputs = this.x11Platform.renderOutputs
        val x11EglOutputs = mutableListOf<X11EglOutput>()
        val wlOutputs = mutableListOf<WlOutput>()

        x11Outputs.forEach {
            x11EglOutputs.add(this.x11EglOutputFactory.create(it,
                                                              createEglSurface(eglDisplay,
                                                                               eglConfig,
                                                                               it.xWindow),
                                                              eglContext,
                                                              eglDisplay))
        }
        x11EglOutputs.forEach {
            wlOutputs.add(this.wlOutputFactory.create(createOutput(it)))
        }

        val x11EglPlatform = this.privateX11EglPlatformFactory.create(wlOutputs,
                                                                      eglDisplay,
                                                                      eglContext,
                                                                      eglExtensions)

        this.x11Platform.x11EventBus.xEventSignal.connect {
            val responseType = it.dref().response_type() and 0x7f
            when (responseType) {
                XCB_CLIENT_MESSAGE -> {
                    handle(it.castp(xcb_client_message_event_t::class.java),
                           x11EglPlatform)
                }
            }
        }

        return x11EglPlatform
    }

    private fun createEglDisplay(nativeDisplay: Long): Long {

        val noDisplayExtensions = Pointer.wrap<String>(String::class.java,
                                                       this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                  EGL_EXTENSIONS))
        if (noDisplayExtensions.address == 0L) {
            throw RuntimeException("Could not query egl extensions.")
        }
        val extensions = noDisplayExtensions.dref()

        if (!extensions.contains("EGL_EXT_platform_x11")) {
            throw RuntimeException("Required extension EGL_EXT_platform_x11 not available.")
        }

        val eglGetPlatformDisplayEXT = Pointer.wrap<EglGetPlatformDisplayEXT>(EglGetPlatformDisplayEXT::class.java,
                                                                              this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address))

        val eglDisplay = eglGetPlatformDisplayEXT.dref().`$`(EGL_PLATFORM_X11_KHR,
                                                             nativeDisplay,
                                                             0L)
        if (eglDisplay == 0L) {
            throw RuntimeException("eglGetDisplay() failed")
        }
        if (this.libEGL.eglInitialize(eglDisplay,
                                      0L,
                                      0L) == 0) {
            throw RuntimeException("eglInitialize() failed")
        }

        return eglDisplay
    }

    private fun createEglContext(eglDisplay: Long,
                                 config: Long): Long {
        val eglContextAttribs = Pointer.nref(//@formatter:off
                EGL_CONTEXT_CLIENT_VERSION, 2,
EGL_NONE
 //@formatter:on
                                            )
        val context = this.libEGL.eglCreateContext(eglDisplay,
                                                   config,
                                                   EGL_NO_CONTEXT,
                                                   eglContextAttribs.address)
        if (context == 0L) {
            throw RuntimeException("eglCreateContext() failed")
        }
        return context
    }

    private fun createEglSurface(eglDisplay: Long,
                                 config: Long,
                                 nativeWindow: Int): Long {
        val eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                             EGL_BACK_BUFFER,
                                             EGL_NONE)

        val eglGetPlatformDisplayEXT = Pointer.wrap<EglCreatePlatformWindowSurfaceEXT>(EglCreatePlatformWindowSurfaceEXT::class.java,
                                                                                       this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address))
        val eglSurface = eglGetPlatformDisplayEXT.dref().`$`(eglDisplay,
                                                             config,
                                                             Pointer.nref(nativeWindow).address,
                                                             eglSurfaceAttribs.address)
        if (eglSurface == 0L) {
            throw RuntimeException("eglCreateWindowSurface() failed")
        }

        return eglSurface
    }

    private fun createOutput(x11EglOutput: X11EglOutput): Output {
        val x11Output = x11EglOutput.x11Output

        val screen = x11Output.screen
        val width = x11Output.width
        val height = x11Output.height

        val outputGeometry = OutputGeometry.builder().x(x11Output.x).y(x11Output.y).subpixel(0).make("Westford xcb").model("X11").physicalWidth(width / screen.width_in_pixels() * screen.width_in_millimeters()).physicalHeight(height / screen.height_in_pixels() * screen.height_in_millimeters()).transform(WlOutputTransform.NORMAL.value).build()
        val outputMode = OutputMode.builder().flags(0).width(width).height(height).refresh(60).build()
        return this.outputFactory.create(x11EglOutput,
                                         x11Output.name,
                                         outputGeometry,
                                         outputMode)
    }

    private fun handle(event: Pointer<xcb_client_message_event_t>,
                       x11EglPlatform: X11EglPlatform) {
        val atom = event.dref().data().data32().dref()
        val sourceWindow = event.dref().window()

        if (atom == this.x11Platform.x11Atoms["WM_DELETE_WINDOW"]) {

            val wlOutputIterator = x11EglPlatform.wlOutputs.iterator()
            while (wlOutputIterator.hasNext()) {
                val wlOutput = wlOutputIterator.next()
                val x11EglOutput = wlOutput.output.renderOutput as X11EglOutput
                val x11Output = x11EglOutput.x11Output

                if (x11Output.xWindow == sourceWindow) {
                    this.libxcb.xcb_destroy_window(this.x11Platform.xcbConnection,
                                                   sourceWindow)
                    wlOutputIterator.remove()
                    x11EglPlatform.renderOutputDestroyedSignal.emit(RenderOutputDestroyed.create(wlOutput))
                    return
                }
            }
        }
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }
}
