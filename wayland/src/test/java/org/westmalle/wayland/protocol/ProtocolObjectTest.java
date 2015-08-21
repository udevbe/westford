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
package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class ProtocolObjectTest {

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;
    private ProtocolObjectDummy         protocolObject;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
        this.protocolObject = new ProtocolObjectDummy();
    }

    @Test
    public void testAddNone() throws Exception {
        //given
        //when
        //then
        assertThat((Iterable) this.protocolObject.getResources()).isEmpty();
    }

    @Test
    public void testAddSingle() throws Exception {
        //given
        final Client client = mock(Client.class);
        //when
        final Resource<?> resource = this.protocolObject.add(client,
                                                             1,
                                                             1);
        //then
        assertThat((Iterable) this.protocolObject.getResources()).contains(resource);
        assertThat((Iterable) this.protocolObject.getResources()).hasSize(1);
    }

    @Test
    public void testAddMultiple() throws Exception {
        //given
        final Client client = mock(Client.class);
        //when
        final Resource<?> resource0 = this.protocolObject.add(client,
                                                              1,
                                                              1);
        final Resource<?> resource1 = this.protocolObject.add(client,
                                                              1,
                                                              2);
        final Resource<?> resource2 = this.protocolObject.add(client,
                                                              1,
                                                              3);
        //then
        assertThat((Iterable) this.protocolObject.getResources()).contains(resource0);
        assertThat((Iterable) this.protocolObject.getResources()).contains(resource1);
        assertThat((Iterable) this.protocolObject.getResources()).contains(resource2);
        assertThat((Iterable) this.protocolObject.getResources()).hasSize(3);
    }

    @Test
    public void testDestroyed() {
        //given
        final Client client = mock(Client.class);
        //when
        final Resource<?> resource = this.protocolObject.add(client,
                                                             1,
                                                             1);
        //then
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(resource).register(listenerArgumentCaptor.capture());

        //and when
        final DestroyListener listener = listenerArgumentCaptor.getValue();
        listener.handle();

        //then
        assertThat((Iterable<Resource<?>>) this.protocolObject.getResources()).doesNotContain(resource);
    }
}