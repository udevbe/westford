package org.westmalle.wayland.egl;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.core.RenderEngine;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.nativ.NativeString;
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
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE0;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TRIANGLES;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_VERTEX_SHADER;

@Singleton
public class EglGles2RenderEngine implements RenderEngine {

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
    EglGles2RenderEngine(@Nonnull final LibGLESv2 libGLESv2) {
        this.libGLESv2 = libGLESv2;
    }

    @Override
    public void begin(@Nonnull final WlOutput wlOutput) {
        final Output       output       = wlOutput.getOutput();
        final OutputMode   mode         = output.getMode();
        final HasEglOutput hasEglOutput = (HasEglOutput) output.getPlatformImplementation();
        final EglOutput    eglOutput    = hasEglOutput.getEglOutput();
        eglOutput.begin();

        if (!this.init) {
            init();
        }

        final int width  = mode.getWidth();
        final int height = mode.getHeight();

        this.libGLESv2.glViewport(0,
                                  0,
                                  width,
                                  height);

        this.libGLESv2.glClearColor(0.0f,
                                    0.0f,
                                    0.0f,
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
        final String extensions = this.libGLESv2.glGetString(GL_EXTENSIONS)
                                                .getString(0);

        LOGGER.info("GLESv2 extensions: " + extensions);
        if (!extensions.contains("GL_EXT_texture_format_BGRA8888")) {
            LOGGER.severe("Required extension GL_EXT_texture_format_BGRA8888 not available");
            System.exit(1);
        }

        for (final Gles2BufferFormat gles2BufferFormat : Gles2BufferFormat.values()) {
            this.shaderPrograms.put(gles2BufferFormat,
                                    createShaderProgram(gles2BufferFormat));
        }

        this.init = true;
    }

    private int createShaderProgram(final Gles2BufferFormat bufferFormat) {
        //vertex shader
        final int     vertexShader       = this.libGLESv2.glCreateShader(GL_VERTEX_SHADER);
        final Pointer vertexShaderSource = new NativeString(bufferFormat.getVertexShaderSource()).getPointer();
        final Pointer vertexShaders      = new Memory(Pointer.SIZE);
        vertexShaders.setPointer(0,
                                 vertexShaderSource);
        this.libGLESv2.glShaderSource(vertexShader,
                                      1,
                                      vertexShaders,
                                      null);
        this.libGLESv2.glCompileShader(vertexShader);

        checkShader(vertexShader);

        //fragment shader
        final int     fragmentShader       = this.libGLESv2.glCreateShader(GL_FRAGMENT_SHADER);
        final Pointer fragmentShaderSource = new NativeString(bufferFormat.getFragmentShaderSource()).getPointer();
        final Pointer fragmentShaders      = new Memory(Pointer.SIZE);
        fragmentShaders.setPointer(0,
                                   fragmentShaderSource);
        this.libGLESv2.glShaderSource(fragmentShader,
                                      1,
                                      fragmentShaders,
                                      null);
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
        final Pointer linked = new Memory(Integer.BYTES);
        this.libGLESv2.glGetProgramiv(shaderProgram,
                                      GL_LINK_STATUS,
                                      linked);
        if (linked.getInt(0) == 0) {
            final Pointer infoLen = new Memory(Integer.BYTES);
            this.libGLESv2.glGetProgramiv(shaderProgram,
                                          GL_INFO_LOG_LENGTH,
                                          infoLen);
            int logSize = infoLen.getInt(0);
            if (logSize <= 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Memory log = new Memory(logSize);
            this.libGLESv2.glGetProgramInfoLog(shaderProgram,
                                               logSize,
                                               null,
                                               log);
            this.libGLESv2.glDeleteProgram(shaderProgram);
            System.err.println("Error compiling the vertex shader: " + log.getString(0));
            System.exit(1);
        }

        //find shader arguments
        final Memory u_projection = new NativeString("u_projection").getPointer();
        this.projectionArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                 u_projection);
        final Memory u_transform = new NativeString("u_transform").getPointer();
        this.transformArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                u_transform);

        final Memory a_position = new NativeString("a_position").getPointer();
        this.positionArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                              a_position);

