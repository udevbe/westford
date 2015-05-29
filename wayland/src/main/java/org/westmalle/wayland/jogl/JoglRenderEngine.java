//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.jogl;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.output.Point;
import org.westmalle.wayland.output.RenderEngine;
import org.westmalle.wayland.output.Surface;
import org.westmalle.wayland.output.calc.Mat4;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.freedesktop.wayland.shared.WlShmFormat.ARGB8888;
import static org.freedesktop.wayland.shared.WlShmFormat.XRGB8888;

public class JoglRenderEngine implements RenderEngine {

    @Nonnull
    private static final String SURFACE_V          =
            "uniform mat4 mu_projection;\n" +
            "\n" +
            "attribute vec2 va_position;\n" +
            "attribute vec2 va_texcoord;\n" +
            "\n" +
            "varying vec2 vv_texcoord;\n" +
            "\n" +
            "void main(){\n" +
            "    vv_texcoord = va_texcoord;\n" +
            "    gl_Position = vec4(va_position, 0.0, 1.0) * mu_projection;\n" +
            "}";
    @Nonnull
    private static final String SURFACE_ARGB8888_F =
            "varying vec2 vv_texcoord;\n" +
            "uniform sampler2D tex;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(tex, vv_texcoord);\n" +
            "}";
    @Nonnull
    private static final String SURFACE_XRGB8888_F =
            "varying vec2 vv_texcoord;\n" +
            "uniform sampler2D tex;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor.rgb = texture2D(tex, vv_texcoord).rgb;\n" +
            "    gl_FragColor.a = 1.;\n" +
            "}";

    @Nonnull
    private final Map<WlSurfaceResource, JoglSurfaceData> cachedSurfaceData = new WeakHashMap<>();
    @Nonnull
    private final Map<JoglBufferFormat, Integer>          shaderPrograms    = Maps.newHashMap();

    @Nonnull
    private final ExecutorService renderThread;
    @Nonnull
    private final GLContext       glContext;
    @Nonnull
    private final IntBuffer       elementBuffer;
    @Nonnull
    private final IntBuffer       vertexBuffer;
    @Nonnull
    private final int[] elements = new int[]{
            0,
            1,
            2,
            2,
            3,
            0
    };

    @Nonnull
    private Optional<Mat4>   projection = Optional.empty();
    @Nonnull
    private Optional<GL2ES2> gl         = Optional.empty();

