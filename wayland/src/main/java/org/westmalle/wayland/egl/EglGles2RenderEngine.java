package org.westmalle.wayland.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Maps;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.westmalle.wayland.nativ.LibGLESv2;
import org.westmalle.wayland.nativ.NativeString;
import org.westmalle.wayland.output.Output;
import org.westmalle.wayland.output.OutputMode;
import org.westmalle.wayland.output.RenderEngine;
import org.westmalle.wayland.output.Surface;
import org.westmalle.wayland.output.calc.Mat4;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.westmalle.wayland.nativ.LibGLESv2.*;

@AutoFactory(className = "EglRenderEngineFactory")
public class EglGles2RenderEngine implements RenderEngine {

    @Nonnull
    private static final String SURFACE_V          =
            "uniform mat4 mu_projection;\n" +
            "\n" +
            "attribute mediump vec2 va_position;\n" +
            "attribute mediump vec2 va_texcoord;\n" +
            "\n" +
            "varying mediump vec2 vv_texcoord;\n" +
            "\n" +
            "void main(){\n" +
            "    vv_texcoord = va_texcoord;\n" +
            "    gl_Position = mu_projection * vec4(va_position, 0.0, 1.0) ;\n" +
            "}";
    @Nonnull
    private static final String SURFACE_ARGB8888_F =
            "varying mediump vec2 vv_texcoord;\n" +
            "uniform sampler2D tex;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(tex, vv_texcoord);\n" +
            "}";
    @Nonnull
    private static final String SURFACE_XRGB8888_F =
            "varying mediump vec2 vv_texcoord;\n" +
            "uniform sampler2D tex;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor.rgb = texture2D(tex, vv_texcoord).rgb;\n" +
            "    gl_FragColor.a = 1.;\n" +
            "}";

    @Nonnull
    private final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = new WeakHashMap<>();
    @Nonnull
    private final Map<Gles2BufferFormat, Integer>          shaderPrograms    = Maps.newHashMap();

    @Nonnull
    private final ExecutorService renderThread;
    @Nonnull
    private final LibGLESv2       libGLESv2;

    private Memory bufferData;
    private Memory elementBuffer;
    private Memory vertexBuffer;
    private Mat4   projection;

    EglGles2RenderEngine(@Provided @Nonnull final LibGLESv2 libGLESv2) {
        this.renderThread = Executors.newSingleThreadExecutor(r -> new Thread(r,
                                                                              "GL Render Engine"));
        this.libGLESv2 = libGLESv2;
    }

    @Override
    public void begin(@Nonnull final WlOutput wlOutput) {
        this.renderThread.submit(() -> doBegin(wlOutput));
    }

    private void doBegin(@Nonnull final WlOutput wlOutput) {
        final Output       output       = wlOutput.getOutput();
        final OutputMode   mode         = output.getMode();
        final HasEglOutput hasEglOutput = (HasEglOutput) output.getImplementation();
        final EglOutput    eglOutput    = hasEglOutput.getEglOutput();
        eglOutput.begin();

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
        this.libGLESv2.glClear(GL_COLOR_BUFFER_BIT);
        //define triangles to be drawn.
        //make element buffer active
        this.libGLESv2.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,
                                    getElementBuffer().getInt(0));

