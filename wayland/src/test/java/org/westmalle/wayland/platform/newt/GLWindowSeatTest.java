package org.westmalle.wayland.platform.newt;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.JobExecutor;
import org.westmalle.wayland.output.PointerDevice;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSeat;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes are final, so we have to powermock them:
                        NEWTEvent.class,
                        MouseEvent.class
                })
public class GLWindowSeatTest {

    @Mock
    private WlSeat       wlSeat;
    @Mock
    private JobExecutor  jobExecutor;
    @InjectMocks
    private GLWindowSeat glWindowSeat;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            runnable.run();
            return null;
        }).when(this.jobExecutor)
          .submit(any());
    }

    @Test
    public void testMousePressed() throws Exception {
        //given
        final MouseEvent mouseEvent = mock(MouseEvent.class);
        final long time = 87654;
        when(mouseEvent.getWhen()).thenReturn(time);
        final short button = 3;
        when(mouseEvent.getButton()).thenReturn(button);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(this.wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource);
        when(wlPointer.getResources()).thenReturn(wlPointerResources);

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);

        this.wlSeat.setWlPointer(wlPointer);
        //when
        this.glWindowSeat.mousePressed(mouseEvent);
        //then
        verify(pointerDevice).button(wlPointerResources,
                                     (int) time,
                                     button,
                                     WlPointerButtonState.PRESSED);
    }

    @Test
    public void testMouseReleased() throws Exception {
        //given
        final MouseEvent mouseEvent = mock(MouseEvent.class);
        final long time = 87654;
        when(mouseEvent.getWhen()).thenReturn(time);
        final short button = 3;
        when(mouseEvent.getButton()).thenReturn(button);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(this.wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource);
        when(wlPointer.getResources()).thenReturn(wlPointerResources);

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);

        this.wlSeat.setWlPointer(wlPointer);
        //when
        this.glWindowSeat.mouseReleased(mouseEvent);
        //then
        verify(pointerDevice).button(wlPointerResources,
                                     (int) time,
                                     button,
                                     WlPointerButtonState.RELEASED);
    }

    @Test
    public void testMouseMoved() throws Exception {
        //given
        final MouseEvent mouseEvent = mock(MouseEvent.class);
        final long time = 87654;
        when(mouseEvent.getWhen()).thenReturn(time);
        final int x = 321;
        final int y = 543;
        when(mouseEvent.getX()).thenReturn(x);
        when(mouseEvent.getY()).thenReturn(y);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(this.wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource);
        when(wlPointer.getResources()).thenReturn(wlPointerResources);

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);

        this.wlSeat.setWlPointer(wlPointer);
        //when
        this.glWindowSeat.mouseMoved(mouseEvent);
        //then
        verify(pointerDevice).motion(wlPointerResources,
                                     (int) time,
                                     x,
                                     y);
    }
}