    JoglRenderEngine(@Nonnull final ExecutorService renderThread,
                     @Nonnull final GLContext glContext,
                     @Nonnull final IntBuffer elementBuffer,
                     @Nonnull final IntBuffer vertexBuffer) {
        this.renderThread = renderThread;
        this.glContext = glContext;
        this.elementBuffer = elementBuffer;
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public void begin(@Nonnull final WlOutput wlOutput) {
        this.renderThread.submit(() -> doBegin(wlOutput));
    }

    private void doBegin(@Nonnull final WlOutput wlOutput) {
        final GLWindow drawable = (GLWindow) wlOutput.getOutput().getImplementation();
        final int surfaceWidth  = drawable.getSurfaceWidth();
        final int surfaceHeight = drawable.getSurfaceHeight();
        //@formatter:off
        this.projection = Optional.of(Mat4.create(2.0f / surfaceWidth, 0,                     0, -1,
                                                  0,                   2.0f / -surfaceHeight, 0,  1,
                                                  0,                   0,                     1,  0,
                                                  0,                   0,                     0,  1));
        //@formatter:on
        refreshGl();
        final GL2ES2 gl2ES2 = this.gl.get();
        gl2ES2.glViewport(0,
                          0,
                          surfaceWidth,
                          surfaceHeight);
        gl2ES2.glClear(GL.GL_COLOR_BUFFER_BIT);
        //define triangles to be drawn.
        //make element buffer active
        gl2ES2.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER,
                            this.elementBuffer.get(0));
        gl2ES2.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER,
                            4 * this.elements.length,
                            Buffers.newDirectIntBuffer(this.elements),
                            GL.GL_DYNAMIC_DRAW);
        //make vertexBuffer active
        gl2ES2.glBindBuffer(GL.GL_ARRAY_BUFFER,
                            this.vertexBuffer.get(0));
    }

    @Override
    public void draw(@Nonnull final WlSurfaceResource surfaceResource,
                     @Nonnull final WlBufferResource wlBufferResource) {
        this.renderThread.submit(() -> doDraw(surfaceResource,
                                              wlBufferResource));
    }

    private void doDraw(final WlSurfaceResource surfaceResource,
                        final WlBufferResource wlBufferResource) {
        final ShmBuffer buffer = ShmBuffer.get(wlBufferResource);
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer resource is not an ShmBuffer.");
        }

        final WlSurface implementation = (WlSurface) surfaceResource.getImplementation();
        final Surface   surface        = implementation.getSurface();
        final Point     position       = surface.getPosition();
        final float[] vertices = {
                position.getX(), position.getY(), 0f, 0f,
                position.getX() + buffer.getWidth(), position.getY(), 1f, 0f,
                position.getX() + buffer.getWidth(), position.getY() + buffer.getHeight(), 1f, 1f,
                position.getX(), position.getY() + buffer.getHeight(), 0f, 1f
        };

        buffer.beginAccess();
        final GL2ES2 gl2ES2 = this.gl.get();
        querySurfaceData(surfaceResource,
                         buffer).makeActive(gl2ES2,
                                            buffer);
        buffer.endAccess();
        surface.firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));

        final int shaderProgram = queryShaderProgram(queryBufferFormat(buffer));
        configureShaders(shaderProgram,
                         this.projection.get(),
                         vertices);
        gl2ES2.glUseProgram(shaderProgram);
        gl2ES2.glDrawElements(GL.GL_TRIANGLES,
                              6,
                              GL.GL_UNSIGNED_INT,
                              0);
    }

    @Nonnull
    @Override
    public Future<?> end(@Nonnull final WlOutput wlOutput) {
        return this.renderThread.submit(() -> doEnd(wlOutput));
    }

    private void doEnd(@Nonnull final WlOutput wlOutput) {
        final GLWindow drawable = (GLWindow) wlOutput.getOutput().getImplementation();
        drawable.swapBuffers();
        this.gl = Optional.empty();
        this.projection = Optional.empty();
    }

    private int queryShaderProgram(final JoglBufferFormat bufferFormat) {
        Integer shaderProgram = this.shaderPrograms.get(bufferFormat);
        if (shaderProgram == null) {
            shaderProgram = createShaderProgram(bufferFormat);
            this.shaderPrograms.put(bufferFormat,
                                    shaderProgram);
        }
        return shaderProgram;
    }

    private int createShaderProgram(final JoglBufferFormat bufferFormat) {
        final GL2ES2 gl2ES2       = this.gl.get();
        final int    vertexShader = gl2ES2.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        compileShader(vertexShader,
                      SURFACE_V);

        final int fragmentShader;
        if (bufferFormat == JoglBufferFormat.SHM_ARGB8888) {
            fragmentShader = gl2ES2.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
            compileShader(fragmentShader,
                          SURFACE_ARGB8888_F);
        }
        else if (bufferFormat == JoglBufferFormat.SHM_XRGB8888) {
            fragmentShader = gl2ES2.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
            compileShader(fragmentShader,
                          SURFACE_XRGB8888_F);
        }
        else {
            throw new UnsupportedOperationException("Buffer format " + bufferFormat + " is not supported");
        }

        final int shaderProgram = gl2ES2.glCreateProgram();
        gl2ES2.glAttachShader(shaderProgram,
                              vertexShader);
        gl2ES2.glAttachShader(shaderProgram,
                              fragmentShader);
        gl2ES2.glLinkProgram(shaderProgram);
        return shaderProgram;
    }

    private void compileShader(final int shaderHandle,
                               final String shaderSource) {
        final String[] lines   = new String[]{shaderSource};
        final int[]    lengths = new int[]{lines[0].length()};
        final GL2ES2   gl2ES2  = this.gl.get();
        gl2ES2.glShaderSource(shaderHandle,
                              lines.length,
                              lines,
                              lengths,
                              0);
        gl2ES2.glCompileShader(shaderHandle);

        final IntBuffer vstatus = IntBuffer.allocate(1);
        gl2ES2.glGetShaderiv(shaderHandle,
                             GL2ES2.GL_COMPILE_STATUS,
                             vstatus);
        if (vstatus.get(0) != GL.GL_TRUE) {
            //failure!
            //get log length
            final int[] logLength = new int[1];
            gl2ES2.glGetShaderiv(shaderHandle,
                                 GL2ES2.GL_INFO_LOG_LENGTH,
                                 logLength,
                                 0);
            //get log
            if (logLength[0] == 0) {
                logLength[0] = 1024;
            }
            final byte[] log = new byte[logLength[0]];
            gl2ES2.glGetShaderInfoLog(shaderHandle,
                                      logLength[0],
                                      null,
                                      0,
                                      log,
                                      0);
            System.err.println("Error compiling the vertex shader: " + new String(log));
            System.exit(1);
        }
    }

    private void configureShaders(final Integer program,
                                  final Mat4 projection,
                                  final float[] vertices) {
        final GL2ES2 gl2ES2 = this.gl.get();
        final int uniTrans = gl2ES2.glGetUniformLocation(program,
                                                         "mu_projection");
        gl2ES2.glUniformMatrix4fv(uniTrans,
                                  1,
                                  false,
                                  projection.toBuffer());

        gl2ES2.glBufferData(GL.GL_ARRAY_BUFFER,
                            vertices.length * 4,
                            Buffers.newDirectFloatBuffer(vertices),
                            GL.GL_DYNAMIC_DRAW);
        final int posAttrib = gl2ES2.glGetAttribLocation(program,
                                                         "va_position");
        final int texAttrib = gl2ES2.glGetAttribLocation(program,
                                                         "va_texcoord");
        gl2ES2.glEnableVertexAttribArray(posAttrib);
        gl2ES2.glVertexAttribPointer(posAttrib,
                                     2,
                                     GL.GL_FLOAT,
                                     false,
                                     4 * 4,
                                     0);
        gl2ES2.glEnableVertexAttribArray(texAttrib);
        gl2ES2.glVertexAttribPointer(texAttrib,
                                     2,
                                     GL.GL_FLOAT,
                                     false,
                                     4 * 4,
                                     2 * 4);
    }

    private void refreshGl() {
        this.gl = Optional.of(this.glContext.getGL()
                                            .getGL2ES2());
    }

    private JoglBufferFormat queryBufferFormat(final ShmBuffer buffer) {
        final JoglBufferFormat format;
        final int              bufferFormat = buffer.getFormat();
        if (bufferFormat == ARGB8888.getValue()) {
            format = JoglBufferFormat.SHM_ARGB8888;
        }
        else if (bufferFormat == XRGB8888.getValue()) {
            format = JoglBufferFormat.SHM_XRGB8888;
        }
        else {
            throw new UnsupportedOperationException("Format " + buffer.getFormat() + " not supported.");
        }
        return format;
    }

    private JoglSurfaceData querySurfaceData(final WlSurfaceResource surfaceResource,
                                             final ShmBuffer buffer) {
        final GL2ES2    gl2ES2      = this.gl.get();
        JoglSurfaceData surfaceData = this.cachedSurfaceData.get(surfaceResource);
        if (surfaceData == null) {
            surfaceData = JoglSurfaceData.create(gl2ES2);
            surfaceData.init(gl2ES2,
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
                surfaceData.destroy(gl2ES2);

                surfaceData = JoglSurfaceData.create(gl2ES2);
                surfaceData.init(gl2ES2,
                                 buffer);
                this.cachedSurfaceData.put(surfaceResource,
                                           surfaceData);
            }
        }
        return surfaceData;
    }
}
