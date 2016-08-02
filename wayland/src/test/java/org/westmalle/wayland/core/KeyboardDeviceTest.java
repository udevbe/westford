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

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.events.KeyboardFocusGained;
import org.westmalle.wayland.core.events.KeyboardFocusLost;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.NativeFileFactory;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEY_DOWN;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_LAYOUT_EFFECTIVE;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_DEPRESSED;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_LATCHED;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_LOCKED;

@RunWith(MockitoJUnitRunner.class)
public class KeyboardDeviceTest {

    @Mock
    private Display           display;
    @Mock
    private NativeFileFactory nativeFileFactory;
    @Mock
    private Libc              libc;
    @Mock
    private Libxkbcommon      libxkbcommon;
    @Mock
    private Xkb               xkb;
    @InjectMocks
    private KeyboardDevice    keyboardDevice;

    @Test
    public void testModifierKey() throws Exception {
        //given
        final Client client0 = mock(Client.class);

        final WlKeyboardResource      wlKeyboardResource0 = mock(WlKeyboardResource.class);
        final Set<WlKeyboardResource> wlKeyboardResources = new HashSet<>();
        wlKeyboardResources.add(wlKeyboardResource0);

        when(wlKeyboardResource0.getClient()).thenReturn(client0);

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Set<WlKeyboardResource> keyboardFocuses = new HashSet<>();
        when(surface.getKeyboardFocuses()).thenReturn(keyboardFocuses);
        final Signal<KeyboardFocusGained, Slot<KeyboardFocusGained>> keyboardFocusGainedSignal = mock(Signal.class);
        when(surface.getKeyboardFocusGainedSignal()).thenReturn(keyboardFocusGainedSignal);
        when(wlSurfaceResource.getClient()).thenReturn(client0);
        this.keyboardDevice.setFocus(Collections.singleton(wlKeyboardResource0),
                                     Optional.of(wlSurfaceResource));

        final int                key                       = 123;
        final WlKeyboardKeyState wlKeyboardKeyStatePressed = WlKeyboardKeyState.PRESSED;

        final int serial0 = 1278;
        final int serial1 = 1279;
        final int serial2 = 1280;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        final int  time0    = 27646;
        final long xkbState = 58746;
        when(this.xkb.getState()).thenReturn(xkbState);
        when(this.libxkbcommon.xkb_state_update_key(xkbState,
                                                    //xkb expects evdev key, which has an offset of 8
                                                    key + 8,
                                                    XKB_KEY_DOWN)).thenReturn(XKB_STATE_MODS_DEPRESSED);

        final int modsDepressed = 10;
        when(this.libxkbcommon.xkb_state_serialize_mods(this.xkb.getState(),
                                                        XKB_STATE_MODS_DEPRESSED)).thenReturn(modsDepressed);
        final int modsLatched = 20;
        when(this.libxkbcommon.xkb_state_serialize_mods(this.xkb.getState(),
                                                        XKB_STATE_MODS_LATCHED)).thenReturn(modsLatched);
        final int modsLocked = 30;
        when(this.libxkbcommon.xkb_state_serialize_mods(this.xkb.getState(),
                                                        XKB_STATE_MODS_LOCKED)).thenReturn(modsLocked);
        final int group = 40;
        when(this.libxkbcommon.xkb_state_serialize_layout(this.xkb.getState(),
                                                          XKB_STATE_LAYOUT_EFFECTIVE)).thenReturn(group);

        //when
        this.keyboardDevice.key(wlKeyboardResources,
                                time0,
                                key,
                                wlKeyboardKeyStatePressed);

        //then
        verify(wlKeyboardResource0).key(serial0,
                                        time0,
                                        key,
                                        wlKeyboardKeyStatePressed.value);
        verify(wlKeyboardResource0).modifiers(serial1,
                                              modsDepressed,
                                              modsLatched,
                                              modsLocked,
                                              group);
    }