        this.libGLESv2.glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                                    getBufferData().size(),
                                    getBufferData(),
                                    GL_DYNAMIC_DRAW);
        //make vertexBuffer active
        this.libGLESv2.glBindBuffer(GL_ARRAY_BUFFER,
                                    getVertexBuffer().getInt(0));
    }

    @Nonnull
    private Memory getBufferData() {
        if (this.bufferData == null) {
            final int[] elements = new int[]{0, 1, 2,
                                             2, 3, 0
            };
            this.bufferData = new Memory(Integer.BYTES * elements.length);
            this.bufferData.write(0,
                                  elements,
                                  0,
                                  elements.length);
        }
        return this.bufferData;
    }

    @Nonnull
    private Memory getElementBuffer() {
        if (this.elementBuffer == null) {
            final Memory elementBuffer = new Memory(Integer.BYTES);
            this.libGLESv2.glGenBuffers(1,
                                        elementBuffer);
            this.elementBuffer = elementBuffer;
        }
        return this.elementBuffer;
    }

    @Nonnull
    private Memory getVertexBuffer() {
        if (this.vertexBuffer == null) {
            final Memory vertexBuffer = new Memory(Integer.BYTES);
            this.libGLESv2.glGenBuffers(1,
                                        vertexBuffer);
            this.vertexBuffer = vertexBuffer;
        }
        return this.vertexBuffer;
    }

    @Override
    public void draw(@Nonnull final WlSurfaceResource surfaceResource,
                     @Nonnull final WlBufferResource wlBufferResource) {
        this.renderThread.submit(() -> doDraw(surfaceResource,
                                              wlBufferResource));
    }

    private void doDraw(@Nonnull final WlSurfaceResource surfaceResource,
                        @Nonnull final WlBufferResource wlBufferResource) {
        final ShmBuffer buffer = ShmBuffer.get(wlBufferResource);
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer resource is not an ShmBuffer.");
        }

        final WlSurface implementation = (WlSurface) surfaceResource.getImplementation();
        final Surface   surface        = implementation.getSurface();
        //@formatter:off
        final float[] vertices = {
                0,                 0,                  0f, 0f,
                buffer.getWidth(), 0,                  1f, 0f,
                buffer.getWidth(), buffer.getHeight(), 1f, 1f,
                0,                 buffer.getHeight(), 0f, 1f
        };
        //@formatter:on

        buffer.beginAccess();
        querySurfaceData(surfaceResource,
                         buffer).makeActive(this.libGLESv2,
                                            buffer);
        buffer.endAccess();
        surface.firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));

        final int shaderProgram = queryShaderProgram(queryBufferFormat(buffer));
        configureShaders(shaderProgram,
                         this.projection.multiply(surface.getTransform()),
                         vertices);
        this.libGLESv2.glUseProgram(shaderProgram);
        this.libGLESv2.glDrawElements(GL_TRIANGLES,
                                      6,
                                      GL_UNSIGNED_INT,
                                      null);
    }

    private Gles2SurfaceData querySurfaceData(final WlSurfaceResource surfaceResource,
                                              final ShmBuffer buffer) {
        Gles2SurfaceData surfaceData = this.cachedSurfaceData.get(surfaceResource);
        if (surfaceData == null) {
            surfaceData = Gles2SurfaceData.create(this.libGLESv2);
            surfaceData.init(this.libGLESv2,
                             buffer);
            this.cachedSurfaceData.put(surfaceResource,
                                       surfaceData);
        }
        else {
            final int surfaceDataWidth = surfaceData.getWidth();
            final int surfaceDataHeight = surfaceData.getHeight();
            final int bufferWidth = buffer.getWidth();
            final int bufferHeight = buffer.getHeight();
            if (surfaceDataWidth != bufferWidth || surfaceDataHeight != bufferHeight) {
                surfaceData.destroy(this.libGLESv2);
                surfaceData = Gles2SurfaceData.create(this.libGLESv2);
                surfaceData.init(this.libGLESv2,
                                 buffer);
                this.cachedSurfaceData.put(surfaceResource,
                                           surfaceData);
            }
        }
        return surfaceData;
    }

    private Gles2BufferFormat queryBufferFormat(final ShmBuffer buffer) {
        final Gles2BufferFormat format;
        final int               bufferFormat = buffer.getFormat();
        if (bufferFormat == WlShmFormat.ARGB8888.getValue()) {
            format = Gles2BufferFormat.SHM_ARGB8888;
        }
        else if (bufferFormat == WlShmFormat.XRGB8888.getValue()) {
            format = Gles2BufferFormat.SHM_XRGB8888;
        }
        else {
            throw new UnsupportedOperationException("Format " + buffer.getFormat() + " not supported.");
        }
        return format;
    }

    private int queryShaderProgram(final Gles2BufferFormat bufferFormat) {
        Integer shaderProgram = this.shaderPrograms.get(bufferFormat);
        if (shaderProgram == null) {
            shaderProgram = createShaderProgram(bufferFormat);
            this.shaderPrograms.put(bufferFormat,
                                    shaderProgram);
        }
        return shaderProgram;
    }

    private int createShaderProgram(final Gles2BufferFormat bufferFormat) {
        final int vertexShader = this.libGLESv2.glCreateShader(GL_VERTEX_SHADER);
        compileShader(vertexShader,
                      SURFACE_V);

        final int fragmentShader;
        if (bufferFormat == Gles2BufferFormat.SHM_ARGB8888) {
            fragmentShader = this.libGLESv2.glCreateShader(GL_FRAGMENT_SHADER);
            compileShader(fragmentShader,
                          SURFACE_ARGB8888_F);
        }
        else if (bufferFormat == Gles2BufferFormat.SHM_XRGB8888) {
            fragmentShader = this.libGLESv2.glCreateShader(GL_FRAGMENT_SHADER);
            compileShader(fragmentShader,
                          SURFACE_XRGB8888_F);
        }
        else {
            throw new UnsupportedOperationException("Buffer format " + bufferFormat + " is not supported");
        }

        final int shaderProgram = this.libGLESv2.glCreateProgram();
        this.libGLESv2.glAttachShader(shaderProgram,
                                      vertexShader);
        this.libGLESv2.glAttachShader(shaderProgram,
                                      fragmentShader);
        this.libGLESv2.glLinkProgram(shaderProgram);
        return shaderProgram;
    }

    private void compileShader(final int shaderHandle,
                               final String shaderSource) {
        final Pointer      lines              = new Memory(Pointer.SIZE);
        final NativeString nativeShaderSource = new NativeString(shaderSource);
        lines.setPointer(0,
                         nativeShaderSource.getPointer());
        final Pointer lengths = new Memory(Integer.BYTES);
        lengths.setInt(0,
                       nativeShaderSource.length());
        this.libGLESv2.glShaderSource(shaderHandle,
                                      1,
                                      lines,
                                      lengths);
        this.libGLESv2.glCompileShader(shaderHandle);

        final Memory vstatus = new Memory(Integer.BYTES);
        this.libGLESv2.glGetShaderiv(shaderHandle,
                                     GL_COMPILE_STATUS,
                                     vstatus);
        if (vstatus.getInt(0) != LibGLESv2.GL_TRUE) {
            //failure!
            //get log length
            final Memory logLength = new Memory(Integer.BYTES);
            this.libGLESv2.glGetShaderiv(shaderHandle,
                                         GL_INFO_LOG_LENGTH,
                                         logLength);
            //get log
            int logSize = logLength.getInt(0);
            if (logSize == 0) {
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

        this.libGLESv2.glEnableVertexAttribArray(posAttrib);
        this.libGLESv2.glVertexAttribPointer(posAttrib,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             4 * Float.BYTES,
                                             null);

        this.libGLESv2.glEnableVertexAttribArray(texAttrib);
        this.libGLESv2.glVertexAttribPointer(texAttrib,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             4 * Float.BYTES,
                                             Pointer.createConstant(2 * Float.BYTES));
    }

    @Nonnull
    @Override
    public Future<?> end(@Nonnull final WlOutput wlOutput) {
        return this.renderThread.submit(() -> doEnd(wlOutput));
    }

    private void doEnd(@Nonnull final WlOutput wlOutput) {
        final HasEglOutput hasEglOutput = (HasEglOutput) wlOutput.getOutput()
                                                                 .getImplementation();
        hasEglOutput.getEglOutput()
                    .end();
    }
}
