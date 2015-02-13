package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.PointerDevice;

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
public class WlPointerTest {

    @Mock
    private PointerDevice pointerDevice;

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
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final WlPointer wlPointer = new WlPointer(this.pointerDevice);
        //when
        wlPointer.release(wlPointerResource);
        //then
        verify(wlPointerResource,
               times(1)).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 1;
        final WlPointer wlPointer = new WlPointer(this.pointerDevice);
        //when
        final WlPointerResource wlPointerResource = wlPointer.create(client,
                                                                     version,
                                                                     id);
        //then
        assertThat(wlPointerResource).isNotNull();
        assertThat(wlPointerResource.getImplementation()).isSameAs(wlPointer);
    }
}