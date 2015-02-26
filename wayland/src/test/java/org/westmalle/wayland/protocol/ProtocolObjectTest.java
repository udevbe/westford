package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class ProtocolObjectTest {

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testGetResourceSingleResource() throws Exception {
        //given
        final Client client = mock(Client.class);
        //when
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        final Resource<?> resource = protocolObject.add(client,
                                                        1,
                                                        1);
        //then
        assertThat(protocolObject.getResource()).isEqualTo(Optional.of(resource));
    }

    @Test
    public void testGetResourceNoResource() throws Exception {
        //given
        //when
        //then
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        assertThat(protocolObject.getResource()).isEqualTo(Optional.empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetResourceMultipleResourceResource() throws Exception {
        //given
        final Client client = mock(Client.class);
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        protocolObject.add(client,
                           1,
                           1);
        protocolObject.add(client,
                           1,
                           2);
        //when
        protocolObject.getResource();
        //then
    }

    @Test
    public void testAddNone() throws Exception {
        //given
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        //when
        //then
        assertThat((Iterable) protocolObject.getResources()).isEmpty();
    }

    @Test
    public void testAddSingle() throws Exception {
        //given
        final Client client = mock(Client.class);
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        //when
        final Resource<?> resource = protocolObject.add(client,
                                                        1,
                                                        1);
        //then
        assertThat((Iterable) protocolObject.getResources()).contains(resource);
        assertThat((Iterable) protocolObject.getResources()).hasSize(1);
    }

    @Test
    public void testAddMultiple() throws Exception {
        //given
        final Client client = mock(Client.class);
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        //when
        final Resource<?> resource0 = protocolObject.add(client,
                                                         1,
                                                         1);
        final Resource<?> resource1 = protocolObject.add(client,
                                                         1,
                                                         2);
        final Resource<?> resource2 = protocolObject.add(client,
                                                         1,
                                                         3);
        //then
        assertThat((Iterable) protocolObject.getResources()).contains(resource0);
        assertThat((Iterable) protocolObject.getResources()).contains(resource1);
        assertThat((Iterable) protocolObject.getResources()).contains(resource2);
        assertThat((Iterable) protocolObject.getResources()).hasSize(3);
    }

    @Test
    public void testResourceDestroyed() {
        //given
        final Client client = mock(Client.class);
        final ProtocolObject<Resource<?>> protocolObject = new ProtocolObjectDummy();
        //when
        final Resource<?> resource = protocolObject.add(client,
                                                        1,
                                                        1);
        //then
        final ArgumentCaptor<Listener> destroyListenerCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(resource,
               times(1)).addDestroyListener(destroyListenerCaptor.capture());
        //and when
        final Listener destroyListener = destroyListenerCaptor.getValue();
        destroyListener.handle();
        //then
        assertThat((Iterable) protocolObject.getResources()).isEmpty();
    }
}