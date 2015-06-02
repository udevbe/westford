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
package org.westmalle.wayland.output;

import com.google.common.util.concurrent.ListenableFuture;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Optional;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ShmBuffer.class)
public class RendererTest {

    @Mock
    private RenderEngine renderEngine;
    @InjectMocks
    private Renderer     renderer;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(ShmBuffer.class);
    }

    @Test
    public void testRender() throws Exception {
        //given
        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = mock(WlSurface.class);
        when(surfaceResource.getImplementation()).thenReturn(wlSurface);

        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        final SurfaceState surfaceState = mock(SurfaceState.class);
        when(surface.getState()).thenReturn(surfaceState);

        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));

        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);

        //when
        this.renderer.render(surfaceResource);
        //then
        verify(this.renderEngine).draw(surfaceResource,
                                       wlBufferResource);
    }

    @Test
    public void testBeginRender() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        final Output   output   = mock(Output.class);
        when(wlOutput.getOutput()).thenReturn(output);
        final Object outputImpl = mock(Object.class);
        when(output.getImplementation()).thenReturn(outputImpl);
        //when
        this.renderer.beginRender(wlOutput);
        //then
        verify(this.renderEngine).begin(wlOutput);
    }

    @Test
    public void testEndRender() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        final Output   output   = mock(Output.class);
        final Object outputImpl = mock(Object.class);
        when(output.getImplementation()).thenReturn(outputImpl);
        final Future future = mock(ListenableFuture.class);
        when(this.renderEngine.end(wlOutput)).thenReturn(future);
        //when
        this.renderer.endRender(wlOutput);
        //then
        verify(this.renderEngine).end(wlOutput);
    }
}