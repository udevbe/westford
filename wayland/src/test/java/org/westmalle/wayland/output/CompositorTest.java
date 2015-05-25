package org.westmalle.wayland.output;

import com.jogamp.opengl.GLDrawable;
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
import org.westmalle.wayland.protocol.WlOutput;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositorTest {

    @Mock
    private Display     display;
    @Mock
    private Renderer renderer;

    @InjectMocks
    private Compositor compositor;

    @Test
    public void testRequestRender() throws Exception {
        //given
        final GLDrawable      glDrawable0 = mock(GLDrawable.class);
        final GLDrawable      glDrawable1 = mock(GLDrawable.class);

        WlOutput wlOutput0 = mock(WlOutput.class);
        Output output0 = mock(Output.class);
        when(wlOutput0.getOutput()).thenReturn(output0);
        when(output0.getImplementation()).thenReturn(glDrawable0);
        this.compositor.getWlOutputs().add(wlOutput0);

        WlOutput wlOutput1 = mock(WlOutput.class);
        Output output1 = mock(Output.class);
        when(wlOutput1.getOutput()).thenReturn(output1);
        when(output1.getImplementation()).thenReturn(glDrawable1);
        this.compositor.getWlOutputs().add(wlOutput1);

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
        idleHandlers.get(0)
                    .handle();
        //then
        final InOrder inOrder0 = inOrder(this.renderer,
                                         this.display);
        inOrder0.verify(this.renderer)
                .beginRender(glDrawable0);
        inOrder0.verify(this.renderer)
                .render(wlSurfaceResource0);
        inOrder0.verify(this.renderer)
                .render(wlSurfaceResource1);
        inOrder0.verify(this.renderer)
                .render(wlSurfaceResource2);
        inOrder0.verify(this.renderer)
                .endRender(glDrawable0);

        final InOrder inOrder1 = inOrder(this.renderer,
                                         this.display);
        inOrder1.verify(this.renderer)
                .beginRender(glDrawable1);
        inOrder1.verify(this.renderer)
                .render(wlSurfaceResource0);
        inOrder1.verify(this.renderer)
                .render(wlSurfaceResource1);
        inOrder1.verify(this.renderer)
                .render(wlSurfaceResource2);
        inOrder1.verify(this.renderer)
                .endRender(glDrawable1);

        verify(this.display).flushClients();
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