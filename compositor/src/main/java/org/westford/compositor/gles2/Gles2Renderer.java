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
package org.westford.compositor.gles2;

import org.freedesktop.jaccall.JNI;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.westford.compositor.core.Buffer;
import org.westford.compositor.core.BufferVisitor;
import org.westford.compositor.core.EglBuffer;
import org.westford.compositor.core.EglOutput;
import org.westford.compositor.core.EglOutputState;
import org.westford.compositor.core.EglSurfaceState;
import org.westford.compositor.core.GlRenderer;
import org.westford.compositor.core.Output;
import org.westford.compositor.core.OutputMode;
import org.westford.compositor.core.RenderOutput;
import org.westford.compositor.core.Scene;
import org.westford.compositor.core.ShmSurfaceState;
import org.westford.compositor.core.SmBuffer;
import org.westford.compositor.core.Surface;
import org.westford.compositor.core.SurfaceRenderState;
import org.westford.compositor.core.SurfaceRenderStateVisitor;
import org.westford.compositor.core.UnsupportedBuffer;
import org.westford.compositor.core.calc.Mat4;
import org.westford.compositor.drm.egl.DrmEglOutput;
import org.westford.compositor.protocol.WlSurface;
import org.westford.compositor.x11.egl.X11EglOutput;
import org.westford.nativ.libEGL.EglBindWaylandDisplayWL;
import org.westford.nativ.libEGL.EglCreateImageKHR;
import org.westford.nativ.libEGL.EglDestroyImageKHR;
import org.westford.nativ.libEGL.EglQueryWaylandBufferWL;
import org.westford.nativ.libEGL.LibEGL;
import org.westford.nativ.libGLESv2.GlEGLImageTargetTexture2DOES;
import org.westford.nativ.libGLESv2.LibGLESv2;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Pointer.wrap;
import static org.freedesktop.jaccall.Size.sizeof;
import static org.westford.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westford.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westford.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westford.nativ.libEGL.LibEGL.EGL_HEIGHT;
import static org.westford.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_IMAGE_KHR;
import static org.westford.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westford.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westford.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westford.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westford.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_EXTERNAL_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_FORMAT;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_RGB;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_RGBA;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_Y_UV_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_Y_U_V_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_TEXTURE_Y_XUXV_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_WAYLAND_BUFFER_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_WAYLAND_PLANE_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_WAYLAND_Y_INVERTED_WL;
import static org.westford.nativ.libEGL.LibEGL.EGL_WIDTH;
import static org.westford.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;

@Singleton
public class Gles2Renderer implements GlRenderer {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String VERTEX_SHADER =
            "uniform mat4 u_projection;\n" +
            "uniform mat4 u_transform;\n" +
            "attribute vec2 a_position;\n" +
            "attribute vec2 a_texCoord;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "    v_texCoord = a_texCoord;\n" +
            "    gl_Position = u_projection * u_transform * vec4(a_position, 0.0, 1.0) ;\n" +
            "}";

    private static final String FRAGMENT_SHADER_ARGB8888 =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture0;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(u_texture0, v_texCoord);\n" +
            "}";

    private static final String FRAGMENT_SHADER_XRGB8888 =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture0;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "    gl_FragColor = vec4(texture2D(u_texture0, v_texCoord).bgr, 1.0);\n" +
            "}";

    private static final String FRAGMENT_SHADER_EGL_EXTERNAL =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES u_texture0;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "   gl_FragColor = texture2D(u_texture0, v_texCoord)\n;" +
            "}";

    private static final String FRAGMENT_CONVERT_YUV =
            "  gl_FragColor.r = y + 1.59602678 * v;\n" +
            "  gl_FragColor.g = y - 0.39176229 * u - 0.81296764 * v;\n" +
            "  gl_FragColor.b = y + 2.01723214 * u;\n" +
            "  gl_FragColor.a = 1.0;\n" +
            "}";

    private static final String FRAGMENT_SHADER_EGL_Y_UV =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture0;\n" +
            "uniform sampler2D u_texture1;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main() {\n" +
            "  float y = 1.16438356 * (texture2D(u_texture0, v_texCoord).x - 0.0625);\n" +
            "  float u = texture2D(u_texture1, v_texCoord).r - 0.5;\n" +
            "  float v = texture2D(u_texture1, v_texCoord).g - 0.5;\n" +
            FRAGMENT_CONVERT_YUV;

    private static final String FRAGMENT_SHADER_EGL_Y_U_V =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture0;\n" +
            "uniform sampler2D u_texture1;\n" +
            "uniform sampler2D u_texture2;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main() {\n" +
            "  float y = 1.16438356 * (texture2D(u_texture0, v_texCoord).x - 0.0625);\n" +
            "  float u = texture2D(u_texture1, v_texCoord).x - 0.5;\n" +
            "  float v = texture2D(u_texture2, v_texCoord).x - 0.5;\n" +
            FRAGMENT_CONVERT_YUV;

    private static final String FRAGMENT_SHADER_EGL_Y_XUXV =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture0;\n" +
            "uniform sampler2D u_texture1;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main() {\n" +
            "  float y = 1.16438356 * (texture2D(u_texture0, v_texCoord).x - 0.0625);\n" +
            "  float u = texture2D(u_texture1, v_texCoord).g - 0.5;\n" +
            "  float v = texture2D(u_texture1, v_texCoord).a - 0.5;\n" +
            FRAGMENT_CONVERT_YUV;

