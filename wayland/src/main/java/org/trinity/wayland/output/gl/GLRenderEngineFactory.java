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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jogamp.common.nio.Buffers;

import javax.inject.Inject;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class GLRenderEngineFactory {

    @Inject
    GLRenderEngineFactory() {
    }

    public GLRenderEngine create(final GLAutoDrawable drawable) {

        final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(r -> new Thread(r,
                                                                                                                                            "GL Render Engine")));
        try {
            return executorService.submit(() -> {
                makeCurrent(drawable);
                final GL2ES2 gl = drawable.getGL()
                                          .getGL2ES2();
                gl.setSwapInterval(1);
                final IntBuffer elementBuffer = Buffers.newDirectIntBuffer(1);
                gl.glGenBuffers(1,
                                elementBuffer);
                final IntBuffer vertexBuffer = Buffers.newDirectIntBuffer(1);
                gl.glGenBuffers(1,
                                vertexBuffer);

                return new GLRenderEngine(executorService,
                                          drawable,
                                          elementBuffer,
                                          vertexBuffer);
            })
                                  .get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private GLContext makeCurrent(final GLAutoDrawable drawable) {
        final GLContext context = drawable.getContext();
        final int current = context.makeCurrent();
        switch (current) {
            case GLContext.CONTEXT_NOT_CURRENT:
                throw new IllegalStateException("GLContext could not be made current.");
            case GLContext.CONTEXT_CURRENT:
            case GLContext.CONTEXT_CURRENT_NEW:
        }
        return context;
    }
}
