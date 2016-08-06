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
package org.westmalle.wayland.gles2;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.core.Scene;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COMPILE_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_LINK_STATUS;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShmBuffer.class})
public class Gles2RendererTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private LibGLESv2     libGLESv2;
    @Mock
    private Scene         scene;
    @InjectMocks
    private Gles2Renderer eglGles2RenderEngine;

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

    //TODO
    @Test
    public void testOnDestroy() throws Exception {
        //TODO split out surfaces using unit test w/ args?
        //given: a known surface, an unknown surface
        //when: onDestroy is called
        //then: surface state is cleaned up
    }

    //TODO
    @Test
    public void testVisit() throws Exception {
        //TODO split out surfaces using unit test w/ args?
        //given: a supported platform, a new surface w/ shm buffer, an existing surface w/ shm buffer, a new surface w/ egl buffer, an existing surface w/ egl buffer, a surface w/o buffer, a surface w/ unsupported buffer
        //when: visit is called
        //then: platform begin is called, projection is updated, platform end is called
    }

    @Test
    public void testRequestRenderWithSubsurfaces() throws Exception {
        //TODO
        //given: a compositor, a surface, several stacks of nested subsurfaces
        //when: request render is called
        //then: the tree of subsurfaces is rendered in pre-order.
    }

    //TODO
    public void testVisitUnsupportedPlatform() {
        //given: an unsupported platform
        //when: visit is called
        //then: an exception is thrown.
    }

    //TODO convert?
    @Test
    public void testBeginNoProjectionUpdate() throws Exception {
//        //given
//
//        final WlOutput    wlOutput      = mock(WlOutput.class);
//        final Output      output        = mock(Output.class);
//        final OutputMode  mode          = mock(OutputMode.class);
//        final EglRenderPlatform platform      = mock(EglRenderPlatform.class);
//        final int         width         = 640;
//        final int         height        = 480;
//        final Integer     shaderProgram = 12346;
//
//        when(wlOutput.getOutput()).thenReturn(output);
//        when(output.getMode()).thenReturn(mode);
//        when(mode.getWidth()).thenReturn(width);
//        when(mode.getHeight()).thenReturn(height);
//
//        final Map<Integer, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
//                                                                               "shaderPrograms");
//        shaderPrograms.put(WlShmFormat.ARGB8888.value,
//                           shaderProgram);
//
//        //@formatter:off
//        Whitebox.setInternalState(this.eglGles2RenderEngine,
//                                  Mat4.create(2.0f / width, 0,              0, -1,
//                                              0,            2.0f / -height, 0,  1,
//                                              0,            0,              1,  0,
//                                              0,            0,              0,  1).toArray());
//        //@formatter:on
//        //when
//        this.eglGles2RenderEngine.begin(platform);
//
//        //then
//        verify(platform).begin();
//        verify(this.libGLESv2,
//               times(0)).glUniformMatrix4fv(anyInt(),
//                                            anyInt(),
//                                            anyInt(),
//                                            anyLong());
    }

    //TODO convert?
    @Test
    public void testBeginProjectionUpdate() throws Exception {
//        //given
//        final WlOutput    wlOutput = mock(WlOutput.class);
//        final Output      output   = mock(Output.class);
//        final OutputMode  mode     = mock(OutputMode.class);
//        final EglRenderPlatform platform = mock(EglRenderPlatform.class);
//        final int         width    = 640;
//        final int         height   = 480;
//
//        when(wlOutput.getOutput()).thenReturn(output);
//        when(output.getMode()).thenReturn(mode);
//        when(mode.getWidth()).thenReturn(width);
//        when(mode.getHeight()).thenReturn(height);
//
//        //when
//        this.eglGles2RenderEngine.begin(platform);
//
//        //then FIXME do proper check
//        verify(platform).begin();
    }

    //TODO convert?
    @Test
    public void testDrawExistingBuffer() throws Exception {
//        //given
//        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
//        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
//        mockStatic(ShmBuffer.class);
//        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
//        final WlSurface wlSurface = mock(WlSurface.class);
//        final Surface   surface   = mock(Surface.class);
//        mockStatic(Gles2SurfaceData.class);
//        final Gles2SurfaceData gles2SurfaceData = mock(Gles2SurfaceData.class);
//        final int              shmFormat        = WlShmFormat.ARGB8888.value;
//        final Map<Integer, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
//                                                                               "shaderPrograms");
//        final Integer shaderProgram    = 12346;
//        final Mat4    surfaceTransform = Mat4.IDENTITY;
//        final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = Whitebox.getInternalState(this.eglGles2RenderEngine,
//                                                                                                     "cachedSurfaceData");
//
//        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
//        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//        when(Gles2SurfaceData.create(this.libGLESv2,
//                                     shmBuffer)).thenReturn(gles2SurfaceData);
//        when(shmBuffer.getFormat()).thenReturn(shmFormat);
//        shaderPrograms.put(WlShmFormat.ARGB8888.value,
//                           shaderProgram);
//        Whitebox.setInternalState(this.eglGles2RenderEngine,
//                                  "projection",
//                                  Mat4.IDENTITY.toArray());
//        when(surface.getTransform()).thenReturn(surfaceTransform);
//        cachedSurfaceData.put(wlSurfaceResource,
//                              gles2SurfaceData);
//
//        SurfaceState surfaceState = mock(SurfaceState.class);
//        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));
//
//        when(surface.getState()).thenReturn(surfaceState);
//
//        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
//        surfaceStack.add(wlSurfaceResource);
//        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);
//
//        final EglRenderPlatform eglPlatform = mock(EglRenderPlatform.class);
//
//        //when
//        this.eglGles2RenderEngine.render(eglPlatform);
//
//        //then
//        verifyStatic();
//        ShmBuffer.get(wlBufferResource);
//        verify(gles2SurfaceData).update(this.libGLESv2,
//                                        wlSurfaceResource,
//                                        shmBuffer);
    }

    //TODO convert?
    @Test
    public void testDrawNotShmBuffer() throws Exception {
//        //given
//        this.exception.expect(IllegalArgumentException.class);
//        this.exception.expectMessage("Buffer resource is not an ShmBuffer.");
//
//        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
//        final WlSurface         wlSurface         = mock(WlSurface.class);
//        final Surface           surface           = mock(Surface.class);
//        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
//        mockStatic(ShmBuffer.class);
//
//        when(ShmBuffer.get(wlBufferResource)).thenReturn(null);
//        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//
//        SurfaceState surfaceState = mock(SurfaceState.class);
//        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));
//
//        when(surface.getState()).thenReturn(surfaceState);
//
//        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
//        surfaceStack.add(wlSurfaceResource);
//        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);
//
//        final EglRenderPlatform eglPlatform = mock(EglRenderPlatform.class);
//
//        //when
//        this.eglGles2RenderEngine.render(eglPlatform);
//        //then
//        verifyStatic();
//        ShmBuffer.get(wlBufferResource);
//        //exception is thrown
    }

    //TODO convert test
    @Test
    public void testDrawNewBuffer() throws Exception {
//        //given
//        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
//        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
//        mockStatic(ShmBuffer.class);
//        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
//        final WlSurface wlSurface = mock(WlSurface.class);
//        final Surface   surface   = mock(Surface.class);
//        mockStatic(Gles2SurfaceData.class);
//        final Gles2SurfaceData gles2SurfaceData = mock(Gles2SurfaceData.class);
//        final int              shmFormat        = WlShmFormat.ARGB8888.value;
//        final Map<Integer, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
//                                                                               "shaderPrograms");
//        final Integer shaderProgram    = 12346;
//        final Mat4    surfaceTransform = Mat4.IDENTITY;
//
//        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
//        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//        when(Gles2SurfaceData.create(this.libGLESv2,
//                                     shmBuffer)).thenReturn(gles2SurfaceData);
//        when(shmBuffer.getFormat()).thenReturn(shmFormat);
//        shaderPrograms.put(WlShmFormat.ARGB8888.value,
//                           shaderProgram);
//        Whitebox.setInternalState(this.eglGles2RenderEngine,
//                                  "projection",
//                                  Mat4.IDENTITY.toArray());
//        when(surface.getTransform()).thenReturn(surfaceTransform);
//
//        SurfaceState surfaceState = mock(SurfaceState.class);
//        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));
//
//        when(surface.getState()).thenReturn(surfaceState);
//
//        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
//        surfaceStack.add(wlSurfaceResource);
//        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);
//
//        final EglRenderPlatform eglPlatform = mock(EglRenderPlatform.class);
//
//        //when
//        this.eglGles2RenderEngine.render(eglPlatform);
//
//        //then
//        verifyStatic();
//        ShmBuffer.get(wlBufferResource);
//        verify(gles2SurfaceData).update(this.libGLESv2,
//                                        wlSurfaceResource,
//                                        shmBuffer);
//
//        //and when
//        this.eglGles2RenderEngine.render(eglPlatform);
//
//        //then
//        verify(gles2SurfaceData,
//               times(2)).update(this.libGLESv2,
//                                wlSurfaceResource,
//                                shmBuffer);
    }

    //TODO convert test
    @Test
    public void testDrawResizedBuffer() throws Exception {
//        //given
//        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
//        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
//        mockStatic(ShmBuffer.class);
//        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
//        final WlSurface wlSurface = mock(WlSurface.class);
//        final Surface   surface   = mock(Surface.class);
//        mockStatic(Gles2SurfaceData.class);
//        final Gles2SurfaceData gles2SurfaceData = mock(Gles2SurfaceData.class);
//        final int              bufferWidth      = 640;
//        final int              bufferHeight     = 480;
//        final int              shmFormat        = WlShmFormat.ARGB8888.value;
//        final Map<Integer, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
//                                                                               "shaderPrograms");
//        final Integer shaderProgram    = 12346;
//        final Mat4    surfaceTransform = Mat4.IDENTITY;
//        final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = Whitebox.getInternalState(this.eglGles2RenderEngine,
//                                                                                                     "cachedSurfaceData");
//
//        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
//        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//        when(Gles2SurfaceData.create(this.libGLESv2,
//                                     shmBuffer)).thenReturn(gles2SurfaceData);
//        when(shmBuffer.getWidth()).thenReturn(bufferWidth);
//        when(shmBuffer.getHeight()).thenReturn(bufferHeight);
//        when(shmBuffer.getHeight()).thenReturn(bufferHeight);
//        when(shmBuffer.getFormat()).thenReturn(shmFormat);
//        shaderPrograms.put(WlShmFormat.ARGB8888.value,
//                           shaderProgram);
//        Whitebox.setInternalState(this.eglGles2RenderEngine,
//                                  "projection",
//                                  Mat4.IDENTITY.toArray());
//        when(surface.getTransform()).thenReturn(surfaceTransform);
//        cachedSurfaceData.put(wlSurfaceResource,
//                              gles2SurfaceData);
//
//        SurfaceState surfaceState = mock(SurfaceState.class);
//        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));
//
//        when(surface.getState()).thenReturn(surfaceState);
//
//        LinkedList<WlSurfaceResource> surfaceStack = new LinkedList<>();
//        surfaceStack.add(wlSurfaceResource);
//        when(this.scene.getSurfacesStack()).thenReturn(surfaceStack);
//
//        final EglRenderPlatform eglPlatform = mock(EglRenderPlatform.class);
//
//        //when
//        this.eglGles2RenderEngine.render(eglPlatform);
//
//        //then
//        verify(gles2SurfaceData).delete(this.libGLESv2);
//        verify(gles2SurfaceData).update(this.libGLESv2,
//                                        wlSurfaceResource,
//                                        shmBuffer);
    }
}