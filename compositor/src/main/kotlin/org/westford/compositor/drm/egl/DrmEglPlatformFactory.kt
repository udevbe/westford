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

import org.freedesktop.jaccall.Pointer
import org.westford.compositor.core.GlRenderer
import org.westford.compositor.core.OutputFactory
import org.westford.compositor.core.OutputGeometry
import org.westford.compositor.core.OutputMode
import org.westford.compositor.drm.DrmOutput
import org.westford.compositor.drm.DrmPlatform
import org.westford.compositor.protocol.WlOutput
import org.westford.compositor.protocol.WlOutputFactory
import org.westford.launch.LifeCycleSignals
import org.westford.launch.Privileges
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
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_PLATFORM_GBM_KHR
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_RENDER_BUFFER
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_VENDOR
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_VERSION
import org.westford.nativ.libGLESv2.LibGLESv2
import org.westford.nativ.libgbm.Libgbm
import java.lang.String.format
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject

class DrmEglPlatformFactory @Inject internal constructor(private val wlOutputFactory: WlOutputFactory,
                                                         private val outputFactory: OutputFactory,
                                                         private val privateDrmEglPlatformFactory: PrivateDrmEglPlatformFactory,
                                                         private val libgbm: Libgbm,
                                                         private val gbmBoFactory: GbmBoFactory,
                                                         private val libEGL: LibEGL,
                                                         private val libGLESv2: LibGLESv2,
                                                         private val drmPlatform: DrmPlatform,
                                                         private val drmEglOutputFactory: DrmEglOutputFactory,
                                                         private val glRenderer: GlRenderer,
                                                         private val lifeCycleSignals: LifeCycleSignals,
                                                         private val privileges: Privileges) {

    fun create(): DrmEglPlatform {
        val gbmDevice = this.libgbm.gbm_create_device(this.drmPlatform.drmFd)
        val eglDisplay = createEglDisplay(gbmDevice)

        val eglExtensions = Pointer.wrap<String>(String::class.java,
                                                 this.libEGL.eglQueryString(eglDisplay,
                                                                            EGL_EXTENSIONS)).get()
        val eglClientApis = Pointer.wrap<String>(String::class.java,
                                                 this.libEGL.eglQueryString(eglDisplay,
                                                                            EGL_CLIENT_APIS)).get()
        val eglVendor = Pointer.wrap<String>(String::class.java,
                                             this.libEGL.eglQueryString(eglDisplay,
                                                                        EGL_VENDOR)).get()
        val eglVersion = Pointer.wrap<String>(String::class.java,
                                              this.libEGL.eglQueryString(eglDisplay,
                                                                         EGL_VERSION)).get()

        LOGGER.info(format("Creating DRM EGL output:\n" + "\tEGL client apis: %s\n" + "\tEGL vendor: %s\n" + "\tEGL version: %s\n" + "\tEGL extensions: %s",
                           eglClientApis,
                           eglVendor,
                           eglVersion,
                           eglExtensions))

        val eglConfig = this.glRenderer.eglConfig(eglDisplay,
                                                  eglExtensions)
        val eglContext = createEglContext(eglDisplay,
                                          eglConfig)

        val drmOutputs = this.drmPlatform.renderOutputs
        val drmEglRenderOutputs = ArrayList<DrmEglOutput>(drmOutputs.size)
        val wlOutputs = ArrayList<WlOutput>(drmEglRenderOutputs.size)

        drmOutputs.forEach {
            drmEglRenderOutputs.add(createDrmEglRenderOutput(it,
                                                             gbmDevice,
                                                             eglDisplay,
                                                             eglContext,
                                                             eglConfig))
        }
        drmEglRenderOutputs.forEach {
            wlOutputs.add(createWlOutput(it))
        }

        this.lifeCycleSignals.activateSignal.connect {
            this.privileges.setDrmMaster(this.drmPlatform.drmFd)
            wlOutputs.forEach {
                val drmEglOutput = it.output.renderOutput as DrmEglOutput
                drmEglOutput.setDefaultMode()
                drmEglOutput.enable(it)
            }
        }
        this.lifeCycleSignals.deactivateSignal.connect {
            drmEglRenderOutputs.forEach {
                it.disable()
            }
            this.privileges.dropDrmMaster(this.drmPlatform.drmFd)
        }


        return this.privateDrmEglPlatformFactory.create(gbmDevice,
                                                        eglDisplay,
                                                        eglContext,
                                                        eglExtensions,
                                                        wlOutputs)
    }

    private fun createWlOutput(drmEglOutput: DrmEglOutput): WlOutput {

        val drmOutput = drmEglOutput.drmOutput

        val drmModeConnector = drmOutput.drmModeConnector
        val drmModeModeInfo = drmOutput.mode

        val fallBackDpi = 96

        var mmWidth = drmModeConnector.mmWidth
        val hdisplay = drmOutput.mode.hdisplay
        if (mmWidth == 0) {
            mmWidth = (hdisplay * 25.4 / fallBackDpi).toInt()
        }

        var mmHeight = drmModeConnector.mmHeight
        val vdisplay = drmOutput.mode.vdisplay
        if (mmHeight == 0) {
            mmHeight = (vdisplay * 25.4 / fallBackDpi).toInt()
        }

        //TODO gather more geo & drmModeModeInfo info
        val outputGeometry = OutputGeometry(physicalWidth = mmWidth,
                                            physicalHeight = mmHeight,
                                            make = "unknown",
                                            model = "unknown",
                                            x = 0,
                                            y = 0,
                                            subpixel = drmModeConnector.drmModeSubPixel,
                                            transform = 0)
        val outputMode = OutputMode(width = hdisplay.toInt(),
                                    height = vdisplay.toInt(),
                                    refresh = drmOutput.mode.vrefresh,
                                    flags = drmModeModeInfo.flags)

        //FIXME deduce an output name from the drm connector
        return this.wlOutputFactory.create(this.outputFactory.create(drmEglOutput,
                                                                     "fixme",
                                                                     outputGeometry,
                                                                     outputMode))
    }

    private fun createEglDisplay(gbmDevice: Long): Long {

        val noDisplayExtensions = Pointer.wrap<String>(String::class.java,
                                                       this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                  EGL_EXTENSIONS))
        if (noDisplayExtensions.address == 0L) {
            throw RuntimeException("Could not query egl extensions.")
        }
        val extensions = noDisplayExtensions.get()

        if (!extensions.contains("EGL_MESA_platform_gbm")) {
            throw RuntimeException("Required extension EGL_MESA_platform_gbm not available.")
        }

        val eglGetPlatformDisplayEXT = Pointer.wrap<EglGetPlatformDisplayEXT>(EglGetPlatformDisplayEXT::class.java,
                                                                              this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address))

        val eglDisplay = eglGetPlatformDisplayEXT.get()(EGL_PLATFORM_GBM_KHR,
                                                        gbmDevice,
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

    private fun createDrmEglRenderOutput(drmOutput: DrmOutput,
                                         gbmDevice: Long,
                                         eglDisplay: Long,
                                         eglContext: Long,
                                         eglConfig: Long): DrmEglOutput {

        val drmModeModeInfo = drmOutput.mode

        //TODO test if format is supported (gbm_device_is_format_supported)?
        val gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                        drmModeModeInfo.hdisplay.toInt(),
                                                        drmModeModeInfo.vdisplay.toInt(),
                                                        Libgbm.GBM_FORMAT_XRGB8888,
                                                        Libgbm.GBM_BO_USE_SCANOUT or Libgbm.GBM_BO_USE_RENDERING)

        if (gbmSurface == 0L) {
            throw RuntimeException("failed to create gbm surface")
        }

        val eglSurface = createEglSurface(eglDisplay,
                                          eglConfig,
                                          gbmSurface)

        this.libEGL.eglMakeCurrent(eglDisplay,
                                   eglSurface,
                                   eglSurface,
                                   eglContext)
        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f)
        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT)
        this.libEGL.eglSwapBuffers(eglDisplay,
                                   eglSurface)

        val gbmBo = this.gbmBoFactory.create(gbmSurface)
        val drmEglRenderOutput = this.drmEglOutputFactory.create(this.drmPlatform.drmFd,
                                                                 gbmDevice,
                                                                 gbmBo,
                                                                 gbmSurface,
                                                                 drmOutput,
                                                                 eglSurface,
                                                                 eglContext,
                                                                 eglDisplay)
        drmEglRenderOutput.setDefaultMode()

        return drmEglRenderOutput
    }

    private fun createEglSurface(eglDisplay: Long,
                                 config: Long,
                                 gbmSurface: Long): Long {
        val eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                             EGL_BACK_BUFFER,
                                             EGL_NONE)

        val eglGetPlatformDisplayEXT = Pointer.wrap<EglCreatePlatformWindowSurfaceEXT>(EglCreatePlatformWindowSurfaceEXT::class.java,
                                                                                       this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address))
        val eglSurface = eglGetPlatformDisplayEXT.get()(eglDisplay,
                                                        config,
                                                        gbmSurface,
                                                        eglSurfaceAttribs.address)
        if (eglSurface == 0L) {
            throw RuntimeException("eglCreateWindowSurface() failed")
        }

        return eglSurface
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }
}
