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
package org.westford.compositor.gles2

import org.freedesktop.jaccall.JNI
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Pointer.malloc
import org.freedesktop.jaccall.Pointer.wrap
import org.freedesktop.jaccall.Size.sizeof
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.ShmBuffer
import org.freedesktop.wayland.server.WlBufferResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.freedesktop.wayland.shared.WlShmFormat
import org.westford.compositor.core.Buffer
import org.westford.compositor.core.BufferVisitor
import org.westford.compositor.core.EglBuffer
import org.westford.compositor.core.EglOutput
import org.westford.compositor.core.EglOutputState
import org.westford.compositor.core.EglSurfaceState
import org.westford.compositor.core.GlRenderer
import org.westford.compositor.core.Output
import org.westford.compositor.core.ShmSurfaceState
import org.westford.compositor.core.SmBuffer
import org.westford.compositor.core.SurfaceRenderState
import org.westford.compositor.core.SurfaceRenderStateVisitor
import org.westford.compositor.core.SurfaceView
import org.westford.compositor.core.UnsupportedBuffer
import org.westford.compositor.core.calc.Mat4
import org.westford.compositor.gles2.Gles2Shaders.FRAGMENT_SHADER_EGL_EXTERNAL
import org.westford.compositor.gles2.Gles2Shaders.FRAGMENT_SHADER_EGL_Y_UV
import org.westford.compositor.gles2.Gles2Shaders.FRAGMENT_SHADER_EGL_Y_U_V
import org.westford.compositor.gles2.Gles2Shaders.FRAGMENT_SHADER_EGL_Y_XUXV
import org.westford.compositor.gles2.Gles2Shaders.VERTEX_SHADER
import org.westford.compositor.protocol.WlOutput
import org.westford.compositor.protocol.WlSurface
import org.westford.nativ.libEGL.EglBindWaylandDisplayWL
import org.westford.nativ.libEGL.EglCreateImageKHR
import org.westford.nativ.libEGL.EglDestroyImageKHR
import org.westford.nativ.libEGL.EglQueryWaylandBufferWL
import org.westford.nativ.libEGL.LibEGL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_ALPHA_SIZE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_BLUE_SIZE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_GREEN_SIZE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_HEIGHT
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NONE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NO_CONTEXT
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NO_DISPLAY
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_NO_IMAGE_KHR
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_OPENGL_ES2_BIT
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_OPENGL_ES_API
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_RED_SIZE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_RENDERABLE_TYPE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_SURFACE_TYPE
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_EXTERNAL_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_FORMAT
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_RGB
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_RGBA
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_Y_UV_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_Y_U_V_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_TEXTURE_Y_XUXV_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_WAYLAND_BUFFER_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_WAYLAND_PLANE_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_WAYLAND_Y_INVERTED_WL
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_WIDTH
import org.westford.nativ.libEGL.LibEGL.Companion.EGL_WINDOW_BIT
import org.westford.nativ.libGLESv2.GlEGLImageTargetTexture2DOES
import org.westford.nativ.libGLESv2.LibGLESv2
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class Gles2Renderer @Inject internal constructor(private val libEGL: LibEGL,
                                                            private val libGLESv2: LibGLESv2,
                                                            private val display: Display) : GlRenderer {

    private val textureArgs = IntArray(3)
    private var eglQueryWaylandBufferWL: EglQueryWaylandBufferWL? = null
    private var eglCreateImageKHR: EglCreateImageKHR? = null
    private var eglDestroyImageKHR: EglDestroyImageKHR? = null
    private var glEGLImageTargetTexture2DOES: GlEGLImageTargetTexture2DOES? = null
    //shader programs
    //used by shm & egl
    private var argb8888ShaderProgram: Int = 0
    //used by shm
    private var xrgb8888ShaderProgram: Int = 0
    //used by egl
    private var y_u_vShaderProgram: Int = 0
    private var y_uvShaderProgram: Int = 0
    private var y_xuxvShaderProgram: Int = 0
    private var externalImageShaderProgram: Int = 0
    //shader args:
    //used by shm & egl
    private var projectionArg: Int = 0
    private var transformArg: Int = 0
    private var positionArg: Int = 0
    private var textureCoordinateArg: Int = 0
    private var eglDisplay = EGL_NO_DISPLAY
    private var hasWlEglDisplay = false
    private var init = false

    private var eglOutputState: EglOutputState? = null
    private var newEglOutputState: EglOutputState.Builder? = null

    override fun onDestroy(wlSurfaceResource: WlSurfaceResource) {

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        wlSurface.surface.renderState?.accept(object : SurfaceRenderStateVisitor {
            override fun visit(shmSurfaceState: ShmSurfaceState): SurfaceRenderState? {
                destroy(shmSurfaceState)
                return null
            }

            override fun visit(eglSurfaceState: EglSurfaceState): SurfaceRenderState? {
                destroy(eglSurfaceState)
                return null
            }
        })
    }

    private fun destroy(eglSurfaceState: EglSurfaceState) {

        //delete textures & egl images
        for (texture in eglSurfaceState.textures) {
            this.libGLESv2.glDeleteTextures(1,
                                            Pointer.nref(texture).address)
        }

        for (eglImage in eglSurfaceState.eglImages) {
            this.eglDestroyImageKHR?.`$`(this.eglDisplay,
                                         eglImage)
        }

    }

    private fun destroy(shmSurfaceState: ShmSurfaceState) {

        //delete texture
        this.libGLESv2.glDeleteTextures(1,
                                        Pointer.nref(shmSurfaceState.texture).address)

    }

    override fun queryBuffer(wlBufferResource: WlBufferResource): Buffer {

        val buffer: Buffer

        val shmBuffer = ShmBuffer.get(wlBufferResource)
        if (shmBuffer != null) {
            buffer = SmBuffer.create(shmBuffer.width,
                                     shmBuffer.height,
                                     wlBufferResource,
                                     shmBuffer)
        }
        else if (this.eglQueryWaylandBufferWL != null) {
            val queryWlEglBuffer = this.eglQueryWaylandBufferWL
            val textureFormatP = Pointer.nref(0)
            val bufferPointer = wlBufferResource.pointer

            queryWlEglBuffer?.`$`(this.eglDisplay,
                                  bufferPointer!!,
                                  EGL_TEXTURE_FORMAT,
                                  textureFormatP.address)
            val textureFormat = textureFormatP.dref()

            if (textureFormat != 0) {
                val widthP = Pointer.nref(0)
                val heightP = Pointer.nref(0)
                queryWlEglBuffer?.`$`(this.eglDisplay,
                                      bufferPointer,
                                      EGL_WIDTH,
                                      widthP.address)
                queryWlEglBuffer?.`$`(this.eglDisplay,
                                      bufferPointer,
                                      EGL_HEIGHT,
                                      heightP.address)
                val width = widthP.dref()
                val height = heightP.dref()

                buffer = EglBuffer.create(width,
                                          height,
                                          wlBufferResource,
                                          textureFormat)
            }
            else {
                buffer = UnsupportedBuffer.create(wlBufferResource)
            }
        }
        else {
            //TODO dma buffer.
            buffer = UnsupportedBuffer.create(wlBufferResource)
        }

        return buffer

    }

    override fun eglConfig(eglDisplay: Long,
                           eglExtensions: String): Long {

        assert(eglDisplay != EGL_NO_DISPLAY)

        if (this.libEGL.eglBindAPI(EGL_OPENGL_ES_API).toLong() == 0L) {
            throw RuntimeException("eglBindAPI failed")
        }

        val configs_size = 256 * sizeof(null as Pointer<*>?)
        val configs = malloc(configs_size,
                             Pointer::class.java)
        val num_configs = Pointer.nref(0)
        val egl_config_attribs = Pointer.nref(//@formatter:off
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_RED_SIZE, 1,
                EGL_GREEN_SIZE, 1,
                EGL_BLUE_SIZE, 1,
                EGL_ALPHA_SIZE, 0,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_NONE
                //@formatter:on
                                             )
        if (this.libEGL.eglChooseConfig(eglDisplay,
                                        egl_config_attribs.address,
                                        configs.address,
                                        configs_size,
                                        num_configs.address) == 0) {
            throw RuntimeException("eglChooseConfig() failed")
        }
        if (num_configs.dref() == 0) {
            throw RuntimeException("failed to find suitable EGLConfig")
        }

        bindWlEglDisplay(eglDisplay,
                         eglExtensions)
        this.eglDisplay = eglDisplay

        return configs.dref().address

    }

    private fun bindWlEglDisplay(eglDisplay: Long,
                                 eglExtensions: String) {

        if (bindDisplay(eglDisplay,
                        eglExtensions)) {
            this.eglQueryWaylandBufferWL = wrap(EglQueryWaylandBufferWL::class.java,
                                                this.libEGL.eglGetProcAddress(Pointer.nref("eglQueryWaylandBufferWL").address)).dref()

            //FIXME we need to check this gl extension before we can be 100% sure we support wayland egl.
            this.glEGLImageTargetTexture2DOES = wrap(GlEGLImageTargetTexture2DOES::class.java,
                                                     this.libEGL.eglGetProcAddress(Pointer.nref("glEGLImageTargetTexture2DOES").address)).dref()

            if (eglExtensions.contains("EGL_KHR_image_base")) {
                this.eglCreateImageKHR = wrap(EglCreateImageKHR::class.java,
                                              this.libEGL.eglGetProcAddress(Pointer.nref("eglCreateImageKHR").address)).dref()
                this.eglDestroyImageKHR = wrap(EglDestroyImageKHR::class.java,
                                               this.libEGL.eglGetProcAddress(Pointer.nref("eglDestroyImageKHR").address)).dref()
                this.hasWlEglDisplay = true
            }
            else {
                LOGGER.warning("Extension EGL_KHR_image_base not available. Required for client side egl support.")
            }
        }

    }

    private fun bindDisplay(eglDisplay: Long,
                            extensions: String): Boolean {

        if (extensions.contains("EGL_WL_bind_wayland_display")) {
            val eglBindWaylandDisplayWL = Pointer.wrap<EglBindWaylandDisplayWL>(EglBindWaylandDisplayWL::class.java,
                                                                                this.libEGL.eglGetProcAddress(Pointer.nref("eglBindWaylandDisplayWL").address))
            return eglBindWaylandDisplayWL.dref().`$`(eglDisplay,
                                                      this.display.pointer) != 0
        }
        else {
            LOGGER.warning("Extension EGL_WL_bind_wayland_display not available. Required for client side egl support.")
            return false
        }

    }

    /**
     * Prepare the renderer for drawing.
     *
     *
     * This makes sure all subsequent calls to [.drawView] have the correct transformations set up for the given output.

     * @param eglOutput
     * *
     * @param wlOutput
     */
    fun prepareDraw(eglOutput: EglOutput,
                    wlOutput: WlOutput) {

        this.libEGL.eglMakeCurrent(this.eglDisplay,
                                   eglOutput.eglSurface,
                                   eglOutput.eglSurface,
                                   eglOutput.eglContext)

        if (!this.init) {
            //one time init because we need a current context
            assert(eglOutput.eglContext != EGL_NO_CONTEXT)
            initRenderer()
        }

        setupEglOutputState(eglOutput,
                            wlOutput)

        //TODO comment out these 2 calls when we have a shell that provides a solid background.
        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f)
        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT)

    }

    /**
     * Flush all pending drawing commands and signal any listeners that drawing has finish for the given output.

     * @param eglOutput
     */
    fun finishDraw(eglOutput: EglOutput) {
        flushRenderState(eglOutput)
    }

    private fun initRenderer() {
        //check for required texture glExtensions
        val glExtensions = wrap<String>(String::class.java,
                                        this.libGLESv2.glGetString(LibGLESv2.GL_EXTENSIONS)).dref()

        //init shm shaders
        LOGGER.info("GLESv2 glExtensions: " + glExtensions)
        if (!glExtensions.contains("GL_EXT_texture_format_BGRA8888")) {
            LOGGER.severe("Required extension GL_EXT_texture_format_BGRA8888 not available")
            System.exit(1)
        }
        //this shader is reused in wl egl
        this.argb8888ShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         Gles2Shaders.FRAGMENT_SHADER_ARGB8888,
                                                         1)
        this.xrgb8888ShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         Gles2Shaders.FRAGMENT_SHADER_XRGB8888,
                                                         1)

        //compile wl egl shaders
        if (this.hasWlEglDisplay) {
            this.y_u_vShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                          FRAGMENT_SHADER_EGL_Y_U_V,
                                                          3)
            this.y_uvShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_EGL_Y_UV,
                                                         2)
            this.y_xuxvShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                           FRAGMENT_SHADER_EGL_Y_XUXV,
                                                           2)

            if (glExtensions.contains("GL_OES_EGL_image_external")) {
                this.externalImageShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                                      FRAGMENT_SHADER_EGL_EXTERNAL,
                                                                      1)
            }
            else {
                LOGGER.warning("Extension GL_OES_EGL_image_external not available.")
            }
        }

        //configure texture blending
        this.libGLESv2.glBlendFunc(LibGLESv2.GL_ONE,
                                   LibGLESv2.GL_ONE_MINUS_SRC_ALPHA)
        this.init = true
    }

    private fun setupEglOutputState(eglOutput: EglOutput,
                                    wlOutput: WlOutput) {
        //to be used state
        val eglOutputState = eglOutput.state ?: initOutputRenderState(eglOutput,
                                                                      wlOutput)
        //updates to state are registered with the builder
        this.newEglOutputState = eglOutputState.toBuilder()
        this.eglOutputState = eglOutputState
    }

    private fun flushRenderState(eglOutput: EglOutput) {
        eglOutput.updateState(this.newEglOutputState!!.build())
        this.libEGL.eglSwapBuffers(this.eglDisplay,
                                   eglOutput.eglSurface)
    }

    private fun createShaderProgram(vertexShaderSource: String,
                                    fragmentShaderSource: String,
                                    nroTextures: Int): Int {
        val vertexShader = compileShader(vertexShaderSource,
                                         LibGLESv2.GL_VERTEX_SHADER)
        val fragmentShader = compileShader(fragmentShaderSource,
                                           LibGLESv2.GL_FRAGMENT_SHADER)

        //shader program
        val shaderProgram = this.libGLESv2.glCreateProgram()
        this.libGLESv2.glAttachShader(shaderProgram,
                                      vertexShader)

        this.libGLESv2.glAttachShader(shaderProgram,
                                      fragmentShader)

        this.libGLESv2.glLinkProgram(shaderProgram)

        //check the link status
        val linked = Pointer.nref(0)
        this.libGLESv2.glGetProgramiv(shaderProgram,
                                      LibGLESv2.GL_LINK_STATUS,
                                      linked.address)
        if (linked.dref() == 0) {
            val infoLen = Pointer.nref(0)
            this.libGLESv2.glGetProgramiv(shaderProgram,
                                          LibGLESv2.GL_INFO_LOG_LENGTH,
                                          infoLen.address)
            var logSize = infoLen.dref()
            if (logSize <= 0) {
                //some drivers report incorrect log size
                logSize = 1024
            }
            val log = Pointer.nref(String(CharArray(logSize)))
            this.libGLESv2.glGetProgramInfoLog(shaderProgram,
                                               logSize,
                                               0L,
                                               log.address)
            this.libGLESv2.glDeleteProgram(shaderProgram)
            System.err.println("Error compiling the vertex shader: " + log.dref())
            System.exit(1)
        }

        //find shader arguments
        this.projectionArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                 Pointer.nref("u_projection").address)
        this.transformArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                Pointer.nref("u_transform").address)
        this.positionArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                              Pointer.nref("a_position").address)
        this.textureCoordinateArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                                       Pointer.nref("a_texCoord").address)

        for (i in 0..nroTextures - 1) {
            this.textureArgs[i] = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                      Pointer.nref("u_texture" + i).address)
        }

        return shaderProgram
    }

    private fun initOutputRenderState(eglOutput: EglOutput,
                                      wlOutput: WlOutput): EglOutputState {
        val builder = EglOutputState.builder()
        val output = wlOutput.output
        updateTransform(builder,
                        output)

        val eglOutputState = builder.build()
        eglOutput.updateState(eglOutputState)

        //listen for external updates
        output.transformSignal.connect {
            handleOutputUpdate(eglOutput,
                               wlOutput)
        }
        output.modeSignal.connect {
            handleOutputUpdate(eglOutput,
                               wlOutput)
        }

        return eglOutputState
    }

    private fun compileShader(shaderSource: String,
                              shaderType: Int): Int {
        val shader = this.libGLESv2.glCreateShader(shaderType)
        val shaders = Pointer.nref(Pointer.nref(shaderSource))
        this.libGLESv2.glShaderSource(shader,
                                      1,
                                      shaders.address,
                                      0L)
        this.libGLESv2.glCompileShader(shader)

        checkShaderCompilation(shader)
        return shader
    }

    private fun updateTransform(eglOutputStateBuilder: EglOutputState.Builder,
                                output: Output) {
        val mode = output.mode
        val width = mode.width
        val height = mode.height

        //first time render for this output, clear it.
        this.libGLESv2.glViewport(0,
                                  0,
                                  width,
                                  height)
        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f)
        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT)

        eglOutputStateBuilder.glTransform(createGlTransform(output))
    }

    private fun handleOutputUpdate(eglOutput: EglOutput,
                                   wlOutput: WlOutput) {
        eglOutput.state?.let {
            val stateBuilder = it.toBuilder()
            updateTransform(stateBuilder,
                            wlOutput.output)
            eglOutput.updateState(stateBuilder.build())
            //schedule new render
            eglOutput.render(wlOutput)
        }
    }

    private fun checkShaderCompilation(shader: Int) {
        val vstatus = Pointer.nref(0)
        this.libGLESv2.glGetShaderiv(shader,
                                     LibGLESv2.GL_COMPILE_STATUS,
                                     vstatus.address)
        if (vstatus.dref() == 0) {
            //failure!
            //get log length
            val logLength = Pointer.nref(0)
            this.libGLESv2.glGetShaderiv(shader,
                                         LibGLESv2.GL_INFO_LOG_LENGTH,
                                         logLength.address)
            //get log
            var logSize = logLength.dref()
            if (logSize == 0) {
                //some drivers report incorrect log size
                logSize = 1024
            }
            val log = Pointer.nref(String(CharArray(logSize)))
            this.libGLESv2.glGetShaderInfoLog(shader,
                                              logSize,
                                              0L,
                                              log.address)
            System.err.println("Error compiling the vertex shader: " + log.dref())
            System.exit(1)
        }
    }

    private fun createGlTransform(output: Output): Mat4 {
        val mode = output.mode

        val width = mode.width
        val height = mode.height

        //@formatter:off
        return Mat4.create(2.0f / width, 0f,             0f, -1f,
                           0f,           2.0f / -height, 0f, 1f,
                           0f,           0f,             1f, 0f,
                           0f,           0f,             0f, 1f).multiply(output.inverseTransform)
        //@formatter:on
    }

    fun drawView(surfaceView: SurfaceView) {
        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        wlSurface.surface.state.buffer?.let {
            drawView(surfaceView,
                     it)
        }
    }

    private fun drawView(surfaceView: SurfaceView,
                         wlBufferResource: WlBufferResource) {
        queryBuffer(wlBufferResource).accept(object : BufferVisitor {
            override fun visit(buffer: Buffer) {
                LOGGER.warning("Unsupported buffer.")
            }

            override fun visit(eglBuffer: EglBuffer) {
                drawEgl(surfaceView,
                        eglBuffer)
            }

            override fun visit(smBuffer: SmBuffer) {
                drawShm(surfaceView,
                        smBuffer)
            }
        })
    }

    private fun drawShm(surfaceView: SurfaceView,
                        smBuffer: SmBuffer) {
        queryShmSurfaceRenderState(surfaceView,
                                   smBuffer.shmBuffer)?.accept(object : SurfaceRenderStateVisitor {
            override fun visit(shmSurfaceState: ShmSurfaceState): SurfaceRenderState? {
                drawShm(surfaceView,
                        shmSurfaceState)
                return null
            }
        })
    }

    private fun queryShmSurfaceRenderState(surfaceView: SurfaceView,
                                           shmBuffer: ShmBuffer): SurfaceRenderState? {
        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val renderStateOptional = surface.renderState?.accept(object : SurfaceRenderStateVisitor {
            override fun visit(shmSurfaceState: ShmSurfaceState): SurfaceRenderState? {
                //the surface already has an shm render state associated. update it.
                return createShmSurfaceRenderState(surfaceView,
                                                   shmBuffer,
                                                   shmSurfaceState)
            }

            override fun visit(eglSurfaceState: EglSurfaceState): SurfaceRenderState? {
                //the surface was previously associated with an egl render state but is now using an shm render state. create it.
                destroy(eglSurfaceState)
                //TODO we could reuse the texture id from the egl surface render state
                return createShmSurfaceRenderState(surfaceView,
                                                   shmBuffer,
                                                   oldRenderState = null)
            }
        }) ?: createShmSurfaceRenderState(surfaceView,
                                          shmBuffer,
                                          oldRenderState = null)

        if (renderStateOptional != null) {
            surface.renderState = renderStateOptional
        }
        else {
            onDestroy(surfaceView.wlSurfaceResource)
        }

        return renderStateOptional
    }

    private fun createShmSurfaceRenderState(surfaceView: SurfaceView,
                                            shmBuffer: ShmBuffer,
                                            oldRenderState: ShmSurfaceState?): SurfaceRenderState? {
        //new values
        val pitch: Int
        val height = shmBuffer.height
        val target = LibGLESv2.GL_TEXTURE_2D
        val shaderProgram: Int
        val glFormat: Int
        val glPixelType: Int
        val texture: Int

        val shmBufferFormat = shmBuffer.format
        val argb8888 = WlShmFormat.ARGB8888.value
        val xrgb8888 = WlShmFormat.XRGB8888.value

        if (argb8888 == shmBufferFormat) {
            shaderProgram = this.argb8888ShaderProgram
            pitch = shmBuffer.stride / 4
            glFormat = LibGLESv2.GL_BGRA_EXT
            glPixelType = LibGLESv2.GL_UNSIGNED_BYTE
        }
        else if (xrgb8888 == shmBufferFormat) {
            shaderProgram = this.xrgb8888ShaderProgram
            pitch = shmBuffer.stride / 4
            glFormat = LibGLESv2.GL_BGRA_EXT
            glPixelType = LibGLESv2.GL_UNSIGNED_BYTE
        }
        else {
            LOGGER.warning(String.format("Unknown shm buffer format: %d",
                                         shmBufferFormat))
            return null
        }

        val newShmSurfaceState: ShmSurfaceState


        if (oldRenderState != null) {
            val oldShmSurfaceState = oldRenderState
            texture = oldShmSurfaceState.texture

            newShmSurfaceState = ShmSurfaceState.create(pitch,
                                                        height,
                                                        target,
                                                        shaderProgram,
                                                        glFormat,
                                                        glPixelType,
                                                        texture)

            if (pitch != oldShmSurfaceState.pitch || height != oldShmSurfaceState.height || glFormat != oldShmSurfaceState.glFormat || glPixelType != oldShmSurfaceState.glPixelType) {
                //state needs full texture updating
                shmUpdateAll(surfaceView,
                             shmBuffer,
                             newShmSurfaceState)
            }
            else {
                //partial texture update
                shmUpdateDamaged(surfaceView,
                                 shmBuffer,
                                 newShmSurfaceState)
            }
        }
        else {
            //allocate new texture id & upload full texture
            texture = genTexture(target)
            newShmSurfaceState = ShmSurfaceState.create(pitch,
                                                        height,
                                                        target,
                                                        shaderProgram,
                                                        glFormat,
                                                        glPixelType,
                                                        texture)
            shmUpdateAll(surfaceView,
                         shmBuffer,
                         newShmSurfaceState)
        }

        return newShmSurfaceState
    }

    private fun shmUpdateDamaged(wlSurfaceResource: SurfaceView,
                                 shmBuffer: ShmBuffer,
                                 newShmSurfaceState: ShmSurfaceState) {
        //TODO implement damage
        shmUpdateAll(wlSurfaceResource,
                     shmBuffer,
                     newShmSurfaceState)
    }

    private fun shmUpdateAll(surfaceView: SurfaceView,
                             shmBuffer: ShmBuffer,
                             newShmSurfaceState: ShmSurfaceState) {
        this.libGLESv2.glBindTexture(newShmSurfaceState.target,
                                     newShmSurfaceState.texture)
        shmBuffer.beginAccess()
        this.libGLESv2.glTexImage2D(newShmSurfaceState.target,
                                    0,
                                    newShmSurfaceState.glFormat,
                                    newShmSurfaceState.pitch,
                                    newShmSurfaceState.height,
                                    0,
                                    newShmSurfaceState.glFormat,
                                    newShmSurfaceState.glPixelType,
                                    JNI.unwrap(shmBuffer.data))
        shmBuffer.endAccess()
        this.libGLESv2.glBindTexture(newShmSurfaceState.target,
                                     0)

        //FIXME firing the paint callback here is actually wrong since we might still need to draw on a different output. Only when all views of a surface are processed, we can call the fire paint callback.
        //TODO Introduce the concept of views => output <-- view (=many2many) --> surface
        //FIXME we should only fire the callback once all views are rendered
        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        wlSurface.surface.firePaintCallbacks(NANOSECONDS.toMillis(System.nanoTime()).toInt())

    }

    private fun drawShm(surfaceView: SurfaceView,
                        shmSurfaceState: ShmSurfaceState) {

        val shaderProgram = shmSurfaceState.shaderProgram

        //activate & setup shader
        this.libGLESv2.glUseProgram(shaderProgram)
        setupVertexParams(surfaceView,
                          shmSurfaceState.pitch.toFloat(),
                          shmSurfaceState.height.toFloat())

        //set the buffer in the shader
        this.libGLESv2.glActiveTexture(LibGLESv2.GL_TEXTURE0)
        this.libGLESv2.glBindTexture(shmSurfaceState.target,
                                     shmSurfaceState.texture)
        this.libGLESv2.glUniform1i(this.textureArgs[0],
                                   0)

        //draw
        //enable texture blending
        this.libGLESv2.glEnable(LibGLESv2.GL_BLEND)
        this.libGLESv2.glDrawArrays(LibGLESv2.GL_TRIANGLES,
                                    0,
                                    6)

        //cleanup
        this.libGLESv2.glDisable(LibGLESv2.GL_BLEND)
        this.libGLESv2.glDisableVertexAttribArray(this.positionArg)
        this.libGLESv2.glDisableVertexAttribArray(this.textureArgs[0])
        this.libGLESv2.glUseProgram(0)
    }

    private fun queryEglSurfaceRenderState(surfaceView: SurfaceView,
                                           eglBuffer: EglBuffer): SurfaceRenderState? {

        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val renderStateOptional = surface.renderState?.accept(object : SurfaceRenderStateVisitor {
            override fun visit(shmSurfaceState: ShmSurfaceState): SurfaceRenderState? {
                //the surface was previously associated with an shm render state but is now using an egl render state. create it.
                destroy(shmSurfaceState)
                return createEglSurfaceRenderState(eglBuffer,
                                                   oldRenderState = null)
            }

            override fun visit(eglSurfaceState: EglSurfaceState): SurfaceRenderState? {
                //TODO we could reuse the texture id
                //the surface already has an egl render state associated. update it.
                return createEglSurfaceRenderState(eglBuffer,
                                                   eglSurfaceState)
            }
        }) ?: createEglSurfaceRenderState(eglBuffer,
                                          oldRenderState = null)

        if (renderStateOptional != null) {
            surface.renderState = renderStateOptional
        }
        else {
            onDestroy(surfaceView.wlSurfaceResource)
        }

        return renderStateOptional
    }

    private fun createEglSurfaceRenderState(eglBuffer: EglBuffer,
                                            oldRenderState: EglSurfaceState?): SurfaceRenderState? {

        //surface egl render states:
        val pitch = eglBuffer.width
        val height = eglBuffer.height
        val yInverted: Boolean
        val shaderProgram: Int
        val target: Int
        val textures: IntArray
        val eglImages: LongArray

        //gather render states:
        val buffer = eglBuffer.wlBufferResource.pointer

        val queryWaylandBuffer = this.eglQueryWaylandBufferWL

        val yInvertedP = Pointer.nref(0)

        yInverted = queryWaylandBuffer?.`$`(this.eglDisplay,
                                            buffer,
                                            EGL_WAYLAND_Y_INVERTED_WL,
                                            yInvertedP.address) == 0 || yInvertedP.dref() != 0

        when (eglBuffer.textureFormat) {
            EGL_TEXTURE_RGB, EGL_TEXTURE_RGBA -> {
                textures = IntArray(1)
                eglImages = LongArray(1)
                target = LibGLESv2.GL_TEXTURE_2D
                shaderProgram = this.argb8888ShaderProgram
            }
            EGL_TEXTURE_EXTERNAL_WL           -> {
                textures = IntArray(1)
                eglImages = LongArray(1)
                target = LibGLESv2.GL_TEXTURE_EXTERNAL_OES
                shaderProgram = this.externalImageShaderProgram
            }
            EGL_TEXTURE_Y_UV_WL               -> {
                textures = IntArray(2)
                eglImages = LongArray(2)
                target = LibGLESv2.GL_TEXTURE_2D
                shaderProgram = this.y_uvShaderProgram
            }
            EGL_TEXTURE_Y_U_V_WL              -> {
                textures = IntArray(3)
                eglImages = LongArray(3)
                target = LibGLESv2.GL_TEXTURE_2D
                shaderProgram = this.y_u_vShaderProgram
            }
            EGL_TEXTURE_Y_XUXV_WL             -> {
                textures = IntArray(2)
                eglImages = LongArray(2)
                target = LibGLESv2.GL_TEXTURE_2D
                shaderProgram = this.y_xuxvShaderProgram
            }
            else                              -> {
                throw UnsupportedOperationException("Texture format ${eglBuffer.textureFormat} is not supported.")
            }
        }

        //delete old egl images
        oldRenderState?.let {
            for (oldEglImage in it.eglImages) {
                this.eglDestroyImageKHR?.`$`(this.eglDisplay,
                                             oldEglImage)
            }
        }

        //create egl images
        val attribs = IntArray(3)

        for (i in eglImages.indices) {
            attribs[0] = EGL_WAYLAND_PLANE_WL
            attribs[1] = i
            attribs[2] = EGL_NONE

            val eglImage = this.eglCreateImageKHR?.`$`(this.eglDisplay,
                                                       EGL_NO_CONTEXT,
                                                       EGL_WAYLAND_BUFFER_WL,
                                                       buffer,
                                                       Pointer.nref(*attribs).address) ?: 0L
            if (eglImage == EGL_NO_IMAGE_KHR) {
                return null
            }
            else {
                eglImages[i] = eglImage
            }

            //make sure we have valid texture ids
            oldRenderState?.let {

                val oldTextures = it.textures
                val deltaNewTextures = textures.size - oldTextures.size
                val needNewTextures = deltaNewTextures > 0

                //reuse old texture ids
                System.arraycopy(oldTextures,
                                 0,
                                 textures,
                                 0,
                                 if (needNewTextures) oldTextures.size else textures.size)

                if (needNewTextures) {
                    //generate missing texture ids
                    for (j in textures.size - 1 downTo textures.size - deltaNewTextures) {
                        textures[j] = genTexture(target)
                    }
                }
                else if (deltaNewTextures < 0) {
                    //cleanup old unused texture ids
                    for (j in oldTextures.size - 1 downTo oldTextures.size + deltaNewTextures) {
                        this.libGLESv2.glDeleteTextures(1,
                                                        Pointer.nref(oldTextures[j]).address)
                    }
                }
            }

            this.libGLESv2.glActiveTexture(LibGLESv2.GL_TEXTURE0 + i)
            this.libGLESv2.glBindTexture(target,
                                         textures[i])
            this.glEGLImageTargetTexture2DOES?.`$`(target,
                                                   eglImage)
        }

        return EglSurfaceState.create(pitch,
                                      height,
                                      target,
                                      shaderProgram,
                                      yInverted,
                                      textures,
                                      eglImages)

    }

    private fun drawEgl(surfaceView: SurfaceView,
                        eglBuffer: EglBuffer) {

        queryEglSurfaceRenderState(surfaceView,
                                   eglBuffer)?.accept(object : SurfaceRenderStateVisitor {
            override fun visit(eglSurfaceState: EglSurfaceState): SurfaceRenderState? {
                drawEgl(surfaceView,
                        eglSurfaceState)
                return null
            }
        })
    }

    private fun drawEgl(surfaceView: SurfaceView,
                        eglSurfaceState: EglSurfaceState) {

        //TODO unify with drawShm
        val shaderProgram = eglSurfaceState.shaderProgram

        //activate & setup shader
        this.libGLESv2.glUseProgram(shaderProgram)
        setupVertexParams(surfaceView,
                          eglSurfaceState.pitch.toFloat(),
                          eglSurfaceState.height.toFloat())

        //set the buffer in the shader
        val textures = eglSurfaceState.textures
        run {
            var i = 0
            val texturesLength = textures.size
            while (i < texturesLength) {
                val texture = textures[i]
                val target = eglSurfaceState.target

                this.libGLESv2.glActiveTexture(LibGLESv2.GL_TEXTURE0 + i)
                this.libGLESv2.glBindTexture(target,
                                             texture)
                this.libGLESv2.glTexParameteri(target,
                                               LibGLESv2.GL_TEXTURE_MIN_FILTER,
                                               LibGLESv2.GL_NEAREST)
                this.libGLESv2.glTexParameteri(target,
                                               LibGLESv2.GL_TEXTURE_MAG_FILTER,
                                               LibGLESv2.GL_NEAREST)
                this.libGLESv2.glUniform1i(this.textureArgs[i],
                                           0)
                i++
            }
        }

        //draw
        //enable texture blending
        this.libGLESv2.glEnable(LibGLESv2.GL_BLEND)
        this.libGLESv2.glDrawArrays(LibGLESv2.GL_TRIANGLES,
                                    0,
                                    6)

        //cleanup
        this.libGLESv2.glDisable(LibGLESv2.GL_BLEND)
        this.libGLESv2.glDisableVertexAttribArray(this.positionArg)
        var i = 0
        val texturesLength = textures.size
        while (i < texturesLength) {
            this.libGLESv2.glDisableVertexAttribArray(this.textureArgs[i])
            i++
        }
        this.libGLESv2.glUseProgram(0)

        //FIXME firing the paint callback here is actually wrong since we might still need to draw on a different output. Only when all views of a surface are processed, we can call the fire paint callback.
        //FIXME we should only fire the callback once all views are rendered
        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        wlSurface.surface.firePaintCallbacks(NANOSECONDS.toMillis(System.nanoTime()).toInt())
    }

    private fun genTexture(target: Int): Int {

        val texture = Pointer.nref(0)
        this.libGLESv2.glGenTextures(1,
                                     texture.address)
        val textureId = texture.dref()
        this.libGLESv2.glBindTexture(target,
                                     textureId)
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_WRAP_S,
                                       LibGLESv2.GL_CLAMP_TO_EDGE)
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_WRAP_T,
                                       LibGLESv2.GL_CLAMP_TO_EDGE)
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_MIN_FILTER,
                                       LibGLESv2.GL_NEAREST)
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_MAG_FILTER,
                                       LibGLESv2.GL_NEAREST)
        this.libGLESv2.glBindTexture(target,
                                     0)
        return textureId

    }

    private fun setupVertexParams(surfaceView: SurfaceView,
                                  bufferWidth: Float,
                                  bufferHeight: Float) {

        val wlSurface = surfaceView.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val transform = surfaceView.positionTransform.multiply(surface.transform).toArray()

        //define vertex data
        val vertexData = vertexData(bufferWidth,
                                    bufferHeight)

        //upload uniform vertex data
        val projectionBuffer = Pointer.nref(*this.eglOutputState!!.glTransform.toArray())
        this.libGLESv2.glUniformMatrix4fv(this.projectionArg,
                                          1,
                                          0,
                                          projectionBuffer.address)

        val transformBuffer = Pointer.nref(*transform)
        this.libGLESv2.glUniformMatrix4fv(this.transformArg,
                                          1,
                                          0,
                                          transformBuffer.address)
        //set vertex data in shader
        this.libGLESv2.glEnableVertexAttribArray(this.positionArg)
        this.libGLESv2.glVertexAttribPointer(this.positionArg,
                                             2,
                                             LibGLESv2.GL_FLOAT,
                                             0,
                                             4 * java.lang.Float.BYTES,
                                             vertexData.address)

        this.libGLESv2.glEnableVertexAttribArray(this.textureCoordinateArg)
        this.libGLESv2.glVertexAttribPointer(this.textureCoordinateArg,
                                             2,
                                             LibGLESv2.GL_FLOAT,
                                             0,
                                             4 * java.lang.Float.BYTES,
                                             vertexData.offset(2).address)

    }

    private fun vertexData(bufferWidth: Float,
                           bufferHeight: Float): Pointer<Float> {
        //first pair => attribute vec2 a_position
        //second pair => attribute vec2 a_texCoord
        return Pointer.nref(//top left:
                0f,
                0f,
                0f,
                0f, //top right:
                bufferWidth,
                0f,
                1f,
                0f, //bottom right:
                bufferWidth,
                bufferHeight,
                1f,
                1f, //bottom right:
                bufferWidth,
                bufferHeight,
                1f,
                1f, //bottom left:
                0f,
                bufferHeight,
                0f,
                1f, //top left:
                0f,
                0f,
                0f,
                0f)
    }

    companion object {
        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }
}
