package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.wlshell.ShellSurface;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class WlShellSurfaceTest {

    @Mock
    private ShellSurface                shellSurface;
    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testMove() throws Exception {
        //TODO
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 1;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        //when
        final WlShellSurfaceResource wlShellSurfaceResource = wlShellSurface.create(client,
                                                                                    version,
                                                                                    id);
        //then
        assertThat(wlShellSurfaceResource).isNotNull();
    }

    @Test
    public void testResize() throws Exception {
        //TODO
    }

    @Test
    public void testPong() {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final int serial = 7654;

        //when
        wlShellSurface.pong(wlShellSurfaceResource,
                            serial);

        //then
        verify(this.shellSurface).pong(wlShellSurfaceResource,
                                       serial);
    }

    @Test
    public void testSetTitle() {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final String title = "appTitle";

        //when
        wlShellSurface.setTitle(wlShellSurfaceResource,
                                title);

        //then
        verify(this.shellSurface).setTitle(Optional.of(title));
    }

    @Test
    public void testSetClass() {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final String clazz = "appClass";

        //when
        wlShellSurface.setClass(wlShellSurfaceResource,
                                clazz);

        //then
        verify(this.shellSurface).setClazz(Optional.of(clazz));
    }
}