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
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class
                })
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
        WlTouchResource wlTouchResource = mock(WlTouchResource.class);

        WlTouch wlTouch = new WlTouch();
        //when
        wlTouch.release(wlTouchResource);
        //then
        verify(wlTouchResource,
               times(1)).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 1;

        WlTouch wlTouch = new WlTouch();
        //when
        final WlTouchResource wlTouchResource = wlTouch.create(client,
                                                               version,
                                                               id);
        //then
        assertThat(wlTouchResource).isNotNull();
        assertThat(wlTouchResource.getImplementation()).isSameAs(wlTouch);
    }
}