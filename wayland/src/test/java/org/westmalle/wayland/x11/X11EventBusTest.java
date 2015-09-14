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

import com.sun.jna.Pointer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X11EventBusTest {

    @Mock
    private Libxcb      libxcb;
    @Mock
    private Libc        libc;
    @Mock
    private Pointer     xcbConnection;
    @InjectMocks
    private X11EventBus x11EventBus;

    @Test
    public void testHandle() throws Exception {
        //given
        final int                 fd            = 0;
        final int                 mask          = 0;
        final xcb_generic_event_t generic_event = new xcb_generic_event_t();

        when(this.libxcb.xcb_poll_for_event(this.xcbConnection)).thenReturn(generic_event,
                                                                            generic_event,
                                                                            null);
        final Slot<xcb_generic_event_t> slot = mock(Slot.class);
        this.x11EventBus.getXEventSignal()
                        .connect(slot);

        //when
        this.x11EventBus.handle(fd,
                                mask);

        //then
        verify(this.libc,
               times(2)).free(generic_event.getPointer());
        verify(slot,
               times(2)).handle(generic_event);
    }
}