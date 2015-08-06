package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.NativeFileFactory;
import org.westmalle.wayland.nativ.libc.Libc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class KeyboardDeviceTest {

    @Mock
    private Display           display;
    @Mock
    private NativeFileFactory nativeFileFactory;
    @Mock
    private Libc              libc;
    @Mock
    private Compositor        compositor;
    @InjectMocks
    private KeyboardDevice    keyboardDevice;

    @Test
    public void testKey() throws Exception {
        //given
        final WlKeyboardResource      wlKeyboardResource0 = mock(WlKeyboardResource.class);
        final WlKeyboardResource      wlKeyboardResource1 = mock(WlKeyboardResource.class);
        final Set<WlKeyboardResource> wlKeyboardResources = new HashSet<>();
        wlKeyboardResources.add(wlKeyboardResource0);
        wlKeyboardResources.add(wlKeyboardResource1);

        //TODO set keyboard focus

        final int                key                = 123;
        final WlKeyboardKeyState wlKeyboardKeyState = WlKeyboardKeyState.PRESSED;

        //when
        this.keyboardDevice.key(wlKeyboardResources,
                                key,
                                wlKeyboardKeyState);

        //then

        throw new UnsupportedOperationException();
    }

    @Test
    public void testSetFocus() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testUpdateKeymap() throws Exception {
        throw new UnsupportedOperationException();
    }
}