package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlTouchResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class WlTouchTest {

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
        final WlTouchResource wlTouchResource = mock(WlTouchResource.class);

        final WlTouch wlTouch = new WlTouch();
        //when
        wlTouch.release(wlTouchResource);
        //then
        verify(wlTouchResource).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 1;

        final WlTouch wlTouch = new WlTouch();
        //when
        final WlTouchResource wlTouchResource = wlTouch.create(client,
                                                               version,
                                                               id);
        //then
        assertThat(wlTouchResource).isNotNull();
        assertThat(wlTouchResource.getImplementation()).isSameAs(wlTouch);
    }
}