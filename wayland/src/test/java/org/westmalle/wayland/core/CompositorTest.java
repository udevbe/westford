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
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventSource.class)
public class CompositorTest {

    @Mock
    private Display  display;
    @Mock
    private Renderer renderer;

    @InjectMocks
    private Compositor compositor;

    @Test
    public void testRequestRender() throws Exception {
        //given
        final WlOutput wlOutput0 = mock(WlOutput.class);
        final WlOutput wlOutput1 = mock(WlOutput.class);

        this.compositor.getWlOutputs().add(wlOutput0);
        this.compositor.getWlOutputs().add(wlOutput1);

        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final List<EventLoop.IdleHandler> idleHandlers = new LinkedList<>();
        when(eventLoop.addIdle(any())).thenAnswer(invocation -> {
            final Object                arg0        = invocation.getArguments()[0];
            final EventLoop.IdleHandler idleHandler = (EventLoop.IdleHandler) arg0;
            idleHandlers.add(idleHandler);
            return mock(EventSource.class);
        });

        //when
        this.compositor.requestRender();
        idleHandlers.get(0)
                    .handle();
        //then
        final InOrder inOrder0 = inOrder(this.renderer,
                                         this.display);
        inOrder0.verify(this.renderer)
                .render(wlOutput0);

        final InOrder inOrder1 = inOrder(this.renderer,
                                         this.display);
        inOrder1.verify(this.renderer)
                .render(wlOutput1);

        verify(this.display).flushClients();
    }

    @Test
    public void testRequestRenderPreviousRenderBusy() throws Exception {
        //given
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);

        final List<EventLoop.IdleHandler> idleHandlers = new LinkedList<>();
        when(eventLoop.addIdle(any())).thenAnswer(invocation -> {
            final Object                arg0        = invocation.getArguments()[0];
            final EventLoop.IdleHandler idleHandler = (EventLoop.IdleHandler) arg0;
            idleHandlers.add(idleHandler);
            return mock(EventSource.class);
        });

        //when
        this.compositor.requestRender();
        this.compositor.requestRender();
        //then
        assertThat(idleHandlers).hasSize(1);
        //and when
        idleHandlers.get(0)
                    .handle();
        this.compositor.requestRender();
        //then
        assertThat(idleHandlers).hasSize(2);
    }

    @Test
    public void testRequestRenderWithSubsurfaces() throws Exception {
        //given: a compositor, a surface, several stacks of nested subsurfaces
        //when: request render is called
        //then: the tree of subsurfaces is rendered in pre-order.
    }

    @Test
    public void testGetSubsurfaceStack() throws Exception {
        //given: a compositor, a surface
        //when: the subsurface stack is queried
        //then: the surface's surface stack is returned consistently, including the surface itself.
    }

    @Test
    public void testRemoveSubsurfaceStack() throws Exception {
        //given: a compositor, a surface, a surfacestack
        //when: the surfacestack is deleted
        //then: the surfacestack and pending surfacestack only includes the (parent) surface.
    }

    @Test
    public void testGetPendingSubsurfaceStack() throws Exception {
        //given: a compositor, a surface
        //when: the pending subsurface stack is queried
        //then: the pending surface's surface stack is returned consistently, including the surface itself.
    }

    @Test
    public void testCommitSubsurfaceStack() throws Exception {
        //given: a compositor, a surface, a pending surfacestack
        //when: the pending surfacestack is commited
        //then: the surface stack now reflects the (previously) pending surfacestack.
    }
}