    @Test
    public void testKey() throws Exception {
        //given
        final Client client0 = mock(Client.class);
        final Client client1 = mock(Client.class);

        final WlKeyboardResource wlKeyboardResource0 = mock(WlKeyboardResource.class);
        final WlKeyboardResource wlKeyboardResource1 = mock(WlKeyboardResource.class);

        final Set<WlKeyboardResource> wlKeyboardResources = new HashSet<>();
        wlKeyboardResources.add(wlKeyboardResource0);
        wlKeyboardResources.add(wlKeyboardResource1);

        when(wlKeyboardResource0.getClient()).thenReturn(client0);
        when(wlKeyboardResource1.getClient()).thenReturn(client1);

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Set<WlKeyboardResource> keyboardFocuses = new HashSet<>();
        when(surface.getKeyboardFocuses()).thenReturn(keyboardFocuses);
        final Signal<KeyboardFocusGained, Slot<KeyboardFocusGained>> keyboardFocusGainedSignal = mock(Signal.class);
        when(surface.getKeyboardFocusGainedSignal()).thenReturn(keyboardFocusGainedSignal);

        when(wlSurfaceResource.getClient()).thenReturn(client0);
        this.keyboardDevice.setFocus(Collections.singleton(wlKeyboardResource0),
                                     Optional.of(wlSurfaceResource));

        final int                key                        = 123;
        final WlKeyboardKeyState wlKeyboardKeyStatePressed  = WlKeyboardKeyState.PRESSED;
        final WlKeyboardKeyState wlKeyboardKeyStateReleased = WlKeyboardKeyState.RELEASED;

        final int serial0 = 1278;
        final int serial1 = 1279;
        final int serial2 = 1280;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        final int time0 = 27646;
        final int time1 = 29253;
        final int time2 = 30898;
        //when
        this.keyboardDevice.key(wlKeyboardResources,
                                time0,
                                key,
                                wlKeyboardKeyStatePressed);

        //then
        assertThat((Iterable<Integer>) this.keyboardDevice.getPressedKeys()).contains(key);
        verify(wlKeyboardResource0).key(serial0,
                                        time0,
                                        key,
                                        wlKeyboardKeyStatePressed.value);
        verify(keyboardFocusGainedSignal).emit(any());

        //and when
        this.keyboardDevice.key(wlKeyboardResources,
                                time1,
                                key,
                                wlKeyboardKeyStateReleased);

        //then
        assertThat(this.keyboardDevice.getPressedKeys()).doesNotContain(key);
        verify(wlKeyboardResource0).key(serial1,
                                        time1,
                                        key,
                                        wlKeyboardKeyStateReleased.value);

    }

    @Test
    public void testSetFocus() throws Exception {
        //given
        final Client client0 = mock(Client.class);

        final WlKeyboardResource wlKeyboardResource0 = mock(WlKeyboardResource.class);
        when(wlKeyboardResource0.getClient()).thenReturn(client0);

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Set<WlKeyboardResource> keyboardFocuses = new HashSet<>();
        when(surface.getKeyboardFocuses()).thenReturn(keyboardFocuses);
        final Signal<KeyboardFocusGained, Slot<KeyboardFocusGained>> keyboardFocusGainedSignal = mock(Signal.class);
        when(surface.getKeyboardFocusGainedSignal()).thenReturn(keyboardFocusGainedSignal);
        when(wlSurfaceResource.getClient()).thenReturn(client0);
        final Signal<KeyboardFocusLost, Slot<KeyboardFocusLost>> keyboardFocusLostSignal = mock(Signal.class);
        when(surface.getKeyboardFocusLostSignal()).thenReturn(keyboardFocusLostSignal);
        when(wlSurfaceResource.getClient()).thenReturn(client0);


        final int serial0 = 1278;
        final int serial1 = 1279;
        final int serial2 = 1280;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        //when
        this.keyboardDevice.setFocus(Collections.singleton(wlKeyboardResource0),
                                     Optional.of(wlSurfaceResource));

        //then
        verify(wlKeyboardResource0).enter(eq(serial0),
                                          eq(wlSurfaceResource),
                                          any());
        //and when
        this.keyboardDevice.setFocus(Collections.singleton(wlKeyboardResource0),
                                     Optional.empty());

        //then
        verify(wlKeyboardResource0).leave(eq(serial1),
                                          eq(wlSurfaceResource));
    }

    @Test
    public void testEmitKeymap() throws Exception {
        //given
        final long pointer = 976435;
        when(this.libc.mmap(anyLong(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt())).thenReturn(pointer);
        final Client client0 = mock(Client.class);

        final WlKeyboardResource wlKeyboardResource0 = mock(WlKeyboardResource.class);
        when(wlKeyboardResource0.getClient()).thenReturn(client0);

        final String keymapString = "foo keymap";
        when(this.xkb.getKeymapString()).thenReturn(keymapString);

        this.keyboardDevice.updateKeymap();
        //when
        this.keyboardDevice.emitKeymap(Collections.singleton(wlKeyboardResource0));

        //then
        verify(wlKeyboardResource0).keymap(WlKeyboardKeymapFormat.XKB_V1.value,
                                           0,
                                           keymapString.length());
    }
}