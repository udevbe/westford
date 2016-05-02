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
package org.westmalle.wayland.x11;

import org.freedesktop.jaccall.Pointer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X11EventBusTest {

    @Mock
    private Libxcb libxcb;

    private X11EventBus x11EventBus;

    @Test
    public void testHandle() throws Exception {
        //given
        final long xcbConnection = 123456;
        this.x11EventBus = new X11EventBus(libxcb,
                                           xcbConnection);

        final int     fd                    = 0;
        final int     mask                  = 0;
        Pointer<Void> generic_event_memory0 = Pointer.malloc(xcb_generic_event_t.SIZE);
        Pointer<Void> generic_event_memory1 = Pointer.malloc(xcb_generic_event_t.SIZE);


        when(this.libxcb.xcb_poll_for_event(xcbConnection)).thenReturn(generic_event_memory0.address,
                                                                       generic_event_memory1.address,
                                                                       0L);
        final Slot<Pointer<xcb_generic_event_t>> slot = mock(Slot.class);
        this.x11EventBus.getXEventSignal()
                        .connect(slot);

        //when
        this.x11EventBus.handle(fd,
                                mask);

        //then
        ArgumentCaptor<Pointer> pointerArgumentCaptor = ArgumentCaptor.forClass(Pointer.class);
        verify(slot,
               times(2)).handle(pointerArgumentCaptor.capture());

        final List<Pointer> pointers = pointerArgumentCaptor.getAllValues();
        assertThat(pointers.get(0).address).isEqualTo(generic_event_memory0.address);
        assertThat(pointers.get(1).address).isEqualTo(generic_event_memory1.address);
    }
}