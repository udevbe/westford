package org.westmalle.wayland.protocol;

import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        InterfaceMeta.class
                })
public class WlSeatTest {

    @Mock
    private Display display;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;
    @Mock
    private InterfaceMeta               interfaceMeta;
    @Mock
    private Pointer                     globalPointer;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class,
                                InterfaceMeta.class);
        when(InterfaceMeta.get((Class<?>) any())).thenReturn(this.interfaceMeta);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
        when(this.waylandServerLibraryMapping.wl_global_create(any(),
                                                               any(),
                                                               anyInt(),
                                                               any(),
                                                               any())).thenReturn(this.globalPointer);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Pointer resourcePointer = mock(Pointer.class);
        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
                                                                 any(),
                                                                 anyInt(),
                                                                 anyInt())).thenReturn(resourcePointer);
        final WlSeat wlSeat = new WlSeat(this.display);
        //when
        final WlSeatResource wlSeatResource = wlSeat.onBindClient(mock(Client.class),
                                                                  1,
                                                                  1);
        //then
        assertThat(wlSeatResource).isNotNull();
        assertThat(wlSeatResource.getImplementation()).isSameAs(wlSeat);
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
        final WlPointer wlPointer = mock(WlPointer.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);
        wlSeat.setWlPointer(wlPointer);
        //when
        wlSeat.getPointer(wlSeatResource,
                          id);
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.POINTER.getValue());
        verify(wlPointer).add(client,
                              version,
                              id);
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
        final WlKeyboard wlKeyboard = mock(WlKeyboard.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);
        wlSeat.setWlKeyboard(wlKeyboard);
        //when
        wlSeat.getKeyboard(wlSeatResource,
                           id);
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.KEYBOARD.getValue());
        verify(wlKeyboard).add(client,
                               version,
                               id);
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
        final WlTouch wlTouch = mock(WlTouch.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);
        wlSeat.setWlTouch(wlTouch);
        //when
        wlSeat.getTouch(wlSeatResource,
                        id);
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.TOUCH.getValue());
        verify(wlTouch).add(client,
                            version,
                            id);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final WlSeat wlSeat  = new WlSeat(this.display);
        final Client client  = mock(Client.class);
        final int    version = 2;
        final int    id      = 7;
        //when
        final WlSeatResource wlSeatResource = wlSeat.create(client,
                                                            version,
                                                            id);
        //then
        assertThat(wlSeatResource).isNotNull();
        assertThat(wlSeatResource.getImplementation()).isSameAs(wlSeat);
    }

    @Test
    public void testSetMultipleInputDevices() {
        //given
        final int            version        = 3;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);
        final WlTouch    wlTouch    = mock(WlTouch.class);
        final WlKeyboard wlKeyboard = mock(WlKeyboard.class);
        final WlPointer  wlPointer  = mock(WlPointer.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);
        //when
        wlSeat.setWlTouch(wlTouch);
        wlSeat.setWlKeyboard(wlKeyboard);
        wlSeat.setWlPointer(wlPointer);
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.TOUCH.getValue());
        verify(wlSeatResource).capabilities(WlSeatCapability.TOUCH.getValue() |
                                            WlSeatCapability.KEYBOARD.getValue());
        verify(wlSeatResource).capabilities(WlSeatCapability.TOUCH.getValue() |
                                            WlSeatCapability.KEYBOARD.getValue() |
                                            WlSeatCapability.POINTER.getValue());
    }

    @Test
    public void testRemoveWlPointer() throws Exception {
        //given
        final int            version        = 3;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);
        final WlPointer wlPointer = mock(WlPointer.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);

        wlSeat.setWlPointer(wlPointer);
        //when
        wlSeat.removeWlPointer();
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.POINTER.getValue());
        verify(wlSeatResource).capabilities(0);
    }

    @Test
    public void testRemoveWlKeyboard() throws Exception {
        //given
        final int            version        = 3;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);
        final WlKeyboard wlKeyboard = mock(WlKeyboard.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);

        wlSeat.setWlKeyboard(wlKeyboard);
        //when
        wlSeat.removeWlKeyboard();
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.KEYBOARD.getValue());
        verify(wlSeatResource).capabilities(0);
    }

    @Test
    public void testRemoveWlTouch() throws Exception {
        //given
        final int            version        = 3;
        final Client         client         = mock(Client.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        when(wlSeatResource.getClient()).thenReturn(client);
        when(wlSeatResource.getVersion()).thenReturn(version);
        final WlTouch wlTouch = mock(WlTouch.class);

        final WlSeat wlSeat = new WlSeat(this.display);
        wlSeat.getResources()
              .add(wlSeatResource);

        wlSeat.setWlTouch(wlTouch);
        //when
        wlSeat.removeWlTouch();
        //then
        verify(wlSeatResource).capabilities(WlSeatCapability.TOUCH.getValue());
        verify(wlSeatResource).capabilities(0);
    }
}