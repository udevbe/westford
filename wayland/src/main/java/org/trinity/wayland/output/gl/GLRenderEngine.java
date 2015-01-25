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
package org.trinity.wayland.output.gl;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.hackoeur.jglm.Mat4;
import com.jogamp.common.nio.Buffers;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.trinity.wayland.output.ShmRenderEngine;
import org.trinity.wayland.output.Surface;
import org.trinity.wayland.protocol.WlSurface;

import javax.media.nativewindow.util.PointImmutable;
import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import java.nio.IntBuffer;
import java.util.Map;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static org.trinity.wayland.output.gl.GLBufferFormat.SHM_ARGB8888;
import static org.trinity.wayland.output.gl.GLBufferFormat.SHM_XRGB8888;

public class GLRenderEngine implements ShmRenderEngine {

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
    private static final String SURFACE_ARGB8888_F =
            "varying vec2 vv_texcoord;\n" +
            "uniform sampler2D tex;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(tex, vv_texcoord);\n" +
            "}";
    private static final String SURFACE_XRGB8888_F =
            "varying vec2 vv_texcoord;\n" +
            "uniform sampler2D tex;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor.rgb = texture2D(tex, vv_texcoord).rgb;\n" +
            "    gl_FragColor.a = 1f;\n" +
            "}";

    private final Map<WlSurfaceResource, GLSurfaceData> cachedSurfaceData = Maps.newHashMap();
    private final Map<GLBufferFormat, Integer>          shaderPrograms    = Maps.newHashMap();

    private final ListeningExecutorService renderThread;
    private final GLAutoDrawable           drawable;
    private final IntBuffer                elementBuffer;
    private final IntBuffer                vertexBuffer;
    private final int[] elements = new int[]{
            0,
            1,
            2,
            2,
            3,
            0
    };

    private Mat4   projection;
    private GL2ES2 gl;

    GLRenderEngine(final ListeningExecutorService renderThread,
                   final GLAutoDrawable drawable,
                   final IntBuffer elementBuffer,
                   final IntBuffer vertexBuffer) {
        this.renderThread = renderThread;
        this.drawable = drawable;
        this.elementBuffer = elementBuffer;
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public ListenableFuture<?> begin() {
        return this.renderThread.submit((Runnable) this::doBegin);
    }

    private void doBegin() {
        this.projection = new Mat4(2.0f / this.drawable.getSurfaceWidth(),
                                   0,
                                   0,
                                   -1,

                                   0,
                                   2.0f / -this.drawable.getSurfaceHeight(),
                                   0,
                                   1,

                                   0,
                                   0,
                                   1,
                                   0,

                                   0,
                                   0,
                                   0,
                                   1);
        refreshGl();
        this.gl.glClear(GL_COLOR_BUFFER_BIT);
        //define triangles to be drawn.
        //make element buffer active
        this.gl.glBindBuffer(GL2ES2.GL_ELEMENT_ARRAY_BUFFER,
                             this.elementBuffer.get(0));
        this.gl.glBufferData(GL2ES2.GL_ELEMENT_ARRAY_BUFFER,
                             4 * this.elements.length,
                             Buffers.newDirectIntBuffer(this.elements),
                             GL2ES2.GL_DYNAMIC_DRAW);
        //make vertexBuffer active
        this.gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER,
                             this.vertexBuffer.get(0));
    }

    @Override
    public ListenableFuture<?> draw(final WlSurfaceResource surfaceResource,
                                    final ShmBuffer buffer) {
        return this.renderThread.submit(() -> doDraw(surfaceResource,
                                                     buffer));
    }

    private void doDraw(final WlSurfaceResource surfaceResource,
                        final ShmBuffer buffer) {

        buffer.beginAccess();
        final WlSurface implementation = (WlSurface) surfaceResource.getImplementation();
        final Surface surface = implementation.getSurface();
        final PointImmutable position = surface.getPosition();
        final float[] vertices = {
                position.getX(), position.getY(), 0f, 0f,
                position.getX() + buffer.getWidth(), position.getY(), 1f, 0f,
                position.getX() + buffer.getWidth(), position.getY() + buffer.getHeight(), 1f, 1f,
                position.getX(), position.getY() + buffer.getHeight(), 0f, 1f
        };

        querySurfaceData(surfaceResource,
                         buffer).makeActive(this.gl,
                                            buffer);

        final int shaderProgram = queryShaderProgram(queryBufferFormat(buffer));
        configureShaders(shaderProgram,
                         this.projection,
                         vertices);
        this.gl.glUseProgram(shaderProgram);
        this.gl.glDrawElements(GL.GL_TRIANGLES,
                               6,
                               GL.GL_UNSIGNED_INT,
                               0);
        buffer.endAccess();
    }

    @Override
    public ListenableFuture<?> end() {
        return this.renderThread.submit((Runnable) this.drawable::swapBuffers);
    }

