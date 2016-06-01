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
package org.westmalle.wayland.egl;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.core.Scene;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.SurfaceState;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COMPILE_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_LINK_STATUS;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShmBuffer.class,
                 Gles2SurfaceData.class})
public class EglGles2RendererTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private LibGLESv2        libGLESv2;
    @Mock
    private Scene            scene;
    @InjectMocks
    private EglGles2Renderer eglGles2RenderEngine;

    @Before
    public void setUp() {
        when(this.libGLESv2.glGetString(GL_EXTENSIONS)).thenReturn(Pointer.nref("GL_EXT_texture_format_BGRA8888")
                                                                           .address);
        doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();
            final long     status    = (Long) arguments[2];
            Pointer.wrap(Integer.class,
                         status)
                   .write(1);
            return null;
        }).when(this.libGLESv2)
          .glGetProgramiv(anyInt(),
                          eq(GL_LINK_STATUS),
                          anyLong());
        doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();
            final long     status    = (Long) arguments[2];
            Pointer.wrap(Integer.class,
                         status)
                   .write(1);
            return null;
        }).when(this.libGLESv2)
          .glGetShaderiv(anyInt(),
                         eq(GL_COMPILE_STATUS),
                         anyLong());
    }

    @Test
    public void testBeginNoProjectionUpdate() throws Exception {
        //given

        final WlOutput     wlOutput      = mock(WlOutput.class);
        final Output       output        = mock(Output.class);
        final OutputMode   mode          = mock(OutputMode.class);
        final HasEglOutput hasEglOutput  = mock(HasEglOutput.class);
        final RenderOutput renderOutput  = mock(RenderOutput.class);
        final int          width         = 640;
        final int          height        = 480;
        final Integer      shaderProgram = 12346;

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getMode()).thenReturn(mode);
        when(output.getPlatformImplementation()).thenReturn(hasEglOutput);
        when(hasEglOutput.getEglOutput()).thenReturn(renderOutput);
        when(mode.getWidth()).thenReturn(width);
        when(mode.getHeight()).thenReturn(height);

        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        for (final Gles2BufferFormat gles2BufferFormat : Gles2BufferFormat.values()) {
            shaderPrograms.put(gles2BufferFormat,
                               shaderProgram);
        }
        //@formatter:off
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  Mat4.create(2.0f / width, 0,              0, -1,
                                              0,            2.0f / -height, 0,  1,
                                              0,            0,              1,  0,
                                              0,            0,              0,  1).toArray());
        //@formatter:on
        //when
        this.eglGles2RenderEngine.begin(wlOutput);

        //then
        verify(renderOutput).begin();
        verify(this.libGLESv2,
               times(0)).glUniformMatrix4fv(anyInt(),
                                            anyInt(),
                                            anyInt(),
                                            anyLong());
    }

    @Test
    public void testBeginProjectionUpdate() throws Exception {
        //given
        final WlOutput     wlOutput     = mock(WlOutput.class);
        final Output       output       = mock(Output.class);
        final OutputMode   mode         = mock(OutputMode.class);
        final HasEglOutput hasEglOutput = mock(HasEglOutput.class);
        final RenderOutput renderOutput = mock(RenderOutput.class);
        final int          width        = 640;
        final int          height       = 480;

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getMode()).thenReturn(mode);
        when(output.getPlatformImplementation()).thenReturn(hasEglOutput);
        when(hasEglOutput.getEglOutput()).thenReturn(renderOutput);
        when(mode.getWidth()).thenReturn(width);
        when(mode.getHeight()).thenReturn(height);

        //when
        this.eglGles2RenderEngine.begin(wlOutput);

        //then
        verify(renderOutput).begin();
    }

    @Test
    public void testDrawExistingBuffer() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        mockStatic(ShmBuffer.class);
        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface   surface   = mock(Surface.class);
        mockStatic(Gles2SurfaceData.class);
        final Gles2SurfaceData gles2SurfaceData = mock(Gles2SurfaceData.class);
        final int              shmFormat        = WlShmFormat.ARGB8888.value;
        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        final Integer shaderProgram    = 12346;
        final Mat4    surfaceTransform = Mat4.IDENTITY;
        final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                                     "cachedSurfaceData");

        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(Gles2SurfaceData.create(this.libGLESv2,
                                     shmBuffer)).thenReturn(gles2SurfaceData);
        when(shmBuffer.getFormat()).thenReturn(shmFormat);
        shaderPrograms.put(Gles2BufferFormat.SHM_ARGB8888,
                           shaderProgram);
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  "projection",
                                  Mat4.IDENTITY.toArray());
        when(surface.getTransform()).thenReturn(surfaceTransform);
        cachedSurfaceData.put(wlSurfaceResource,
                              gles2SurfaceData);

        SurfaceState surfaceState = mock(SurfaceState.class);
        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));

        when(surface.getState()).thenReturn(surfaceState);

        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
        surfaceStack.add(wlSurfaceResource);
        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);

        //when
        this.eglGles2RenderEngine.render();

        //then
        verifyStatic();
        ShmBuffer.get(wlBufferResource);
        verify(gles2SurfaceData).update(this.libGLESv2,
                                        wlSurfaceResource,
                                        shmBuffer);
    }

    @Test
    public void testDrawNotShmBuffer() throws Exception {
        //given
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("Buffer resource is not an ShmBuffer.");

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        mockStatic(ShmBuffer.class);

        when(ShmBuffer.get(wlBufferResource)).thenReturn(null);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);

        SurfaceState surfaceState = mock(SurfaceState.class);
        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));

        when(surface.getState()).thenReturn(surfaceState);

        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
        surfaceStack.add(wlSurfaceResource);
        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);

        //when
        this.eglGles2RenderEngine.render();
        //then
        verifyStatic();
        ShmBuffer.get(wlBufferResource);
        //exception is thrown
    }

    @Test
    public void testDrawNewBuffer() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        mockStatic(ShmBuffer.class);
        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface   surface   = mock(Surface.class);
        mockStatic(Gles2SurfaceData.class);
        final Gles2SurfaceData gles2SurfaceData = mock(Gles2SurfaceData.class);
        final int              shmFormat        = WlShmFormat.ARGB8888.value;
        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        final Integer shaderProgram    = 12346;
        final Mat4    surfaceTransform = Mat4.IDENTITY;

        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(Gles2SurfaceData.create(this.libGLESv2,
                                     shmBuffer)).thenReturn(gles2SurfaceData);
        when(shmBuffer.getFormat()).thenReturn(shmFormat);
        shaderPrograms.put(Gles2BufferFormat.SHM_ARGB8888,
                           shaderProgram);
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  "projection",
                                  Mat4.IDENTITY.toArray());
        when(surface.getTransform()).thenReturn(surfaceTransform);

        SurfaceState surfaceState = mock(SurfaceState.class);
        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));

        when(surface.getState()).thenReturn(surfaceState);

        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
        surfaceStack.add(wlSurfaceResource);
        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);


        //when
        this.eglGles2RenderEngine.render();

        //then
        verifyStatic();
        ShmBuffer.get(wlBufferResource);
        verify(gles2SurfaceData).update(this.libGLESv2,
                                        wlSurfaceResource,
                                        shmBuffer);

        //and when
        this.eglGles2RenderEngine.render();
        //then
        verify(gles2SurfaceData,
               times(2)).update(this.libGLESv2,
                                wlSurfaceResource,
                                shmBuffer);
    }

    @Test
    public void testDrawResizedBuffer() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        mockStatic(ShmBuffer.class);
        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface   surface   = mock(Surface.class);
        mockStatic(Gles2SurfaceData.class);
        final Gles2SurfaceData gles2SurfaceData = mock(Gles2SurfaceData.class);
        final int              bufferWidth      = 640;
        final int              bufferHeight     = 480;
        final int              shmFormat        = WlShmFormat.ARGB8888.value;
        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        final Integer shaderProgram    = 12346;
        final Mat4    surfaceTransform = Mat4.IDENTITY;
        final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                                     "cachedSurfaceData");

        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(Gles2SurfaceData.create(this.libGLESv2,
                                     shmBuffer)).thenReturn(gles2SurfaceData);
        when(shmBuffer.getWidth()).thenReturn(bufferWidth);
        when(shmBuffer.getHeight()).thenReturn(bufferHeight);
        when(shmBuffer.getHeight()).thenReturn(bufferHeight);
        when(shmBuffer.getFormat()).thenReturn(shmFormat);
        shaderPrograms.put(Gles2BufferFormat.SHM_ARGB8888,
                           shaderProgram);
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  "projection",
                                  Mat4.IDENTITY.toArray());
        when(surface.getTransform()).thenReturn(surfaceTransform);
        cachedSurfaceData.put(wlSurfaceResource,
                              gles2SurfaceData);

        SurfaceState surfaceState = mock(SurfaceState.class);
        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));

        when(surface.getState()).thenReturn(surfaceState);

        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
        surfaceStack.add(wlSurfaceResource);
        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);

        //when
        this.eglGles2RenderEngine.render();

        //then
        verify(gles2SurfaceData).delete(this.libGLESv2);
        verify(gles2SurfaceData).update(this.libGLESv2,
                                        wlSurfaceResource,
                                        shmBuffer);
    }

    @Test
    public void testEnd() throws Exception {
        //given
        final WlOutput     wlOutput     = mock(WlOutput.class);
        final Output       output       = mock(Output.class);
        final HasEglOutput hasEglOutput = mock(HasEglOutput.class);
        final RenderOutput renderOutput = mock(RenderOutput.class);

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getPlatformImplementation()).thenReturn(hasEglOutput);
        when(hasEglOutput.getEglOutput()).thenReturn(renderOutput);
        //when
        this.eglGles2RenderEngine.end(wlOutput);
        //then
        verify(renderOutput).end();
    }
}