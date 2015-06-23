package org.westmalle.wayland.egl;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.LibGLESv2;
import org.westmalle.wayland.protocol.WlOutput;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EglGles2RenderEngineTest {

    @Mock
    private LibGLESv2 libGLESv2;
    @InjectMocks
    private EglGles2RenderEngine eglGles2RenderEngine;

    @Test
    public void testBegin() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        //when
        this.eglGles2RenderEngine.begin(wlOutput);
        //then

    }

    @Test
    public void testDraw() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        //when
        this.eglGles2RenderEngine.draw(wlSurfaceResource,
                                       wlBufferResource);
        //then

    }

    @Test
    public void testEnd() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        //when
        this.eglGles2RenderEngine.end(wlOutput);
        //then

    }
}