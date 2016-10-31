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
package org.westford.compositor.protocol;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlTouchResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westford.compositor.core.KeyboardDevice;
import org.westford.compositor.core.Seat;

import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerCore.class,
                        InterfaceMeta.class
                })
public class WlSeatTest {

    @Mock
    private Display      display;
    @Mock
    private WlDataDevice wlDataDevice;
    @Mock
    private Seat         seat;
    @Mock
    private WlPointer    wlPointer;
    @Mock
    private WlKeyboard   wlKeyboard;
    @Mock
    private WlTouch      wlTouch;

    @Mock
    private WaylandServerCore waylandServerCore;
    @Mock
    private InterfaceMeta     interfaceMeta;

    private WlSeat wlSeat;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class,
                                InterfaceMeta.class);
        when(InterfaceMeta.get((Class<?>) any())).thenReturn(this.interfaceMeta);
        when(this.interfaceMeta.getNative()).thenReturn(mock(Pointer.class));
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        final long globalPointer = 13579L;
        ObjectCache.remove(globalPointer);
        when(this.waylandServerCore.wl_global_create(anyLong(),
                                                     anyLong(),
                                                     anyInt(),
                                                     anyLong(),
                                                     anyLong())).thenReturn(globalPointer);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        Whitebox.setInternalState(this.display,
                                  "pointer",
                                  987654321L);
        this.wlSeat = new WlSeat(this.display,
                                 this.wlDataDevice,
                                 this.seat,
                                 this.wlPointer,
                                 this.wlKeyboard,
                                 this.wlTouch);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        //when
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final WlSeatResource wlSeatResource = this.wlSeat.onBindClient(client,
                                                                       1,
                                                                       1);
        //then
        assertThat(wlSeatResource).isNotNull();
        assertThat(wlSeatResource.getImplementation()).isSameAs(this.wlSeat);
    }

    @Test
    public void testGetPointer() throws Exception {
        //given
        final int            id             = 20;
        final int            version        = 1;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);

        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(this.wlPointer.add(client,
                                version,
                                id)).thenReturn(wlPointerResource);
        this.wlSeat.getResources()
                   .add(wlSeatResource);
        //when
        this.wlSeat.getPointer(wlSeatResource,
                               id);
        //then
        verify(this.wlPointer).add(client,
                                   version,
                                   id);
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlPointerResource).register(listenerArgumentCaptor.capture());

        //and when
        final DestroyListener destroyListener = listenerArgumentCaptor.getValue();
        destroyListener.handle();

        //then
        assertThat(this.wlSeat.getWlPointerResource(wlSeatResource)
                              .isPresent()).isFalse();
    }

    @Test
    public void testGetKeyboard() throws Exception {
        //given
        final int            id             = 30;
        final int            version        = 2;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);
        final KeyboardDevice keyboardDevice = mock(KeyboardDevice.class);

        final WlKeyboardResource wlKeyboardResource = mock(WlKeyboardResource.class);
        when(this.wlKeyboard.add(client,
                                 version,
                                 id)).thenReturn(wlKeyboardResource);
        when(this.wlKeyboard.getKeyboardDevice()).thenReturn(keyboardDevice);

        this.wlSeat.getResources()
                   .add(wlSeatResource);
        //when
        this.wlSeat.getKeyboard(wlSeatResource,
                                id);
        //then
        verify(this.wlKeyboard).add(client,
                                    version,
                                    id);
        verify(keyboardDevice).emitKeymap(eq(Collections.singleton(wlKeyboardResource)));
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlKeyboardResource).register(listenerArgumentCaptor.capture());

        //and when
        final DestroyListener destroyListener = listenerArgumentCaptor.getValue();
        destroyListener.handle();

        //then
        assertThat(this.wlSeat.getWlKeyboardResource(wlSeatResource)
                              .isPresent()).isFalse();
    }

    @Test
    public void testGetTouch() throws Exception {
        //given
        final int            id             = 40;
        final int            version        = 5;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);

        final WlTouchResource wlTouchResource = mock(WlTouchResource.class);
        when(this.wlTouch.add(client,
                              version,
                              id)).thenReturn(wlTouchResource);
        this.wlSeat.getResources()
                   .add(wlSeatResource);
        //when
        this.wlSeat.getTouch(wlSeatResource,
                             id);
        //then
        verify(this.wlTouch).add(client,
                                 version,
                                 id);
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlTouchResource).register(listenerArgumentCaptor.capture());

        //and when
        final DestroyListener destroyListener = listenerArgumentCaptor.getValue();
        destroyListener.handle();

        //then
        assertThat(this.wlSeat.getWlTouchResource(wlSeatResource)
                              .isPresent()).isFalse();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int version = 2;
        final int id      = 7;
        //when
        final WlSeatResource wlSeatResource = this.wlSeat.create(client,
                                                                 version,
                                                                 id);
        //then
        assertThat(wlSeatResource).isNotNull();
        assertThat(wlSeatResource.getImplementation()).isSameAs(this.wlSeat);
    }
}