    @Nonnull
    private final LibEGL    libEGL;
    @Nonnull
    private final LibGLESv2 libGLESv2;
    @Nonnull
    private final Display   display;
    @Nonnull
    private final Scene     scene;
    private final int[]                                  textureArgs                  = new int[3];
    @Nonnull
    private       Optional<EglQueryWaylandBufferWL>      eglQueryWaylandBufferWL      = Optional.empty();
    @Nonnull
    private       Optional<EglCreateImageKHR>            eglCreateImageKHR            = Optional.empty();
    @Nonnull
    private       Optional<EglDestroyImageKHR>           eglDestroyImageKHR           = Optional.empty();
    @Nonnull
    private       Optional<GlEGLImageTargetTexture2DOES> glEGLImageTargetTexture2DOES = Optional.empty();
    //shader programs
    //used by shm & egl
    private int argb8888ShaderProgram;
    //used by shm
    private int xrgb8888ShaderProgram;
    //used by egl
    private int y_u_vShaderProgram;
    private int y_uvShaderProgram;
    private int y_xuxvShaderProgram;
    private int externalImageShaderProgram;
    //shader args:
    //used by shm & egl
    private int projectionArg;
    private int transformArg;
    private int positionArg;
    private int textureCoordinateArg;
    private long    eglDisplay      = EGL_NO_DISPLAY;
    private boolean hasWlEglDisplay = false;
    private boolean init            = false;


    private EglOutputState         eglOutputState;
    private EglOutputState.Builder newEglOutputState;

    //TODO guarantee 1 renderer instance per platform
    @Inject
    Gles2Renderer(@Nonnull final LibEGL libEGL,
                  @Nonnull final LibGLESv2 libGLESv2,
                  @Nonnull final Display display,
                  @Nonnull final Scene scene) {
        this.libEGL = libEGL;
        this.libGLESv2 = libGLESv2;
        this.display = display;
        this.scene = scene;
    }

