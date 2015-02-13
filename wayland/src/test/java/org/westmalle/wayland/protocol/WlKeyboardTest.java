package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.Keyboard;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        //following classes have static methods, so we have to powermock them:
        WaylandServerLibrary.class,
})
public class WlKeyboardTest {

    @Mock
    private Keyboard keyboard;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testRelease() throws Exception {
        //given
        WlKeyboardResource wlKeyboardResource = mock(WlKeyboardResource.class);
        WlKeyboard wlKeyboard = new WlKeyboard(keyboard);
        //when
        wlKeyboard.release(wlKeyboardResource);
        //then
        verify(wlKeyboardResource,
               times(1)).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 4;
        final int id = 4;
        WlKeyboard wlKeyboard = new WlKeyboard(keyboard);
        //when
        final WlKeyboardResource wlKeyboardResource = wlKeyboard.create(client,
                                                                        version,
                                                                        id);
        //then
        assertThat(wlKeyboardResource).isNotNull();
        assertThat(wlKeyboardResource.getImplementation()).isSameAs(wlKeyboard);
    }
}