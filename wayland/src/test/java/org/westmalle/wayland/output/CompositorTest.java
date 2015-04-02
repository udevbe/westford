package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositorTest {

    @Mock
    private Display     display;
    @Mock
    private ShmRenderer shmRenderer;

    @InjectMocks
    private Compositor compositor;

    @Test
    public void testRequestRender() throws Exception {
        //given
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final List<EventLoop.IdleHandler> idleHandlers = new LinkedList<>();
        when(eventLoop.addIdle(any())).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final EventLoop.IdleHandler idleHandler = (EventLoop.IdleHandler) arg0;
            idleHandlers.add(idleHandler);
            return mock(EventSource.class);
        });

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource2 = mock(WlSurfaceResource.class);
        this.compositor.getSurfacesStack()
                       .add(wlSurfaceResource0);
        this.compositor.getSurfacesStack()
                       .add(wlSurfaceResource1);
        this.compositor.getSurfacesStack()
                       .add(wlSurfaceResource2);

        //when
        this.compositor.requestRender();
        idleHandlers.get(0).handle();
        //then
        final InOrder inOrder = inOrder(this.shmRenderer,
                                        this.display);
        inOrder.verify(this.shmRenderer)
               .beginRender();
        inOrder.verify(this.shmRenderer)
               .render(wlSurfaceResource0);
        inOrder.verify(this.shmRenderer)
               .render(wlSurfaceResource1);
        inOrder.verify(this.shmRenderer)
               .render(wlSurfaceResource2);
        inOrder.verify(this.shmRenderer)
               .endRender();
        inOrder.verify(this.display)
               .flushClients();
    }

    @Test
    public void testRequestRenderPreviousRenderBusy() throws Exception {
        //given
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);

        final List<EventLoop.IdleHandler> idleHandlers = new LinkedList<>();
        when(eventLoop.addIdle(any())).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final EventLoop.IdleHandler idleHandler = (EventLoop.IdleHandler) arg0;
            idleHandlers.add(idleHandler);
            return mock(EventSource.class);
        });

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource2 = mock(WlSurfaceResource.class);
        this.compositor.getSurfacesStack()
                       .add(wlSurfaceResource0);
        this.compositor.getSurfacesStack()
                       .add(wlSurfaceResource1);
        this.compositor.getSurfacesStack()
                       .add(wlSurfaceResource2);

        //when
        this.compositor.requestRender();
        this.compositor.requestRender();
        //then
        assertThat((Iterable<EventLoop.IdleHandler>) idleHandlers).hasSize(1);
        //and when
        idleHandlers.get(0)
                    .handle();
        this.compositor.requestRender();
        //then
        assertThat((Iterable<EventLoop.IdleHandler>) idleHandlers).hasSize(2);
    }
}