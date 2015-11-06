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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ARRAY_BUFFER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COLOR_BUFFER_BIT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COMPILE_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_DYNAMIC_DRAW;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ELEMENT_ARRAY_BUFFER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FLOAT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FRAGMENT_SHADER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_INFO_LOG_LENGTH;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_LINK_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TRIANGLES;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_VERTEX_SHADER;

@Singleton
public class EglGles2RenderEngine implements RenderEngine {

    @Nonnull
    static final String SURFACE_V          = "uniform mat4 mu_projection;\n" +
                                             "\n" +
                                             "attribute vec2 va_position;\n" +
                                             "attribute vec2 va_texcoord;\n" +
                                             "attribute mat4 va_transform;\n" +
                                             "\n" +
                                             "varying vec2 vv_texcoord;\n" +
                                             "\n" +
                                             "void main(){\n" +
                                             "    vv_texcoord = va_texcoord;\n" +
                                             "    gl_Position = mu_projection * va_transform * vec4(va_position, 0.0, 1.0) ;\n" +
                                             "}";
    @Nonnull
    static final String SURFACE_ARGB8888_F = "precision mediump float;\n" +
                                             "varying vec2 vv_texcoord;\n" +
                                             "uniform sampler2D tex;\n" +
                                             "\n" +
                                             "void main(){\n" +
                                             "    gl_FragColor = texture2D(tex, vv_texcoord);\n" +
                                             "}";
    @Nonnull
    static final String SURFACE_XRGB8888_F = "precision mediump float;\n" +
                                             "varying vec2 vv_texcoord;\n" +
                                             "uniform sampler2D tex;\n" +
                                             "\n" +
                                             "void main() {\n" +
                                             "    gl_FragColor.rgb = texture2D(tex, vv_texcoord).rgb;\n" +
                                             "    gl_FragColor.a = 1.;\n" +
                                             "}";

    @Nonnull
    private final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = new WeakHashMap<>();
    @Nonnull
    private final Map<Gles2BufferFormat, Integer>          shaderPrograms    = new HashMap<>();

    @Nonnull
    private final LibGLESv2 libGLESv2;
    @Nonnull
    private final Memory elementBuffer = new Memory(Integer.BYTES);

    @Nonnull
    private final byte[] elements          = new byte[]{0, 1, 2,
                                                        2, 3, 0};
    @Nonnull
    private final Memory elementBufferData = new Memory(Byte.BYTES * this.elements.length);
    @Nonnull
    private final Memory vertexBuffer      = new Memory(Integer.BYTES);

    private boolean init = false;

    private Mat4 projection;

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

        //TODO try to make this more eager and don't depend on an init flag
        if (!this.init) {
            init();
        }