    private int queryShaderProgram(final GLBufferFormat bufferFormat) {
        Integer shaderProgram = this.shaderPrograms.get(bufferFormat);
        if (shaderProgram == null) {
            shaderProgram = createShaderProgram(bufferFormat);
            this.shaderPrograms.put(bufferFormat,
                                    shaderProgram);
        }
        return shaderProgram;
    }

    private int createShaderProgram(final GLBufferFormat bufferFormat) {
        final int vertexShader = this.gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        compileShader(vertexShader,
                      SURFACE_V);

        final int fragmentShader;
        if (bufferFormat == SHM_ARGB8888) {
            fragmentShader = this.gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
            compileShader(fragmentShader,
                          SURFACE_ARGB8888_F);
        }
        else if (bufferFormat == SHM_XRGB8888) {
            fragmentShader = this.gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
            compileShader(fragmentShader,
                          SURFACE_XRGB8888_F);
        }
        else {
            throw new UnsupportedOperationException("Buffer format " + bufferFormat + " is not supported");
        }

        final int shaderProgram = this.gl.glCreateProgram();
        this.gl.glAttachShader(shaderProgram,
                               vertexShader);
        this.gl.glAttachShader(shaderProgram,
                               fragmentShader);
        this.gl.glLinkProgram(shaderProgram);
        return shaderProgram;
    }

    private void compileShader(final int shaderHandle,
                               final String shaderSource) {
        final String[] lines = new String[]{shaderSource};
        final int[] lengths = new int[]{lines[0].length()};
        this.gl.glShaderSource(shaderHandle,
                               lines.length,
                               lines,
                               lengths,
                               0);
        this.gl.glCompileShader(shaderHandle);

        final IntBuffer vstatus = IntBuffer.allocate(1);
        this.gl.glGetShaderiv(shaderHandle,
                              GL2ES2.GL_COMPILE_STATUS,
                              vstatus);
        if (vstatus.get(0) == GL.GL_TRUE) {
            //success
        }
        else {
            //failure!
            //get log length
            final int[] logLength = new int[1];
            this.gl.glGetShaderiv(shaderHandle,
                                  GL2ES2.GL_INFO_LOG_LENGTH,
                                  logLength,
                                  0);
            //get log
            if (logLength[0] == 0) {
                logLength[0] = 1024;
            }
            final byte[] log = new byte[logLength[0]];
            this.gl.glGetShaderInfoLog(shaderHandle,
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

        final int uniTrans = this.gl.glGetUniformLocation(program,
                                                          "mu_projection");
        this.gl.glUniformMatrix4fv(uniTrans,
                                   1,
                                   false,
                                   projection.getBuffer());

        this.gl.glBufferData(GL2ES2.GL_ARRAY_BUFFER,
                             vertices.length * 4,
                             Buffers.newDirectFloatBuffer(vertices),
                             GL2ES2.GL_DYNAMIC_DRAW);
        final int posAttrib = this.gl.glGetAttribLocation(program,
                                                          "va_position");
        final int texAttrib = this.gl.glGetAttribLocation(program,
                                                          "va_texcoord");
        this.gl.glEnableVertexAttribArray(posAttrib);
        this.gl.glVertexAttribPointer(posAttrib,
                                      2,
                                      GL2ES2.GL_FLOAT,
                                      false,
                                      4 * 4,
                                      0);
        this.gl.glEnableVertexAttribArray(texAttrib);
        this.gl.glVertexAttribPointer(texAttrib,
                                      2,
                                      GL.GL_FLOAT,
                                      false,
                                      4 * 4,
                                      2 * 4);
    }

    private void refreshGl() {
        this.gl = this.drawable.getGL()
                               .getGL2ES2();
    }

    private GLBufferFormat queryBufferFormat(final ShmBuffer buffer) {
        final GLBufferFormat format;
        final int bufferFormat = buffer.getFormat();
        if (bufferFormat == WlShmFormat.ARGB8888.getValue()) {
            format = SHM_ARGB8888;
        }
        else if (bufferFormat == WlShmFormat.XRGB8888.getValue()) {
            format = SHM_XRGB8888;
        }
        else {
            throw new UnsupportedOperationException("Format " + buffer.getFormat() + " not supported.");
        }
        return format;
    }

    private GLSurfaceData querySurfaceData(final WlSurfaceResource surfaceResource,
                                           final ShmBuffer buffer) {
        GLSurfaceData surfaceData = this.cachedSurfaceData.get(surfaceResource);
        if (surfaceData == null) {
            surfaceData = GLSurfaceData.create(this.gl);
            surfaceData.init(this.gl,
                             buffer);
            this.cachedSurfaceData.put(surfaceResource,
                                       surfaceData);
        }
        return surfaceData;
    }
}
