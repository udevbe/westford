package org.westmalle.wayland.egl;

import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShmFormat;
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
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Map;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShmBuffer.class,
                 Gles2SurfaceData.class})
public class EglGles2RenderEngineTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private LibGLESv2            libGLESv2;
    @InjectMocks
    private EglGles2RenderEngine eglGles2RenderEngine;

    @Test
    public void testBegin() throws Exception {
        //given
        final WlOutput     wlOutput     = mock(WlOutput.class);
        final Output       output       = mock(Output.class);
        final OutputMode   mode         = mock(OutputMode.class);
        final HasEglOutput hasEglOutput = mock(HasEglOutput.class);
        final EglOutput    eglOutput    = mock(EglOutput.class);
        final int          width        = 640;
        final int          height       = 480;

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getMode()).thenReturn(mode);
        when(output.getImplementation()).thenReturn(hasEglOutput);
        when(hasEglOutput.getEglOutput()).thenReturn(eglOutput);
        when(mode.getWidth()).thenReturn(width);
        when(mode.getHeight()).thenReturn(height);

        //when
        this.eglGles2RenderEngine.begin(wlOutput);

        //then
        verify(eglOutput).begin();
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
        final int              shmFormat        = WlShmFormat.XRGB8888.getValue();
        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        final Integer shaderProgram    = 12346;
        final Mat4    surfaceTransform = Mat4.IDENTITY;
        final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                                     "cachedSurfaceData");

        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(Gles2SurfaceData.create(this.libGLESv2)).thenReturn(gles2SurfaceData);
        when(shmBuffer.getFormat()).thenReturn(shmFormat);
        shaderPrograms.put(Gles2BufferFormat.SHM_XRGB8888,
                           shaderProgram);
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  "projection",
                                  Mat4.IDENTITY);
        when(surface.getTransform()).thenReturn(surfaceTransform);
        cachedSurfaceData.put(wlSurfaceResource,
                              gles2SurfaceData);

        //when
        this.eglGles2RenderEngine.draw(wlSurfaceResource,
                                       wlBufferResource);
        //then
        verifyStatic();
        ShmBuffer.get(wlBufferResource);
        verify(shmBuffer).beginAccess();
        verify(gles2SurfaceData,
               never()).init(this.libGLESv2,
                             shmBuffer);
        verify(gles2SurfaceData).makeActive(this.libGLESv2,
                                            shmBuffer);
        verify(shmBuffer).endAccess();
        verify(surface).firePaintCallbacks(anyInt());
    }

    @Test
    public void testDrawNotShmBuffer() throws Exception {
        //given
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("Buffer resource is not an ShmBuffer.");

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        mockStatic(ShmBuffer.class);

        when(ShmBuffer.get(wlBufferResource)).thenReturn(null);

        //when
        this.eglGles2RenderEngine.draw(wlSurfaceResource,
                                       wlBufferResource);
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
        final int              shmFormat        = WlShmFormat.XRGB8888.getValue();
        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        final Integer shaderProgram    = 12346;
        final Mat4    surfaceTransform = Mat4.IDENTITY;

        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(Gles2SurfaceData.create(this.libGLESv2)).thenReturn(gles2SurfaceData);
        when(shmBuffer.getFormat()).thenReturn(shmFormat);
        shaderPrograms.put(Gles2BufferFormat.SHM_XRGB8888,
                           shaderProgram);
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  "projection",
                                  Mat4.IDENTITY);
        when(surface.getTransform()).thenReturn(surfaceTransform);

        //when
        this.eglGles2RenderEngine.draw(wlSurfaceResource,
                                       wlBufferResource);
        //then
        verifyStatic();
        ShmBuffer.get(wlBufferResource);
        verify(shmBuffer).beginAccess();
        verify(gles2SurfaceData).init(this.libGLESv2,
                                      shmBuffer);
        verify(gles2SurfaceData).makeActive(this.libGLESv2,
                                            shmBuffer);
        verify(shmBuffer).endAccess();
        verify(surface).firePaintCallbacks(anyInt());

        //and when
        this.eglGles2RenderEngine.draw(wlSurfaceResource,
                                       wlBufferResource);
        //then
        verify(gles2SurfaceData,
               times(2)).makeActive(this.libGLESv2,
                                    shmBuffer);
        verify(gles2SurfaceData).init(this.libGLESv2,
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
        final int              shmFormat        = WlShmFormat.XRGB8888.getValue();
        final Map<Gles2BufferFormat, Integer> shaderPrograms = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                         "shaderPrograms");
        final Integer shaderProgram    = 12346;
        final Mat4    surfaceTransform = Mat4.IDENTITY;
        final Map<WlSurfaceResource, Gles2SurfaceData> cachedSurfaceData = Whitebox.getInternalState(this.eglGles2RenderEngine,
                                                                                                     "cachedSurfaceData");

        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(Gles2SurfaceData.create(this.libGLESv2)).thenReturn(gles2SurfaceData);
        when(shmBuffer.getWidth()).thenReturn(bufferWidth);
        when(shmBuffer.getHeight()).thenReturn(bufferHeight);
        when(shmBuffer.getFormat()).thenReturn(shmFormat);
        shaderPrograms.put(Gles2BufferFormat.SHM_XRGB8888,
                           shaderProgram);
        Whitebox.setInternalState(this.eglGles2RenderEngine,
                                  "projection",
                                  Mat4.IDENTITY);
        when(surface.getTransform()).thenReturn(surfaceTransform);
        cachedSurfaceData.put(wlSurfaceResource,
                              gles2SurfaceData);
        //when
        this.eglGles2RenderEngine.draw(wlSurfaceResource,
                                       wlBufferResource);
        //then
        verify(gles2SurfaceData).destroy(this.libGLESv2);
        verify(gles2SurfaceData).init(this.libGLESv2,
                                      shmBuffer);
        verify(gles2SurfaceData).makeActive(this.libGLESv2,
                                            shmBuffer);
    }

    @Test
    public void testEnd() throws Exception {
        //given
        final WlOutput     wlOutput     = mock(WlOutput.class);
        final Output       output       = mock(Output.class);
        final HasEglOutput hasEglOutput = mock(HasEglOutput.class);
        final EglOutput    eglOutput    = mock(EglOutput.class);

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getImplementation()).thenReturn(hasEglOutput);
        when(hasEglOutput.getEglOutput()).thenReturn(eglOutput);
        //when
        this.eglGles2RenderEngine.end(wlOutput);
        //then
        verify(eglOutput).end();
    }
}