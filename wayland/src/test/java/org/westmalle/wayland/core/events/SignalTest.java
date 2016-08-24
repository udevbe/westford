package org.westmalle.wayland.core.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.Signal;
import org.westmalle.Slot;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SignalTest {

    @InjectMocks
    private Signal<Object, Slot<Object>> signal;

    @Test
    public void testEmit() throws Exception {
        //given
        final Object       event = new Object();
        final Slot<Object> slot  = mock(Slot.class);
        this.signal.connect(slot);

        //when
        this.signal.emit(event);

        //then
        verify(slot).handle(event);
    }

    @Test
    public void testEmitUpdateSlot() throws Exception {
        //given
        final Object       event = new Object();
        final Slot<Object> slot  = mock(Slot.class);
        doAnswer(invocation -> {
            this.signal.disconnect(slot);
            return null;
        }).when(slot)
          .handle(event);
        this.signal.connect(slot);

        //when
        this.signal.emit(event);

        //then
        verify(slot).handle(event);

        //and when
        this.signal.emit(event);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testIsConnected() throws Exception {
        //given
        final Object       event = new Object();
        final Slot<Object> slot  = mock(Slot.class);
        this.signal.connect(slot);

        //when
        final boolean connected = this.signal.isConnected(slot);

        //then
        assertThat(connected).isTrue();
    }
}