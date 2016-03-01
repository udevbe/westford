package org.westmalle.wayland.egl;

import com.github.zubnix.jaccall.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_BLEND;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COMPILE_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FLOAT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FRAGMENT_SHADER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_INFO_LOG_LENGTH;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_LINK_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ONE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ONE_MINUS_SRC_ALPHA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE0;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TRIANGLES;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_VERTEX_SHADER;

@Singleton
public class EglGles2Renderer implements Renderer {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static final String VERTEX_SHADER =
            "uniform mat4 u_projection;\n" +
            "uniform mat4 u_transform;\n" +
            "\n" +
            "attribute vec2 a_position;\n" +
            "attribute vec2 a_texCoord;\n" +
            "\n" +
            "varying vec2 v_texCoord;\n" +
            "\n" +
            "void main(){\n" +
            "    v_texCoord = a_texCoord;\n" +
            "    gl_Position = u_projection * u_transform * vec4(a_position, 0.0, 1.0) ;\n" +
            "}";

    static final String FRAGMENT_SHADER_ARGB8888 =
            "precision mediump float;\n" +
            "\n" +
            "uniform sampler2D u_texture;\n" +
            "\n" +
            "varying vec2 v_texCoord;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(u_texture, v_texCoord);\n" +
            "}";

    @Nonnull
    private final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = new WeakHashMap<>();
    @Nonnull
    private final Map<Gles2BufferFormat, Integer>          shaderPrograms    = new HashMap<>();

    @Nonnull
    private final LibGLESv2 libGLESv2;

    private boolean init = false;

    @Nonnull
    private float[] projection = Mat4.IDENTITY.toArray();

    private int projectionArg;
    private int transformArg;
    private int positionArg;
    private int textureCoordinateArg;
    private int textureArg;

    @Inject
    EglGles2Renderer(@Nonnull final LibGLESv2 libGLESv2) {
        this.libGLESv2 = libGLESv2;
    }

    @Override
    public void begin(@Nonnull final WlOutput wlOutput) {
        final Output     output = wlOutput.getOutput();
        final OutputMode mode   = output.getMode();
        //TODO handle (un)supported outputs more gracefully.
        final HasEglOutput hasEglOutput = (HasEglOutput) output.getPlatformImplementation();
        final RenderOutput renderOutput = hasEglOutput.getEglOutput();
        renderOutput.begin();

        if (!this.init) {
            init();
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

    private void init() {
        //check for required texture extensions
        final String extensions = Pointer.wrap(String.class,
                                               this.libGLESv2.glGetString(GL_EXTENSIONS))
                                         .dref();

        LOGGER.info("GLESv2 extensions: " + extensions);
        if (!extensions.contains("GL_EXT_texture_format_BGRA8888")) {
            LOGGER.severe("Required extension GL_EXT_texture_format_BGRA8888 not available");
            System.exit(1);
        }

        for (final Gles2BufferFormat gles2BufferFormat : Gles2BufferFormat.values()) {
            this.shaderPrograms.put(gles2BufferFormat,
                                    createShaderProgram(gles2BufferFormat));
        }

        //configure texture blending
        this.libGLESv2.glBlendFunc(GL_ONE,
                                   GL_ONE_MINUS_SRC_ALPHA);

        this.init = true;
    }

    private int createShaderProgram(final Gles2BufferFormat bufferFormat) {
        //vertex shader
        final int                      vertexShader  = this.libGLESv2.glCreateShader(GL_VERTEX_SHADER);
        final Pointer<Pointer<String>> vertexShaders = Pointer.nref(Pointer.nref(bufferFormat.getVertexShaderSource()));
        this.libGLESv2.glShaderSource(vertexShader,
                                      1,
                                      vertexShaders.address,
                                      0L);
        this.libGLESv2.glCompileShader(vertexShader);

        checkShader(vertexShader);

        //fragment shader
        final int                      fragmentShader  = this.libGLESv2.glCreateShader(GL_FRAGMENT_SHADER);
        final Pointer<Pointer<String>> fragmentShaders = Pointer.nref(Pointer.nref(bufferFormat.getFragmentShaderSource()));
        this.libGLESv2.glShaderSource(fragmentShader,
                                      1,
                                      fragmentShaders.address,
                                      0L);
        this.libGLESv2.glCompileShader(fragmentShader);

        checkShader(fragmentShader);

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

    @Override
    public void draw(@Nonnull final WlSurfaceResource wlSurfaceResource,
                     @Nonnull final WlBufferResource wlBufferResource) {
        final ShmBuffer shmBuffer = ShmBuffer.get(wlBufferResource);
        if (shmBuffer == null) {
            throw new IllegalArgumentException("Buffer resource is not an ShmBuffer.");
        }

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        final float[] transform = surface.getTransform()
                                         .toArray();

        final float bufferWidth  = shmBuffer.getStride() / 4;
        final float bufferHeight = shmBuffer.getHeight();

        //define vertex data
        final Pointer<Float> vertexData = Pointer.nref(         //top left:
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

        //activate shader
        final Gles2BufferFormat gles2BufferFormat = queryBufferFormat(shmBuffer);
        final Integer           shader            = this.shaderPrograms.get(gles2BufferFormat);
        this.libGLESv2.glUseProgram(shader);

        //upload uniform data
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

        querySurfaceData(wlSurfaceResource,
                         shmBuffer).update(this.libGLESv2,
                                           wlSurfaceResource,
                                           shmBuffer);

        //set the buffer in the shader
        this.libGLESv2.glActiveTexture(GL_TEXTURE0);
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

    private Gles2SurfaceData querySurfaceData(final WlSurfaceResource surfaceResource,
                                              final ShmBuffer shmBuffer) {
        Gles2SurfaceData surfaceData = this.cachedSurfaceData.get(surfaceResource);
        if (surfaceData == null) {
            surfaceData = Gles2SurfaceData.create(this.libGLESv2,
                                                  shmBuffer);
            this.cachedSurfaceData.put(surfaceResource,
                                       surfaceData);
        }
        else {
            final int surfaceDataWidth = surfaceData.getWidth();
            final int surfaceDataHeight = surfaceData.getHeight();
            final int bufferWidth = shmBuffer.getWidth();
            final int bufferHeight = shmBuffer.getHeight();
            if (surfaceDataWidth != bufferWidth || surfaceDataHeight != bufferHeight) {
                this.cachedSurfaceData.remove(surfaceResource)
                                      .delete(this.libGLESv2);
                surfaceData = querySurfaceData(surfaceResource,
                                               shmBuffer);
            }
        }
        return surfaceData;
    }

    private Gles2BufferFormat queryBufferFormat(final ShmBuffer buffer) {
        final int bufferFormat = buffer.getFormat();

        for (final Gles2BufferFormat gles2BufferFormat : Gles2BufferFormat.values()) {
            if (gles2BufferFormat.getWlShmFormat() == bufferFormat) {
                return gles2BufferFormat;
            }
        }

        throw new UnsupportedOperationException("Format " + buffer.getFormat() + " not supported.");
    }

    @Override
    public void end(@Nonnull final WlOutput wlOutput) {
        final HasEglOutput hasEglOutput = (HasEglOutput) wlOutput.getOutput()
                                                                 .getPlatformImplementation();
        hasEglOutput.getEglOutput()
                    .end();
    }
}