        final Memory a_texCoord = new NativeString("a_texCoord").getPointer();
        this.textureCoordinateArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                                       a_texCoord);

        final Memory u_texture = new NativeString("u_texture").getPointer();
        this.textureArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                              u_texture);

        return shaderProgram;
    }

    private void checkShader(final int shader) {
        final Memory vstatus = new Memory(Integer.BYTES);
        this.libGLESv2.glGetShaderiv(shader,
                                     GL_COMPILE_STATUS,
                                     vstatus);
        if (vstatus.getInt(0) == 0) {
            //failure!
            //get log length
            final Memory logLength = new Memory(Integer.BYTES);
            this.libGLESv2.glGetShaderiv(shader,
                                         GL_INFO_LOG_LENGTH,
                                         logLength);
            //get log
            int logSize = logLength.getInt(0);
            if (logSize == 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Memory log = new Memory(logSize);
            this.libGLESv2.glGetShaderInfoLog(shader,
                                              logSize,
                                              null,
                                              log);
            System.err.println("Error compiling the vertex shader: " + log.getString(0));
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

        final int bufferWidth  = shmBuffer.getStride() / 4;
        final int bufferHeight = shmBuffer.getHeight();

        //define vertex data
        final float[] vertexDataValues = {
                //top left:
                //attribute vec2 a_position
                0f, 0f,
                //attribute vec2 a_texCoord
                0f, 0f,

                //top right:
                //attribute vec2 a_position
                bufferWidth, 0f,
                //attribute vec2 a_texCoord
                1f, 0f,

                //bottom right:
                //vec2 a_position
                bufferWidth, bufferHeight,
                //vec2 a_texCoord
                1f, 1f,

                //bottom right:
                //vec2 a_position
                bufferWidth, bufferHeight,
                //vec2 a_texCoord
                1f, 1f,

                //bottom left:
                //vec2 a_position
                0f, bufferHeight,
                //vec2 a_texCoord
                0f, 1f,

                //top left:
                //attribute vec2 a_position
                0f, 0f,
                //attribute vec2 a_texCoord
                0f, 0f
        };
        final Memory vertexData = new Memory(Float.BYTES * vertexDataValues.length);
        vertexData.write(0,
                         vertexDataValues,
                         0,
                         vertexDataValues.length);

        //activate shader
        this.libGLESv2.glUseProgram(this.shaderPrograms.get(queryBufferFormat(shmBuffer)));

        //upload uniform data
        final int     projectionSize   = this.projection.length;
        final Pointer projectionBuffer = new Memory(Float.BYTES * projectionSize);
        projectionBuffer.write(0,
                               this.projection,
                               0,
                               projectionSize);
        this.libGLESv2.glUniformMatrix4fv(this.projectionArg,
                                          1,
                                          false,
                                          projectionBuffer);

        final int     transformSize   = transform.length;
        final Pointer transformBuffer = new Memory(Float.BYTES * transformSize);
        transformBuffer.write(0,
                              transform,
                              0,
                              transformSize);
        this.libGLESv2.glUniformMatrix4fv(this.transformArg,
                                          1,
                                          false,
                                          transformBuffer);

        //set vertex data in shader
        this.libGLESv2.glEnableVertexAttribArray(this.positionArg);
        this.libGLESv2.glVertexAttribPointer(this.positionArg,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             4 * Float.BYTES,
                                             vertexData);

        this.libGLESv2.glEnableVertexAttribArray(this.textureCoordinateArg);
        this.libGLESv2.glVertexAttribPointer(this.textureCoordinateArg,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             4 * Float.BYTES,
                                             vertexData.share(2 * Float.BYTES));

        querySurfaceData(wlSurfaceResource,
                         shmBuffer).update(this.libGLESv2,
                                           wlSurfaceResource,
                                           shmBuffer);

        //set the buffer in the shader
        this.libGLESv2.glActiveTexture(GL_TEXTURE0);
        this.libGLESv2.glUniform1i(this.textureArg,
                                   0);

        //draw
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