    @Override
    public void onDestroy(@Nonnull final WlSurfaceResource wlSurfaceResource) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        wlSurface.getSurface()
                 .getRenderState()
                 .ifPresent(surfaceRenderState -> surfaceRenderState.accept(new SurfaceRenderStateVisitor() {
                     @Override
                     public Optional<SurfaceRenderState> visit(final ShmSurfaceState shmSurfaceState) {
                         destroy(shmSurfaceState);
                         return Optional.empty();
                     }

                     @Override
                     public Optional<SurfaceRenderState> visit(final EglSurfaceState eglSurfaceState) {
                         destroy(eglSurfaceState);
                         return Optional.empty();
                     }
                 }));
    }

    private void destroy(final EglSurfaceState eglSurfaceState) {
        //delete textures & egl images
        for (final int texture : eglSurfaceState.getTextures()) {
            this.libGLESv2.glDeleteTextures(1,
                                            Pointer.nref(texture).address);
        }

        for (final long eglImage : eglSurfaceState.getEglImages()) {
            this.eglDestroyImageKHR.ifPresent(eglDestroyImage -> eglDestroyImage.$(this.eglDisplay,
                                                                                   eglImage));
        }
    }

    private void destroy(final ShmSurfaceState shmSurfaceState) {
        //delete texture
        this.libGLESv2.glDeleteTextures(1,
                                        Pointer.nref(shmSurfaceState.getTexture()).address);
    }

    @Nonnull
    @Override
    public Buffer queryBuffer(@Nonnull final WlBufferResource wlBufferResource) {

        final Buffer buffer;

        final ShmBuffer shmBuffer = ShmBuffer.get(wlBufferResource);
        if (shmBuffer != null) {
            buffer = SmBuffer.create(shmBuffer.getWidth(),
                                     shmBuffer.getHeight(),
                                     wlBufferResource,
                                     shmBuffer);
        }
        else if (this.eglQueryWaylandBufferWL.isPresent()) {
            final EglQueryWaylandBufferWL queryWlEglBuffer = this.eglQueryWaylandBufferWL.get();
            final Pointer<Integer>        textureFormatP   = Pointer.nref(0);
            final Long                    bufferPointer    = wlBufferResource.pointer;

            queryWlEglBuffer.$(this.eglDisplay,
                               bufferPointer,
                               EGL_TEXTURE_FORMAT,
                               textureFormatP.address);
            final int textureFormat = textureFormatP.dref();

            if (textureFormat != 0) {
                final Pointer<Integer> widthP  = Pointer.nref(0);
                final Pointer<Integer> heightP = Pointer.nref(0);
                queryWlEglBuffer.$(this.eglDisplay,
                                   bufferPointer,
                                   EGL_WIDTH,
                                   widthP.address);
                queryWlEglBuffer.$(this.eglDisplay,
                                   bufferPointer,
                                   EGL_HEIGHT,
                                   heightP.address);
                final int width  = widthP.dref();
                final int height = heightP.dref();

                buffer = EglBuffer.create(width,
                                          height,
                                          wlBufferResource,
                                          textureFormat);
            }
            else {
                buffer = UnsupportedBuffer.create(wlBufferResource);
            }
        }
        else //TODO dma buffer.
        {
            buffer = UnsupportedBuffer.create(wlBufferResource);
        }

        return buffer;
    }

    @Override
    public long eglConfig(final long eglDisplay,
                          @Nonnull final String eglExtensions) {
        assert (eglDisplay != EGL_NO_DISPLAY);

        if (this.libEGL.eglBindAPI(EGL_OPENGL_ES_API) == 0L) {
            throw new RuntimeException("eglBindAPI failed");
        }

        final int configs_size = 256 * sizeof((Pointer<?>) null);
        final Pointer<Pointer> configs = malloc(configs_size,
                                                Pointer.class);
        final Pointer<Integer> num_configs = Pointer.nref(0);
        final Pointer<Integer> egl_config_attribs = Pointer.nref(
                //@formatter:off
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
	            EGL_RED_SIZE, 1,
	            EGL_GREEN_SIZE, 1,
	            EGL_BLUE_SIZE, 1,
	            EGL_ALPHA_SIZE, 0,
	            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
	            EGL_NONE
                //@formatter:on
                                                                );
        if (this.libEGL.eglChooseConfig(eglDisplay,
                                        egl_config_attribs.address,
                                        configs.address,
                                        configs_size,
                                        num_configs.address) == 0) {
            throw new RuntimeException("eglChooseConfig() failed");
        }
        if (num_configs.dref() == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }

        bindWlEglDisplay(eglDisplay,
                         eglExtensions);

        this.eglDisplay = eglDisplay;

        return configs.dref().address;
    }

    private void bindWlEglDisplay(final long eglDisplay,
                                  @Nonnull final String eglExtensions) {

        if (bindDisplay(eglDisplay,
                        eglExtensions)) {
            this.eglQueryWaylandBufferWL = Optional.of(wrap(EglQueryWaylandBufferWL.class,
                                                            this.libEGL.eglGetProcAddress(Pointer.nref("eglQueryWaylandBufferWL").address)).dref());

            //FIXME we need to check this gl extension before we can be 100% sure we support wayland egl.
            this.glEGLImageTargetTexture2DOES = Optional.of(wrap(GlEGLImageTargetTexture2DOES.class,
                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("glEGLImageTargetTexture2DOES").address)).dref());

            if (eglExtensions.contains("EGL_KHR_image_base")) {
                this.eglCreateImageKHR = Optional.of(wrap(EglCreateImageKHR.class,
                                                          this.libEGL.eglGetProcAddress(Pointer.nref("eglCreateImageKHR").address)).dref());
                this.eglDestroyImageKHR = Optional.of(wrap(EglDestroyImageKHR.class,
                                                           this.libEGL.eglGetProcAddress(Pointer.nref("eglDestroyImageKHR").address)).dref());
                this.hasWlEglDisplay = true;
            }
            else {
                LOGGER.warning("Extension EGL_KHR_image_base not available. Required for client side egl support.");
            }
        }
    }

    private boolean bindDisplay(final long eglDisplay,
                                final String extensions) {
        if (extensions.contains("EGL_WL_bind_wayland_display")) {
            final Pointer<EglBindWaylandDisplayWL> eglBindWaylandDisplayWL = Pointer.wrap(EglBindWaylandDisplayWL.class,
                                                                                          this.libEGL.eglGetProcAddress(Pointer.nref("eglBindWaylandDisplayWL").address));
            return eglBindWaylandDisplayWL.dref()
                                          .$(eglDisplay,
                                             this.display.pointer) != 0;
        }
        else {
            LOGGER.warning("Extension EGL_WL_bind_wayland_display not available. Required for client side egl support.");
            return false;
        }
    }

    @Override
    public void visit(@Nonnull final RenderOutput renderOutput) {
        throw new UnsupportedOperationException(String.format("Need an egl capable renderOutput. Got %s",
                                                              renderOutput));
    }

    @Override
    public void visit(@Nonnull final EglOutput eglOutput) {
        render(eglOutput,
               this.scene.getSurfacesStack());
    }

    @Override
    public void visit(@Nonnull DrmEglOutput drmEglOutput) {
        visit((EglOutput)drmEglOutput);
    }

    @Override
    public void visit(@Nonnull X11EglOutput x11EglOutput) {
        visit((EglOutput)x11EglOutput);
    }

    public void render(@Nonnull final EglOutput eglOutput,
                       final Iterable<WlSurfaceResource> surfacesStack) {
        this.libEGL.eglMakeCurrent(this.eglDisplay,
                                   eglOutput.getEglSurface(),
                                   eglOutput.getEglSurface(),
                                   eglOutput.getEglContext());
        eglOutput.renderBegin();

        if (!this.init) {
            //one time init because we need a current context
            assert (eglOutput.getEglContext() != EGL_NO_CONTEXT);
            initRenderer();
        }

        setupEglOutputState(eglOutput);

        //TODO comment out these 2 calls when we have a shell that provides a solid background.
        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f);
        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT);

        //naive single pass, bottom to top overdraw rendering.
        surfacesStack.forEach(this::draw);
        flushRenderState(eglOutput);
    }

    private void initRenderer() {
        //check for required texture glExtensions
        final String glExtensions = wrap(String.class,
                                         this.libGLESv2.glGetString(LibGLESv2.GL_EXTENSIONS)).dref();

        //init shm shaders
        LOGGER.info("GLESv2 glExtensions: " + glExtensions);
        if (!glExtensions.contains("GL_EXT_texture_format_BGRA8888")) {
            LOGGER.severe("Required extension GL_EXT_texture_format_BGRA8888 not available");
            System.exit(1);
        }
        //this shader is reused in wl egl
        this.argb8888ShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_ARGB8888,
                                                         1);
        this.xrgb8888ShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_XRGB8888,
                                                         1);

        //compile wl egl shaders
        if (this.hasWlEglDisplay) {
            this.y_u_vShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                          FRAGMENT_SHADER_EGL_Y_U_V,
                                                          3);
            this.y_uvShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_EGL_Y_UV,
                                                         2);
            this.y_xuxvShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                           FRAGMENT_SHADER_EGL_Y_XUXV,
                                                           2);

            if (glExtensions.contains("GL_OES_EGL_image_external")) {
                this.externalImageShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                                      FRAGMENT_SHADER_EGL_EXTERNAL,
                                                                      1);
            }
            else {
                LOGGER.warning("Extension GL_OES_EGL_image_external not available.");
            }
        }

        //configure texture blending
        this.libGLESv2.glBlendFunc(LibGLESv2.GL_ONE,
                                   LibGLESv2.GL_ONE_MINUS_SRC_ALPHA);
        this.init = true;
    }

    private void setupEglOutputState(@Nonnull final EglOutput eglOutput) {
        //to be used state
        this.eglOutputState = eglOutput.getState()
                                       .orElseGet(() -> initOutputRenderState(eglOutput));
        //updates to state are registered with the builder
        this.newEglOutputState = this.eglOutputState.toBuilder();
    }

    private void flushRenderState(final EglOutput eglOutput) {
        eglOutput.updateState(this.newEglOutputState.build());
        eglOutput.renderEndBeforeSwap();
        this.libEGL.eglSwapBuffers(this.eglDisplay,
                                   eglOutput.getEglSurface());
        eglOutput.renderEndAfterSwap();
    }

    private int createShaderProgram(final String vertexShaderSource,
                                    final String fragmentShaderSource,
                                    final int nroTextures) {
        final int vertexShader = compileShader(vertexShaderSource,
                                               LibGLESv2.GL_VERTEX_SHADER);
        final int fragmentShader = compileShader(fragmentShaderSource,
                                                 LibGLESv2.GL_FRAGMENT_SHADER);

        //shader program
        final int shaderProgram = this.libGLESv2.glCreateProgram();
        this.libGLESv2.glAttachShader(shaderProgram,
                                      vertexShader);

        this.libGLESv2.glAttachShader(shaderProgram,
                                      fragmentShader);

        this.libGLESv2.glLinkProgram(shaderProgram);

        //check the link status
        final Pointer<Integer> linked = Pointer.nref(0);
        this.libGLESv2.glGetProgramiv(shaderProgram,
                                      LibGLESv2.GL_LINK_STATUS,
                                      linked.address);
        if (linked.dref() == 0) {
            final Pointer<Integer> infoLen = Pointer.nref(0);
            this.libGLESv2.glGetProgramiv(shaderProgram,
                                          LibGLESv2.GL_INFO_LOG_LENGTH,
                                          infoLen.address);
            int logSize = infoLen.dref();
            if (logSize <= 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Pointer<String> log = Pointer.nref(new String(new char[logSize]));
            this.libGLESv2.glGetProgramInfoLog(shaderProgram,
                                               logSize,
                                               0L,
                                               log.address);
            this.libGLESv2.glDeleteProgram(shaderProgram);
            System.err.println("Error compiling the vertex shader: " + log.dref());
            System.exit(1);
        }

        //find shader arguments
        this.projectionArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                 Pointer.nref("u_projection").address);
        this.transformArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                Pointer.nref("u_transform").address);
        this.positionArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                              Pointer.nref("a_position").address);
        this.textureCoordinateArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                                       Pointer.nref("a_texCoord").address);

        for (int i = 0; i < nroTextures; i++) {
            this.textureArgs[i] = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                      Pointer.nref("u_texture" + i).address);
        }


        return shaderProgram;
    }

    private EglOutputState initOutputRenderState(final EglOutput eglOutput) {

        final EglOutputState.Builder builder = EglOutputState.builder();
        final Output output = eglOutput.getWlOutput()
                                       .getOutput();
        updateTransform(builder,
                        output);

        final EglOutputState eglOutputState = builder.build();
        eglOutput.updateState(eglOutputState);

        //listen for external updates
        output.getTransformSignal()
              .connect(event -> handleOutputUpdate(eglOutput,
                                                   output));
        output.getModeSignal()
              .connect(event -> handleOutputUpdate(eglOutput,
                                                   output));

        return eglOutputState;
    }

    private int compileShader(final String shaderSource,
                              final int shaderType) {
        final int                      shader  = this.libGLESv2.glCreateShader(shaderType);
        final Pointer<Pointer<String>> shaders = Pointer.nref(Pointer.nref(shaderSource));
        this.libGLESv2.glShaderSource(shader,
                                      1,
                                      shaders.address,
                                      0L);
        this.libGLESv2.glCompileShader(shader);

        checkShaderCompilation(shader);
        return shader;
    }

    private void updateTransform(final EglOutputState.Builder eglOutputStateBuilder,
                                 final Output output) {

        final OutputMode mode   = output.getMode();
        final int        width  = mode.getWidth();
        final int        height = mode.getHeight();

        //first time render for this output, clear it.
        this.libGLESv2.glViewport(0,
                                  0,
                                  width,
                                  height);
        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f);
        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT);

        eglOutputStateBuilder.glTransform(createGlTransform(output));
    }

    private void handleOutputUpdate(final EglOutput eglOutput,
                                    final Output output) {
        eglOutput.getState()
                 .ifPresent(eglOutputState -> {
                     final EglOutputState.Builder stateBuilder = eglOutputState.toBuilder();
                     updateTransform(stateBuilder,
                                     output);
                     eglOutput.updateState(stateBuilder.build());
                     //schedule new render
                     eglOutput.render();
                 });
    }

    private void checkShaderCompilation(final int shader) {
        final Pointer<Integer> vstatus = Pointer.nref(0);
        this.libGLESv2.glGetShaderiv(shader,
                                     LibGLESv2.GL_COMPILE_STATUS,
                                     vstatus.address);
        if (vstatus.dref() == 0) {
            //failure!
            //get log length
            final Pointer<Integer> logLength = Pointer.nref(0);
            this.libGLESv2.glGetShaderiv(shader,
                                         LibGLESv2.GL_INFO_LOG_LENGTH,
                                         logLength.address);
            //get log
            int logSize = logLength.dref();
            if (logSize == 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Pointer<String> log = Pointer.nref(new String(new char[logSize]));
            this.libGLESv2.glGetShaderInfoLog(shader,
                                              logSize,
                                              0L,
                                              log.address);
            System.err.println("Error compiling the vertex shader: " + log.dref());
            System.exit(1);
        }
    }

    private Mat4 createGlTransform(final Output output) {

        final OutputMode mode = output.getMode();

        final int width  = mode.getWidth();
        final int height = mode.getHeight();

        //@formatter:off
        return Mat4.create(2.0f / width, 0,              0, -1,
                           0,            2.0f / -height, 0,  1,
                           0,            0,              1,  0,
                           0,            0,              0,  1).multiply(output.getInverseTransform());
        //@formatter:on
    }

    private void draw(final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        //don't bother rendering subsurfaces if the parent doesn't have a buffer.
        wlSurface.getSurface()
                 .getState()
                 .getBuffer()
                 .ifPresent(wlBufferResource -> {
                     final LinkedList<WlSurfaceResource> subsurfaces = this.scene.getSubsurfaceStack(wlSurfaceResource);
                     draw(wlSurfaceResource,
                          wlBufferResource);
                     subsurfaces.forEach((subsurface) -> {
                         if (subsurface != wlSurfaceResource) {
                             draw(subsurface);
                         }
                     });
                 });
    }

    private void draw(final WlSurfaceResource wlSurfaceResource,
                      final WlBufferResource wlBufferResource) {
        queryBuffer(wlBufferResource).accept(new BufferVisitor() {
            @Override
            public void visit(@Nonnull final Buffer buffer) {
                LOGGER.warning("Unsupported buffer.");
            }

            @Override
            public void visit(@Nonnull final EglBuffer eglBuffer) {
                drawEgl(wlSurfaceResource,
                        eglBuffer);
            }

            @Override
            public void visit(@Nonnull final SmBuffer smBuffer) {
                drawShm(wlSurfaceResource,
                        smBuffer);
            }
        });
    }

    private void drawShm(final @Nonnull WlSurfaceResource wlSurfaceResource,
                         final SmBuffer smBuffer) {

        queryShmSurfaceRenderState(wlSurfaceResource,
                                   smBuffer.getShmBuffer()).ifPresent(surfaceRenderState -> surfaceRenderState.accept(new SurfaceRenderStateVisitor() {
            @Override
            public Optional<SurfaceRenderState> visit(final ShmSurfaceState shmSurfaceState) {
                drawShm(wlSurfaceResource,
                        shmSurfaceState);
                return null;
            }
        }));
    }

    private Optional<SurfaceRenderState> queryShmSurfaceRenderState(final WlSurfaceResource wlSurfaceResource,
                                                                    final ShmBuffer shmBuffer) {

        final WlSurface              wlSurface          = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface                surface            = wlSurface.getSurface();
        Optional<SurfaceRenderState> surfaceRenderState = surface.getRenderState();

        if (surfaceRenderState.isPresent()) {
            surfaceRenderState = surfaceRenderState.get()
                                                   .accept(new SurfaceRenderStateVisitor() {
                                                       @Override
                                                       public Optional<SurfaceRenderState> visit(final ShmSurfaceState shmSurfaceState) {
                                                           //the surface already has an shm render state associated. update it.
                                                           return createShmSurfaceRenderState(wlSurfaceResource,
                                                                                              shmBuffer,
                                                                                              Optional.of(shmSurfaceState));
                                                       }

                                                       @Override
                                                       public Optional<SurfaceRenderState> visit(final EglSurfaceState eglSurfaceState) {
                                                           //the surface was previously associated with an egl render state but is now using an shm render state. create it.
                                                           destroy(eglSurfaceState);
                                                           //TODO we could reuse the texture id from the egl surface render state
                                                           return createShmSurfaceRenderState(wlSurfaceResource,
                                                                                              shmBuffer,
                                                                                              Optional.empty());
                                                       }
                                                   });
        }
        else {
            //the surface was not previously associated with any render state. create an shm render state.
            surfaceRenderState = createShmSurfaceRenderState(wlSurfaceResource,
                                                             shmBuffer,
                                                             Optional.empty());
        }

        if (surfaceRenderState.isPresent()) {
            surface
                    .setRenderState(surfaceRenderState.get());
        }
        else {
            onDestroy(wlSurfaceResource);
        }

        return surfaceRenderState;
    }


    private Optional<SurfaceRenderState> createShmSurfaceRenderState(final WlSurfaceResource wlSurfaceResource,
                                                                     final ShmBuffer shmBuffer,
                                                                     final Optional<ShmSurfaceState> oldRenderState) {
        //new values
        final int pitch;
        final int height = shmBuffer.getHeight();
        final int target = LibGLESv2.GL_TEXTURE_2D;
        final int shaderProgram;
        final int glFormat;
        final int glPixelType;
        final int texture;

        final int shmBufferFormat = shmBuffer.getFormat();
        final int argb8888        = WlShmFormat.ARGB8888.value;
        final int xrgb8888        = WlShmFormat.XRGB8888.value;

        if (argb8888 == shmBufferFormat) {
            shaderProgram = this.argb8888ShaderProgram;
            pitch = shmBuffer.getStride() / 4;
            glFormat = LibGLESv2.GL_BGRA_EXT;
            glPixelType = LibGLESv2.GL_UNSIGNED_BYTE;
        }
        else if (xrgb8888 == shmBufferFormat) {
            shaderProgram = this.xrgb8888ShaderProgram;
            pitch = shmBuffer.getStride() / 4;
            glFormat = LibGLESv2.GL_BGRA_EXT;
            glPixelType = LibGLESv2.GL_UNSIGNED_BYTE;
        }
        else {
            LOGGER.warning(String.format("Unknown shm buffer format: %d",
                                         shmBufferFormat));
            return Optional.empty();
        }

        final ShmSurfaceState newShmSurfaceState;


        if (oldRenderState.isPresent()) {
            final ShmSurfaceState oldShmSurfaceState = oldRenderState.get();
            texture = oldShmSurfaceState.getTexture();

            newShmSurfaceState = ShmSurfaceState.create(pitch,
                                                        height,
                                                        target,
                                                        shaderProgram,
                                                        glFormat,
                                                        glPixelType,
                                                        texture);

            if (pitch != oldShmSurfaceState.getPitch() ||
                height != oldShmSurfaceState.getHeight() ||
                glFormat != oldShmSurfaceState.getGlFormat() ||
                glPixelType != oldShmSurfaceState.getGlPixelType()) {
                //state needs full texture updating
                shmUpdateAll(wlSurfaceResource,
                             shmBuffer,
                             newShmSurfaceState);
            }
            else {
                //partial texture update
                shmUpdateDamaged(wlSurfaceResource,
                                 shmBuffer,
                                 newShmSurfaceState);
            }
        }
        else {
            //allocate new texture id & upload full texture
            texture = genTexture(target);
            newShmSurfaceState = ShmSurfaceState.create(pitch,
                                                        height,
                                                        target,
                                                        shaderProgram,
                                                        glFormat,
                                                        glPixelType,
                                                        texture);
            shmUpdateAll(wlSurfaceResource,
                         shmBuffer,
                         newShmSurfaceState);
        }

        return Optional.of(newShmSurfaceState);
    }

    private void shmUpdateDamaged(final WlSurfaceResource wlSurfaceResource,
                                  final ShmBuffer shmBuffer,
                                  final ShmSurfaceState newShmSurfaceState) {
        //TODO implement damage
        shmUpdateAll(wlSurfaceResource,
                     shmBuffer,
                     newShmSurfaceState);
    }

    private void shmUpdateAll(final WlSurfaceResource wlSurfaceResource,
                              final ShmBuffer shmBuffer,
                              final ShmSurfaceState newShmSurfaceState) {
        this.libGLESv2.glBindTexture(newShmSurfaceState.getTarget(),
                                     newShmSurfaceState.getTexture());
        shmBuffer.beginAccess();
        this.libGLESv2.glTexImage2D(newShmSurfaceState.getTarget(),
                                    0,
                                    newShmSurfaceState.getGlFormat(),
                                    newShmSurfaceState.getPitch(),
                                    newShmSurfaceState.getHeight(),
                                    0,
                                    newShmSurfaceState.getGlFormat(),
                                    newShmSurfaceState.getGlPixelType(),
                                    JNI.unwrap(shmBuffer.getData()));
        shmBuffer.endAccess();
        this.libGLESv2.glBindTexture(newShmSurfaceState.getTarget(),
                                     0);

        //FIXME firing the paint callback here is actually wrong since we might still need to draw on a different output. Only when all views of a surface are processed, we can call the fire paint callback.
        //TODO Introduce the concept of views => output <-- view (=many2many) --> surface
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        wlSurface.getSurface()
                 .firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));
    }

    private void drawShm(final @Nonnull WlSurfaceResource wlSurfaceResource,
                         final ShmSurfaceState shmSurfaceState) {
        final int shaderProgram = shmSurfaceState.getShaderProgram();

        //activate & setup shader
        this.libGLESv2.glUseProgram(shaderProgram);
        setupVertexParams(wlSurfaceResource,
                          shmSurfaceState.getPitch(),
                          shmSurfaceState.getHeight());

        //set the buffer in the shader
        this.libGLESv2.glActiveTexture(LibGLESv2.GL_TEXTURE0);
        this.libGLESv2.glBindTexture(shmSurfaceState.getTarget(),
                                     shmSurfaceState.getTexture());
        this.libGLESv2.glUniform1i(this.textureArgs[0],
                                   0);

        //draw
        //enable texture blending
        this.libGLESv2.glEnable(LibGLESv2.GL_BLEND);
        this.libGLESv2.glDrawArrays(LibGLESv2.GL_TRIANGLES,
                                    0,
                                    6);

        //cleanup
        this.libGLESv2.glDisable(LibGLESv2.GL_BLEND);
        this.libGLESv2.glDisableVertexAttribArray(this.positionArg);
        this.libGLESv2.glDisableVertexAttribArray(this.textureArgs[0]);
        this.libGLESv2.glUseProgram(0);
    }

    private Optional<SurfaceRenderState> queryEglSurfaceRenderState(final WlSurfaceResource wlSurfaceResource,
                                                                    final EglBuffer eglBuffer) {

        final WlSurface              wlSurface          = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface                surface            = wlSurface.getSurface();
        Optional<SurfaceRenderState> surfaceRenderState = surface.getRenderState();

        if (surfaceRenderState.isPresent()) {
            surfaceRenderState = surfaceRenderState.get()
                                                   .accept(new SurfaceRenderStateVisitor() {
                                                       @Override
                                                       public Optional<SurfaceRenderState> visit(final ShmSurfaceState shmSurfaceState) {
                                                           //the surface was previously associated with an shm render state but is now using an egl render state. create it.
                                                           //TODO we could reuse the texture id
                                                           destroy(shmSurfaceState);
                                                           return createEglSurfaceRenderState(eglBuffer,
                                                                                              Optional.empty());
                                                       }

                                                       @Override
                                                       public Optional<SurfaceRenderState> visit(final EglSurfaceState eglSurfaceState) {
                                                           //the surface already has an egl render state associated. update it.
                                                           return createEglSurfaceRenderState(eglBuffer,
                                                                                              Optional.of(eglSurfaceState));
                                                       }
                                                   });
        }
        else {
            //the surface was not previously associated with any render state. create an egl render state.
            surfaceRenderState = createEglSurfaceRenderState(eglBuffer,
                                                             Optional.empty());
        }

        if (surfaceRenderState.isPresent()) {
            surface.setRenderState(surfaceRenderState.get());
        }
        else {
            onDestroy(wlSurfaceResource);
        }

        return surfaceRenderState;
    }

    private Optional<SurfaceRenderState> createEglSurfaceRenderState(final EglBuffer eglBuffer,
                                                                     final Optional<EglSurfaceState> oldRenderState) {
        //surface egl render states:
        final int     pitch  = eglBuffer.getWidth();
        final int     height = eglBuffer.getHeight();
        final boolean yInverted;
        final int     shaderProgram;
        final int     target;
        final int[]   textures;
        final long[]  eglImages;

        //gather render states:
        final long buffer = eglBuffer.getWlBufferResource().pointer;

        final EglQueryWaylandBufferWL queryWaylandBuffer = this.eglQueryWaylandBufferWL.get();

        final Pointer<Integer> yInvertedP = Pointer.nref(0);

        yInverted = queryWaylandBuffer.$(this.eglDisplay,
                                         buffer,
                                         EGL_WAYLAND_Y_INVERTED_WL,
                                         yInvertedP.address) == 0 || yInvertedP.dref() != 0;

        switch (eglBuffer.getTextureFormat()) {
            case EGL_TEXTURE_RGB:
            case EGL_TEXTURE_RGBA:
            default:
                textures = new int[1];
                eglImages = new long[1];
                target = LibGLESv2.GL_TEXTURE_2D;
                shaderProgram = this.argb8888ShaderProgram;
                break;
            case EGL_TEXTURE_EXTERNAL_WL:
                textures = new int[1];
                eglImages = new long[1];
                target = LibGLESv2.GL_TEXTURE_EXTERNAL_OES;
                shaderProgram = this.externalImageShaderProgram;
                break;
            case EGL_TEXTURE_Y_UV_WL:
                textures = new int[2];
                eglImages = new long[2];
                target = LibGLESv2.GL_TEXTURE_2D;
                shaderProgram = this.y_uvShaderProgram;
                break;
            case EGL_TEXTURE_Y_U_V_WL:
                textures = new int[3];
                eglImages = new long[3];
                target = LibGLESv2.GL_TEXTURE_2D;
                shaderProgram = this.y_u_vShaderProgram;
                break;
            case EGL_TEXTURE_Y_XUXV_WL:
                textures = new int[2];
                eglImages = new long[2];
                target = LibGLESv2.GL_TEXTURE_2D;
                shaderProgram = this.y_xuxvShaderProgram;
                break;
        }

        //delete old egl images
        oldRenderState.ifPresent(oldEglSurfaceState -> {
            for (final long oldEglImage : oldEglSurfaceState.getEglImages()) {
                this.eglDestroyImageKHR.get()
                                       .$(this.eglDisplay,
                                          oldEglImage);
            }
        });

        //create egl images
        final int[] attribs = new int[3];

        for (int i = 0; i < eglImages.length; i++) {
            attribs[0] = EGL_WAYLAND_PLANE_WL;
            attribs[1] = i;
            attribs[2] = EGL_NONE;

            final long eglImage = this.eglCreateImageKHR.get()
                                                        .$(this.eglDisplay,
                                                           EGL_NO_CONTEXT,
                                                           EGL_WAYLAND_BUFFER_WL,
                                                           buffer,
                                                           Pointer.nref(attribs).address);
            if (eglImage == EGL_NO_IMAGE_KHR) {
                return Optional.empty();
            }
            else {
                eglImages[i] = eglImage;
            }

            //make sure we have valid texture ids
            oldRenderState.ifPresent(oldEglSurfaceState -> {

                final int[]   oldTextures      = oldEglSurfaceState.getTextures();
                final int     deltaNewTextures = textures.length - oldTextures.length;
                final boolean needNewTextures  = deltaNewTextures > 0;

                //reuse old texture ids
                System.arraycopy(oldTextures,
                                 0,
                                 textures,
                                 0,
                                 needNewTextures ? oldTextures.length : textures.length);

                if (needNewTextures) {
                    //generate missing texture ids
                    for (int j = textures.length - 1; j >= textures.length - deltaNewTextures; j--) {
                        textures[j] = genTexture(target);
                    }
                }
                else if (deltaNewTextures < 0) {
                    //cleanup old unused texture ids
                    for (int j = oldTextures.length - 1; j >= oldTextures.length + deltaNewTextures; j--) {
                        this.libGLESv2.glDeleteTextures(1,
                                                        Pointer.nref(oldTextures[j]).address);
                    }
                }
            });

            this.libGLESv2.glActiveTexture(LibGLESv2.GL_TEXTURE0 + i);
            this.libGLESv2.glBindTexture(target,
                                         textures[i]);
            this.glEGLImageTargetTexture2DOES.get()
                                             .$(target,
                                                eglImage);
        }

        return Optional.of(EglSurfaceState.create(pitch,
                                                  height,
                                                  target,
                                                  shaderProgram,
                                                  yInverted,
                                                  textures,
                                                  eglImages));
    }


    private void drawEgl(final WlSurfaceResource wlSurfaceResource,
                         final EglBuffer eglBuffer) {
        queryEglSurfaceRenderState(wlSurfaceResource,
                                   eglBuffer).ifPresent(surfaceRenderState -> surfaceRenderState.accept(new SurfaceRenderStateVisitor() {
            @Override
            public Optional<SurfaceRenderState> visit(final EglSurfaceState eglSurfaceState) {
                drawEgl(wlSurfaceResource,
                        eglSurfaceState);
                return null;
            }
        }));
    }

    private void drawEgl(final WlSurfaceResource wlSurfaceResource,
                         final EglSurfaceState eglSurfaceState) {
        //TODO unify with drawShm

        final int shaderProgram = eglSurfaceState.getShaderProgram();

        //activate & setup shader
        this.libGLESv2.glUseProgram(shaderProgram);
        setupVertexParams(wlSurfaceResource,
                          eglSurfaceState.getPitch(),
                          eglSurfaceState.getHeight());

        //set the buffer in the shader
        final int[] textures = eglSurfaceState.getTextures();
        for (int i = 0, texturesLength = textures.length; i < texturesLength; i++) {
            final int texture = textures[i];
            final int target  = eglSurfaceState.getTarget();

            this.libGLESv2.glActiveTexture(LibGLESv2.GL_TEXTURE0 + i);
            this.libGLESv2.glBindTexture(target,
                                         texture);
            this.libGLESv2.glTexParameteri(target,
                                           LibGLESv2.GL_TEXTURE_MIN_FILTER,
                                           LibGLESv2.GL_NEAREST);
            this.libGLESv2.glTexParameteri(target,
                                           LibGLESv2.GL_TEXTURE_MAG_FILTER,
                                           LibGLESv2.GL_NEAREST);
            this.libGLESv2.glUniform1i(this.textureArgs[i],
                                       0);
        }

        //draw
        //enable texture blending
        this.libGLESv2.glEnable(LibGLESv2.GL_BLEND);
        this.libGLESv2.glDrawArrays(LibGLESv2.GL_TRIANGLES,
                                    0,
                                    6);

        //cleanup
        this.libGLESv2.glDisable(LibGLESv2.GL_BLEND);
        this.libGLESv2.glDisableVertexAttribArray(this.positionArg);
        for (int i = 0, texturesLength = textures.length; i < texturesLength; i++) {
            this.libGLESv2.glDisableVertexAttribArray(this.textureArgs[i]);
        }
        this.libGLESv2.glUseProgram(0);

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        wlSurface.getSurface()
                 .firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));
    }

    private int genTexture(final int target) {
        final Pointer<Integer> texture = Pointer.nref(0);
        this.libGLESv2.glGenTextures(1,
                                     texture.address);
        final Integer textureId = texture.dref();
        this.libGLESv2.glBindTexture(target,
                                     textureId);
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_WRAP_S,
                                       LibGLESv2.GL_CLAMP_TO_EDGE);
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_WRAP_T,
                                       LibGLESv2.GL_CLAMP_TO_EDGE);
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_MIN_FILTER,
                                       LibGLESv2.GL_NEAREST);
        this.libGLESv2.glTexParameteri(target,
                                       LibGLESv2.GL_TEXTURE_MAG_FILTER,
                                       LibGLESv2.GL_NEAREST);
        this.libGLESv2.glBindTexture(target,
                                     0);
        return textureId;
    }

    private void setupVertexParams(final @Nonnull WlSurfaceResource wlSurfaceResource,
                                   final float bufferWidth,
                                   final float bufferHeight) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        final float[] transform = surface.getTransform()
                                         .toArray();

        //define vertex data
        final Pointer<Float> vertexData = vertexData(bufferWidth,
                                                     bufferHeight);

        //upload uniform vertex data
        final Pointer<Float> projectionBuffer = Pointer.nref(this.eglOutputState.getGlTransform()
                                                                                .toArray());
        this.libGLESv2.glUniformMatrix4fv(this.projectionArg,
                                          1,
                                          0,
                                          projectionBuffer.address);

        final Pointer<Float> transformBuffer = Pointer.nref(transform);
        this.libGLESv2.glUniformMatrix4fv(this.transformArg,
                                          1,
                                          0,
                                          transformBuffer.address);
        //set vertex data in shader
        this.libGLESv2.glEnableVertexAttribArray(this.positionArg);
        this.libGLESv2.glVertexAttribPointer(this.positionArg,
                                             2,
                                             LibGLESv2.GL_FLOAT,
                                             0,
                                             4 * Float.BYTES,
                                             vertexData.address);

        this.libGLESv2.glEnableVertexAttribArray(this.textureCoordinateArg);
        this.libGLESv2.glVertexAttribPointer(this.textureCoordinateArg,
                                             2,
                                             LibGLESv2.GL_FLOAT,
                                             0,
                                             4 * Float.BYTES,
                                             vertexData.offset(2).address);
    }

    private Pointer<Float> vertexData(final float bufferWidth,
                                      final float bufferHeight) {
        //first pair => attribute vec2 a_position
        //second pair => attribute vec2 a_texCoord
        return Pointer.nref(//top left:
                            0f,
                            0f,
                            0f,
                            0f,
                            //top right:
                            bufferWidth,
                            0f,
                            1f,
                            0f,
                            //bottom right:
                            bufferWidth,
                            bufferHeight,
                            1f,
                            1f,
                            //bottom right:
                            bufferWidth,
                            bufferHeight,
                            1f,
                            1f,
                            //bottom left:
                            0f,
                            bufferHeight,
                            0f,
                            1f,
                            //top left:
                            0f,
                            0f,
                            0f,
                            0f);
    }
}
