package org.westmalle.wayland.gles2;

import org.freedesktop.jaccall.JNI;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.westmalle.wayland.core.*;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.nativ.libEGL.EglQueryWaylandBufferWL;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libGLESv2.GlEGLImageTargetTexture2DOES;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Pointer.wrap;
import static org.freedesktop.jaccall.Size.sizeof;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_HEIGHT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WAYLAND_Y_INVERTED_WL;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WIDTH;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_BGRA_EXT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_BLEND;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_CLAMP_TO_EDGE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COMPILE_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FLOAT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FRAGMENT_SHADER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_INFO_LOG_LENGTH;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_LINK_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_NEAREST;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ONE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ONE_MINUS_SRC_ALPHA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE0;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_2D;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_MAG_FILTER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_MIN_FILTER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_WRAP_S;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_WRAP_T;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TRIANGLES;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_VERTEX_SHADER;

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
            "uniform sampler2D u_texture;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(u_texture, v_texCoord);\n" +
            "}";

    private static final String FRAGMENT_SHADER_XRGB8888 =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "    gl_FragColor = vec4(texture2D(u_texture, v_texCoord).bgr, 1.0);\n" +
            "}";

    private static final String FRAGMENT_SHADER_EGL_EXTERNAL =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES u_texture;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main(){\n" +
            "   gl_FragColor = texture2D(u_texture, v_texCoord)\n;" +
            "}";

    private static final String FRAGMENT_CONVERT_YUV =
            "  gl_FragColor.r = y + 1.59602678 * v;\n" +
            "  gl_FragColor.g = y - 0.39176229 * u - 0.81296764 * v;\n" +
            "  gl_FragColor.b = y + 2.01723214 * u;\n" +
            "  gl_FragColor.a = 1.0;\n" +
            "}";

    private static final String FRAGMENT_SHADER_EGL_Y_UV =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_texture1;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main() {\n" +
            "  float y = 1.16438356 * (texture2D(u_texture, v_texCoord).x - 0.0625);\n" +
            "  float u = texture2D(u_texture1, v_texCoord).r - 0.5;\n" +
            "  float v = texture2D(u_texture1, v_texCoord).g - 0.5;\n" +
            FRAGMENT_CONVERT_YUV;

    private static final String FRAGMENT_SHADER_EGL_Y_U_V =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_texture1;\n" +
            "uniform sampler2D u_texture2;\n" +
            "varying vec2 v_texCoord;\n" +
            "void main() {\n" +
            "  float y = 1.16438356 * (texture2D(tex, v_texCoord).x - 0.0625);\n" +
            "  float u = texture2D(tex1, v_texCoord).x - 0.5;\n" +
            "  float v = texture2D(tex2, v_texCoord).x - 0.5;\n" +
            FRAGMENT_CONVERT_YUV;

    private static final String FRAGMENT_SHADER_EGL_Y_XUXV =
            "precision mediump float;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_texture1;\n" +
            "varying vec2 v_texcoord;\n" +
            "void main() {\n" +
            "  float y = 1.16438356 * (texture2D(tex, v_texcoord).x - 0.0625);\n" +
            "  float u = texture2D(tex1, v_texcoord).g - 0.5;\n" +
            "  float v = texture2D(tex1, v_texcoord).a - 0.5;\n" +
            FRAGMENT_CONVERT_YUV;


    @Nonnull
    private final Map<WlSurfaceResource, SurfaceRenderState> surfaceRenderStates = new HashMap<>();

    @Nonnull
    private Optional<EglQueryWaylandBufferWL>      eglQueryWaylandBufferWL      = Optional.empty();
    @Nonnull
    private Optional<GlEGLImageTargetTexture2DOES> glEGLImageTargetTexture2DOES = Optional.empty();

    @Nonnull
    private final LibEGL    libEGL;
    @Nonnull
    private final LibGLESv2 libGLESv2;
    @Nonnull
    private final Scene     scene;

    private boolean init = false;

    @Nonnull
    private float[] projection = Mat4.IDENTITY.toArray();

    private int projectionArg;
    private int transformArg;
    private int positionArg;
    private int textureCoordinateArg;
    private int textureArg;

    //used by shm & egl
    private int argb8888ShaderProgram;
    //used by shm
    private int xrgb8888ShaderProgram;
    //used by egl
    private int y_u_vShaderProgram;
    private int y_uvShaderProgram;
    private int y_xuxvShaderProgram;
    private int externalImageShaderProgram;

    @Inject
    Gles2Renderer(@Nonnull final LibEGL libEGL,
                  @Nonnull final LibGLESv2 libGLESv2,
                  @Nonnull final Scene scene) {
        this.libEGL = libEGL;
        this.libGLESv2 = libGLESv2;
        this.scene = scene;
    }

    public void begin(@Nonnull final EglPlatform eglPlatform) {

        eglPlatform.begin();

        //FIXME how to render to multiple outputs?
        final WlOutput   wlOutput = eglPlatform.getWlOutput();
        final Output     output   = wlOutput.getOutput();
        final OutputMode mode     = output.getMode();

        //FIXME move init to factory create method?
        if (!this.init) {
            init(eglPlatform);
        }

        final int width  = mode.getWidth();
        final int height = mode.getHeight();

        this.libGLESv2.glViewport(0,
                                  0,
                                  width,
                                  height);

        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f);

        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT);

        //@formatter:off
        this.projection = Mat4.create(2.0f / width, 0,              0, -1,
                                      0,            2.0f / -height, 0,  1,
                                      0,            0,              1,  0,
                                      0,            0,              0,  1).toArray();
        //@formatter:on
    }

    private void init(final EglPlatform eglPlatform) {
        //check for required texture extensions
        final String extensions = wrap(String.class,
                                       this.libGLESv2.glGetString(GL_EXTENSIONS)).dref();

        //init shm shaders
        LOGGER.info("GLESv2 extensions: " + extensions);
        if (!extensions.contains("GL_EXT_texture_format_BGRA8888")) {
            LOGGER.severe("Required extension GL_EXT_texture_format_BGRA8888 not available");
            System.exit(1);
        }
        //this shader is reused in wl egl
        this.argb8888ShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_ARGB8888);
        this.xrgb8888ShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_XRGB8888);

        //init wl egl functionality
        if (eglPlatform.hasBindDisplay()) {
            this.eglQueryWaylandBufferWL = Optional.of(wrap(EglQueryWaylandBufferWL.class,
                                                            this.libEGL.eglGetProcAddress(Pointer.nref("eglQueryWaylandBufferWL").address))
                                                               .dref());

            this.y_u_vShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                          FRAGMENT_SHADER_EGL_Y_U_V);
            this.y_uvShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                         FRAGMENT_SHADER_EGL_Y_UV);
            this.y_xuxvShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                           FRAGMENT_SHADER_EGL_Y_XUXV);

            this.glEGLImageTargetTexture2DOES = Optional.of(wrap(GlEGLImageTargetTexture2DOES.class,
                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("glEGLImageTargetTexture2DOES").address))
                                                                    .dref());

            if (extensions.contains("GL_OES_EGL_image_external")) {
                this.externalImageShaderProgram = createShaderProgram(VERTEX_SHADER,
                                                                      FRAGMENT_SHADER_EGL_EXTERNAL);

            }
            else {
                LOGGER.warning("Extension GL_OES_EGL_image_external not available");
            }
        }

        //configure texture blending
        this.libGLESv2.glBlendFunc(GL_ONE,
                                   GL_ONE_MINUS_SRC_ALPHA);
        this.init = true;
    }

    private int createShaderProgram(final String vertexShaderSource,
                                    final String fragmentShaderSource) {
        final int vertexShader = createShader(vertexShaderSource,
                                              GL_VERTEX_SHADER);
        final int fragmentShader = createShader(fragmentShaderSource,
                                                GL_FRAGMENT_SHADER);

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
                                      GL_LINK_STATUS,
                                      linked.address);
        if (linked.dref() == 0) {
            final Pointer<Integer> infoLen = Pointer.nref(0);
            this.libGLESv2.glGetProgramiv(shaderProgram,
                                          GL_INFO_LOG_LENGTH,
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
        this.textureArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                              Pointer.nref("u_texture").address);

        return shaderProgram;
    }

    private int createShader(final String shaderSource,
                             final int shaderType) {
        final int                      shader  = this.libGLESv2.glCreateShader(shaderType);
        final Pointer<Pointer<String>> shaders = Pointer.nref(Pointer.nref(shaderSource));
        this.libGLESv2.glShaderSource(shader,
                                      1,
                                      shaders.address,
                                      0L);
        this.libGLESv2.glCompileShader(shader);

        checkShader(shader);
        return shader;
    }

    private void checkShader(final int shader) {
        final Pointer<Integer> vstatus = Pointer.nref(0);
        this.libGLESv2.glGetShaderiv(shader,
                                     GL_COMPILE_STATUS,
                                     vstatus.address);
        if (vstatus.dref() == 0) {
            //failure!
            //get log length
            final Pointer<Integer> logLength = Pointer.nref(0);
            this.libGLESv2.glGetShaderiv(shader,
                                         GL_INFO_LOG_LENGTH,
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

    public void render(@Nonnull final EglPlatform eglPlatform) {
        //naive single pass, bottom to top overdraw rendering.
        this.scene.getSurfacesStack()
                  .forEach((wlSurfaceResource) -> draw(eglPlatform,
                                                       wlSurfaceResource));
    }

    private void draw(final EglPlatform eglPlatform,
                      final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        //don't bother rendering subsurfaces if the parent doesn't have a buffer.
        wlSurface.getSurface()
                 .getState()
                 .getBuffer()
                 .ifPresent(wlBufferResource -> {
                     final LinkedList<WlSurfaceResource> subsurfaces = this.scene.getSubsurfaceStack(wlSurfaceResource);
                     draw(eglPlatform,
                          wlSurfaceResource,
                          wlBufferResource);
                     subsurfaces.forEach((subsurface) -> {
                         if (subsurface != wlSurfaceResource) {
                             draw(eglPlatform,
                                  subsurface);
                         }
                     });
                 });
    }

    private void draw(final EglPlatform eglPlatform,
                      final WlSurfaceResource wlSurfaceResource,
                      final WlBufferResource wlBufferResource) {
        //FIXME we need to listen for surface destruction and clean up any resources associated with it.

        final ShmBuffer shmBuffer = ShmBuffer.get(wlBufferResource);
        if (shmBuffer != null) {
            drawShm(wlSurfaceResource,
                    shmBuffer);
        }
        else if (eglPlatform.hasBindDisplay()) {
            final int textureFormat = this.eglQueryWaylandBufferWL.get()
                                                                  .$(eglPlatform.getEglDisplay(),
                                                                     wlBufferResource.pointer,
                                                                     LibEGL.EGL_TEXTURE_FORMAT,
                                                                     0L);
            if (textureFormat != 0) {
                drawEgl(eglPlatform,
                        wlSurfaceResource,
                        wlBufferResource,
                        textureFormat);
            }
        }

        //TODO dma buffer.
    }

    private void drawEgl(final EglPlatform eglPlatform,
                         final WlSurfaceResource wlSurfaceResource,
                         final WlBufferResource wlBufferResource,
                         final int textureFormat) {


        final Integer shadeProgram = null;//this.eglShaderPrograms.get(textureFormat);
        if (shadeProgram == null) {
            throw new RuntimeException(String.format("Egl texture format not supported: %d",
                                                     textureFormat));
        }

        final long eglDisplay = eglPlatform.getEglDisplay();
        final long buffer     = wlBufferResource.pointer;

        final EglQueryWaylandBufferWL queryWaylandBuffer = this.eglQueryWaylandBufferWL.get();

        final Pointer<Integer> widthP  = Pointer.nref(0);
        final Pointer<Integer> heightP = Pointer.nref(0);
        queryWaylandBuffer.$(eglDisplay,
                             buffer,
                             EGL_WIDTH,
                             widthP.address);
        queryWaylandBuffer.$(eglDisplay,
                             buffer,
                             EGL_HEIGHT,
                             heightP.address);
        final int bufferWidth  = widthP.dref();
        final int bufferHeight = heightP.dref();

        final Pointer<Integer> yInvertedP = Pointer.nref(0);
        final boolean          yInverted;
        if (queryWaylandBuffer.$(eglDisplay,
                                 buffer,
                                 EGL_WAYLAND_Y_INVERTED_WL,
                                 yInvertedP.address) != 0) {
            yInverted = yInvertedP.dref() != 0;
        }
        else {
            yInverted = true;
        }

//        setupVertexParams(wlSurfaceResource,
//                          shadeProgram,
//                          bufferWidth,
//                          bufferHeight);
    }


    private Optional<SurfaceRenderState> queryShmSurfaceRenderState(final WlSurfaceResource wlSurfaceResource,
                                                                    final ShmBuffer shmBuffer) {
        @Nonnull
        Optional<SurfaceRenderState> surfaceRenderState = Optional.ofNullable(this.surfaceRenderStates.get(wlSurfaceResource));

        if (surfaceRenderState.isPresent()) {
            surfaceRenderState = surfaceRenderState.get()
                                                   .accept(new SurfaceRenderStateVisitor() {
                                                       @Override
                                                       public Optional<SurfaceRenderState> visit(final ShmSurfaceRenderState shmSurfaceRenderState) {
                                                           //the surface already has an shm render state associated. update it.
                                                           return createShmSurfaceRenderState(wlSurfaceResource,
                                                                                              shmBuffer,
                                                                                              Optional.of(shmSurfaceRenderState));
                                                       }

                                                       @Override
                                                       public Optional<SurfaceRenderState> visit(final EglSurfaceRenderState eglSurfaceRenderState) {
                                                           //the surface was previously associated with an egl render state but is now using an shm render state. create it.

                                                           //TODO we could reuse the texture id from the egl surface render state
                                                           //TODO destroy egl surface render state

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

        surfaceRenderState.ifPresent(renderState -> this.surfaceRenderStates.put(wlSurfaceResource,
                                                                                 renderState));

        return surfaceRenderState;
    }


    private Optional<SurfaceRenderState> createShmSurfaceRenderState(final WlSurfaceResource wlSurfaceResource,
                                                                     final ShmBuffer shmBuffer,
                                                                     final Optional<ShmSurfaceRenderState> oldRenderState) {
        //new values
        final int pitch;
        final int height = shmBuffer.getHeight();
        final int target = GL_TEXTURE_2D;
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
            glFormat = GL_BGRA_EXT;
            glPixelType = GL_UNSIGNED_BYTE;
        }
        else if (xrgb8888 == shmBufferFormat) {
            shaderProgram = this.xrgb8888ShaderProgram;
            pitch = shmBuffer.getStride() / 4;
            glFormat = GL_BGRA_EXT;
            glPixelType = GL_UNSIGNED_BYTE;
        }
        else {
            LOGGER.warning(String.format("Unknown shm buffer format: %d",
                                         shmBufferFormat));
            return Optional.empty();
        }

        final ShmSurfaceRenderState newShmSurfaceRenderState;


        if (oldRenderState.isPresent()) {
            final ShmSurfaceRenderState oldShmSurfaceRenderState = oldRenderState.get();
            texture = oldShmSurfaceRenderState.getTexture();

            newShmSurfaceRenderState = ShmSurfaceRenderState.create(pitch,
                                                                    height,
                                                                    target,
                                                                    shaderProgram,
                                                                    glFormat,
                                                                    glPixelType,
                                                                    texture);

            if (pitch != oldShmSurfaceRenderState.getPitch() ||
                height != oldShmSurfaceRenderState.getHeight() ||
                glFormat != oldShmSurfaceRenderState.getGlFormat() ||
                glPixelType != oldShmSurfaceRenderState.getGlPixelType()) {
                //state needs full texture updating
                shmUpdateAll(wlSurfaceResource,
                             shmBuffer,
                             newShmSurfaceRenderState);
            }
            else {
                //partial texture update
                shmUpdateDamaged(wlSurfaceResource,
                                 shmBuffer,
                                 newShmSurfaceRenderState);
            }
        }
        else {
            //allocate new texture id & upload full texture
            texture = genTexture(target);
            newShmSurfaceRenderState = ShmSurfaceRenderState.create(pitch,
                                                                    height,
                                                                    target,
                                                                    shaderProgram,
                                                                    glFormat,
                                                                    glPixelType,
                                                                    texture);
            shmUpdateAll(wlSurfaceResource,
                         shmBuffer,
                         newShmSurfaceRenderState);
        }

        return Optional.of(newShmSurfaceRenderState);
    }

    private void shmUpdateDamaged(final WlSurfaceResource wlSurfaceResource,
                                  final ShmBuffer shmBuffer,
                                  final ShmSurfaceRenderState newShmSurfaceRenderState) {
        //TODO implement damage
        shmUpdateAll(wlSurfaceResource,
                     shmBuffer,
                     newShmSurfaceRenderState);
    }

    private void shmUpdateAll(final WlSurfaceResource wlSurfaceResource,
                              final ShmBuffer shmBuffer,
                              final ShmSurfaceRenderState newShmSurfaceRenderState) {
        this.libGLESv2.glBindTexture(newShmSurfaceRenderState.getTarget(),
                                     newShmSurfaceRenderState.getTexture());
        shmBuffer.beginAccess();
        this.libGLESv2.glTexImage2D(newShmSurfaceRenderState.getTarget(),
                                    0,
                                    newShmSurfaceRenderState.getGlFormat(),
                                    newShmSurfaceRenderState.getPitch(),
                                    newShmSurfaceRenderState.getHeight(),
                                    0,
                                    newShmSurfaceRenderState.getGlFormat(),
                                    newShmSurfaceRenderState.getGlPixelType(),
                                    JNI.unwrap(shmBuffer.getData()));
        shmBuffer.endAccess();
        this.libGLESv2.glBindTexture(newShmSurfaceRenderState.getTarget(),
                                     0);
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        wlSurface.getSurface()
                 .firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));
    }

    private int genTexture(int target) {
        Pointer<Integer> texture = Pointer.nref(0);
        this.libGLESv2.glGenTextures(1,
                                     texture.address);
        final Integer textureId = texture.dref();
        this.libGLESv2.glBindTexture(target,
                                     textureId);
        this.libGLESv2.glTexParameteri(target,
                                       GL_TEXTURE_WRAP_S,
                                       GL_CLAMP_TO_EDGE);
        this.libGLESv2.glTexParameteri(target,
                                       GL_TEXTURE_WRAP_T,
                                       GL_CLAMP_TO_EDGE);
        libGLESv2.glTexParameteri(target,
                                  GL_TEXTURE_MIN_FILTER,
                                  GL_NEAREST);
        libGLESv2.glTexParameteri(target,
                                  GL_TEXTURE_MAG_FILTER,
                                  GL_NEAREST);
        this.libGLESv2.glBindTexture(target,
                                     0);
        return textureId;
    }


    private void drawShm(final @Nonnull WlSurfaceResource wlSurfaceResource,
                         final ShmBuffer shmBuffer) {

        queryShmSurfaceRenderState(wlSurfaceResource,
                                   shmBuffer).ifPresent(surfaceRenderState -> surfaceRenderState.accept(new SurfaceRenderStateVisitor() {
            @Override
            public Optional<SurfaceRenderState> visit(final ShmSurfaceRenderState shmSurfaceRenderState) {
                drawShm(wlSurfaceResource,
                        shmSurfaceRenderState);
                return null;
            }
        }));
    }

    private void drawShm(final @Nonnull WlSurfaceResource wlSurfaceResource,
                         final ShmSurfaceRenderState shmSurfaceRenderState) {
        final int shaderProgram = shmSurfaceRenderState.getShaderProgram();

        //activate & setup shader
        this.libGLESv2.glUseProgram(shaderProgram);
        setupVertexParams(wlSurfaceResource,
                          shmSurfaceRenderState.getPitch(),
                          shmSurfaceRenderState.getHeight());

        //set the buffer in the shader
        this.libGLESv2.glActiveTexture(GL_TEXTURE0);
        this.libGLESv2.glBindTexture(shmSurfaceRenderState.getTarget(),
                                     shmSurfaceRenderState.getTexture());
        this.libGLESv2.glUniform1i(this.textureArg,
                                   0);

        //draw
        //enable texture blending
        this.libGLESv2.glEnable(GL_BLEND);
        this.libGLESv2.glDrawArrays(GL_TRIANGLES,
                                    0,
                                    6);

        //cleanup
        this.libGLESv2.glDisable(GL_BLEND);
        this.libGLESv2.glDisableVertexAttribArray(this.positionArg);
        this.libGLESv2.glDisableVertexAttribArray(this.textureArg);
        this.libGLESv2.glUseProgram(0);
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
        final Pointer<Float> projectionBuffer = Pointer.nref(this.projection);
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
                                             GL_FLOAT,
                                             0,
                                             4 * Float.BYTES,
                                             vertexData.address);

        this.libGLESv2.glEnableVertexAttribArray(this.textureCoordinateArg);
        this.libGLESv2.glVertexAttribPointer(this.textureCoordinateArg,
                                             2,
                                             GL_FLOAT,
                                             0,
                                             4 * Float.BYTES,
                                             vertexData.offset(2).address);
    }

    private Pointer<Float> vertexData(final float bufferWidth,
                                      final float bufferHeight) {
        return Pointer.nref(         //top left:
                                     //attribute vec2 a_position
                                     0f,
                                     0f,
                                     //attribute vec2 a_texCoord
                                     0f,
                                     0f,

                                     //top right:
                                     //attribute vec2 a_position
                                     bufferWidth,
                                     0f,
                                     //attribute vec2 a_texCoord
                                     1f,
                                     0f,

                                     //bottom right:
                                     //vec2 a_position
                                     bufferWidth,
                                     bufferHeight,
                                     //vec2 a_texCoord
                                     1f,
                                     1f,

                                     //bottom right:
                                     //vec2 a_position
                                     bufferWidth,
                                     bufferHeight,
                                     //vec2 a_texCoord
                                     1f,
                                     1f,

                                     //bottom left:
                                     //vec2 a_position
                                     0f,
                                     bufferHeight,
                                     //vec2 a_texCoord
                                     0f,
                                     1f,

                                     //top left:
                                     //attribute vec2 a_position
                                     0f,
                                     0f,
                                     //attribute vec2 a_texCoord
                                     0f,
                                     0f);
    }

    @Override
    public void visit(final Platform platform) {
        throw new UnsupportedOperationException(String.format("Need an egl capable platform. Got %s",
                                                              platform));
    }

    @Override
    public void visit(final EglPlatform eglPlatform) {
        begin(eglPlatform);
        render(eglPlatform);
        end(eglPlatform);
    }

    public void end(final EglPlatform renderOutput) {
        renderOutput.end();
    }

    @Override
    public long eglConfig(final long eglDisplay) {


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
        return configs.dref().address;
    }
}
