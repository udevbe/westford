package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompositorTest {

    @Mock
    private Display display;
    @Mock
    private Scene scene;
    @Mock
    private ShmRenderer shmRenderer;

    @InjectMocks
    private Compositor compositor;

    @Test
    public void testRequestRender() throws Exception {
        //given
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        when(eventLoop.addIdle(any())).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final EventLoop.IdleHandler idleHandler = (EventLoop.IdleHandler) arg0;
            idleHandler.handle();
            return null;
        });

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource2 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        wlSurfaceResources.add(wlSurfaceResource1);
        wlSurfaceResources.add(wlSurfaceResource2);
        when(this.scene.getSurfacesStack()).thenReturn(wlSurfaceResources);

        when(this.scene.needsRender(any())).thenReturn(true);

        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);
        //when
        this.compositor.requestRender(surfaceResource);
        //then
        InOrder inOrder = inOrder(this.shmRenderer,
                                  this.display);
        inOrder.verify(this.shmRenderer,
                       times(1)).beginRender();
        inOrder.verify(this.shmRenderer,
                       times(1)).render(wlSurfaceResource0);
        inOrder.verify(this.shmRenderer,
                       times(1)).render(wlSurfaceResource1);
        inOrder.verify(this.shmRenderer,
                       times(1)).render(wlSurfaceResource2);
        inOrder.verify(this.shmRenderer,
                       times(1)).endRender();
        inOrder.verify(this.display,
                       times(1)).flushClients();
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
            return null;
        });

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource2 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        wlSurfaceResources.add(wlSurfaceResource1);
        wlSurfaceResources.add(wlSurfaceResource2);
        when(this.scene.getSurfacesStack()).thenReturn(wlSurfaceResources);

        when(this.scene.needsRender(any())).thenReturn(true);

        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);
        //when
        this.compositor.requestRender(surfaceResource);
        this.compositor.requestRender(surfaceResource);
        //then
        assertThat((Iterable< EventLoop.IdleHandler>)idleHandlers).hasSize(1);
        //and when
        idleHandlers.get(0).handle();
        this.compositor.requestRender(surfaceResource);
        //then
        assertThat((Iterable< EventLoop.IdleHandler>)idleHandlers).hasSize(2);
    }
}