        final int surfaceWidth  = mode.getWidth();
        final int surfaceHeight = mode.getHeight();
        //@formatter:off
        this.projection = Mat4.create(2.0f / surfaceWidth, 0,                     0, -1,
                                      0,                   2.0f / -surfaceHeight, 0,  1,
                                      0,                   0,                     1,  0,
                                      0,                   0,                     0,  1);
        //@formatter:on
        this.libGLESv2.glViewport(0,
                                  0,
                                  surfaceWidth,
                                  surfaceHeight);
        this.libGLESv2.glClearColor(1.0f,
                                    0.0f,
                                    0.0f,
                                    1.0f);
        this.libGLESv2.glClear(GL_COLOR_BUFFER_BIT);
        //define triangles to be drawn.
        //make element buffer active
        this.libGLESv2.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,
                                    this.elementBuffer.getInt(0));

        this.libGLESv2.glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                                    this.elementBufferData.size(),
                                    this.elementBufferData,
                                    GL_DYNAMIC_DRAW);
        //make vertexBuffer active
        this.libGLESv2.glBindBuffer(GL_ARRAY_BUFFER,
                                    this.vertexBuffer.getInt(0));
    }

    private void init() {
        this.libGLESv2.glGenBuffers(1,
                                    this.elementBuffer);
        this.elementBufferData.write(0,
                                     this.elements,
                                     0,
                                     this.elements.length);
        this.libGLESv2.glGenBuffers(1,
                                    this.vertexBuffer);

        for (final Gles2BufferFormat gles2BufferFormat : Gles2BufferFormat.values()) {
            this.shaderPrograms.put(gles2BufferFormat,
                                    createShaderProgram(gles2BufferFormat));
        }

        this.init = true;
    }

    private int createShaderProgram(final Gles2BufferFormat bufferFormat) {
        final int vertexShader = this.libGLESv2.glCreateShader(GL_VERTEX_SHADER);
        compileShader(vertexShader,
                      bufferFormat.getVertexShader());
        final int fragmentShader = this.libGLESv2.glCreateShader(GL_FRAGMENT_SHADER);
        compileShader(fragmentShader,
                      bufferFormat.getFragmentShader());

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

        return shaderProgram;
    }

    private void compileShader(final int shaderHandle,
                               final String shaderSource) {
        final Pointer      shadersSourcePointer = new Memory(Pointer.SIZE);
        final NativeString nativeShaderSource   = new NativeString(shaderSource);
        shadersSourcePointer.setPointer(0,
                                        nativeShaderSource.getPointer());
        this.libGLESv2.glShaderSource(shaderHandle,
                                      1,
                                      shadersSourcePointer,
                                      null);
        this.libGLESv2.glCompileShader(shaderHandle);

        final Memory vstatus = new Memory(Integer.BYTES);
        this.libGLESv2.glGetShaderiv(shaderHandle,
                                     GL_COMPILE_STATUS,
                                     vstatus);
        if (vstatus.getInt(0) == 0) {
            //failure!
            //get log length
            final Memory logLength = new Memory(Integer.BYTES);
            this.libGLESv2.glGetShaderiv(shaderHandle,
                                         GL_INFO_LOG_LENGTH,
                                         logLength);
            //get log
            int logSize = logLength.getInt(0);
            if (logSize == 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Memory log = new Memory(logSize);
            this.libGLESv2.glGetShaderInfoLog(shaderHandle,
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
        final Mat4      transform = surface.getTransform();
        //@formatter:off
        final float[] vertices = {
                //top left:
                //vec2 va_position
                0, 0,
                //vec2 va_texcoord
                0f, 0f,
                //mat4 va_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //top right:
                shmBuffer.getWidth(), 0,
                1f, 0f,
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //bottom right:
                shmBuffer.getWidth(), shmBuffer.getHeight(),
                1f, 1f,
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //bottom left:
                0, shmBuffer.getHeight(),
                0f, 1f,
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),
        };
        //@formatter:on
        shmBuffer.beginAccess();
        querySurfaceData(wlSurfaceResource,
                         shmBuffer).makeActive(this.libGLESv2,
                                               shmBuffer);
        shmBuffer.endAccess();
        surface.firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));

        final int shaderProgram = this.shaderPrograms.get(queryBufferFormat(shmBuffer));
        this.libGLESv2.glUseProgram(shaderProgram);
        //TODO move setting of uniform projection matrix to begin().
        configureShaders(shaderProgram,
                         this.projection,
                         vertices);
        this.libGLESv2.glDrawElements(GL_TRIANGLES,
                                      6,
                                      GL_UNSIGNED_BYTE,
                                      null);
    }

    private Gles2SurfaceData querySurfaceData(final WlSurfaceResource surfaceResource,
                                              final ShmBuffer shmBuffer) {
        Gles2SurfaceData surfaceData = this.cachedSurfaceData.get(surfaceResource);
        if (surfaceData == null) {
            surfaceData = Gles2SurfaceData.create(this.libGLESv2);
            surfaceData.init(this.libGLESv2,
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
                surfaceData.destroy(this.libGLESv2);
                surfaceData = Gles2SurfaceData.create(this.libGLESv2);
                surfaceData.init(this.libGLESv2,
                                 shmBuffer);
                this.cachedSurfaceData.put(surfaceResource,
                                           surfaceData);
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

    private void configureShaders(final Integer program,
                                  final Mat4 projection,
                                  final float[] vertices) {
        final int uniTrans = this.libGLESv2.glGetUniformLocation(program,
                                                                 new NativeString("mu_projection").getPointer());
        final Pointer projectionBuffer = new Memory(Float.BYTES * 16);
        projectionBuffer.write(0,
                               projection.toArray(),
                               0,
                               16);
        this.libGLESv2.glUniformMatrix4fv(uniTrans,
                                          1,
                                          false,
                                          projectionBuffer);

        final Memory verticesBuffer = new Memory(Float.BYTES * vertices.length);
        verticesBuffer.write(0,
                             vertices,
                             0,
                             vertices.length);
        this.libGLESv2.glBufferData(GL_ARRAY_BUFFER,
                                    vertices.length * Float.BYTES,
                                    verticesBuffer,
                                    GL_DYNAMIC_DRAW);

        final int posAttrib = this.libGLESv2.glGetAttribLocation(program,
                                                                 new NativeString("va_position").getPointer());
        final int texAttrib = this.libGLESv2.glGetAttribLocation(program,
                                                                 new NativeString("va_texcoord").getPointer());
        final int transAttrib = this.libGLESv2.glGetAttribLocation(program,
                                                                   new NativeString("va_transform").getPointer());

        this.libGLESv2.glEnableVertexAttribArray(posAttrib);
        this.libGLESv2.glVertexAttribPointer(posAttrib,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             null);

        this.libGLESv2.glEnableVertexAttribArray(texAttrib);
        this.libGLESv2.glVertexAttribPointer(texAttrib,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             new Pointer(2 * Float.BYTES));

        //column 0
        this.libGLESv2.glEnableVertexAttribArray(transAttrib);
        this.libGLESv2.glVertexAttribPointer(transAttrib,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             new Pointer(4 * Float.BYTES));
        //column 1
        this.libGLESv2.glEnableVertexAttribArray(transAttrib + 1);
        this.libGLESv2.glVertexAttribPointer(transAttrib + 1,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             new Pointer(8 * Float.BYTES));
        //column 2
        this.libGLESv2.glEnableVertexAttribArray(transAttrib + 2);
        this.libGLESv2.glVertexAttribPointer(transAttrib + 2,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             new Pointer(12 * Float.BYTES));
        //column 3
        this.libGLESv2.glEnableVertexAttribArray(transAttrib + 3);
        this.libGLESv2.glVertexAttribPointer(transAttrib + 3,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             new Pointer(16 * Float.BYTES));
    }

    @Override
    public void end(@Nonnull final WlOutput wlOutput) {
        final HasEglOutput hasEglOutput = (HasEglOutput) wlOutput.getOutput()
                                                                 .getPlatformImplementation();
        hasEglOutput.getEglOutput()
                    .end();
    }
}
