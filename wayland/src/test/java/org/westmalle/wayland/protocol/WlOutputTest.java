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

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlOutputResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westmalle.wayland.core.Output;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerCore.class,
                        InterfaceMeta.class
                })
public class WlOutputTest {

    @Mock
    private WaylandServerCore waylandServerCore;
    @Mock
    private InterfaceMeta     interfaceMeta;

    @Mock
    private Display display;
    @Mock
    private Output  output;

    private WlOutput wlOutput;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class,
                                InterfaceMeta.class);
        when(InterfaceMeta.get((Class<?>) any())).thenReturn(this.interfaceMeta);
        when(this.interfaceMeta.getNative()).thenReturn(mock(Pointer.class));
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        final long globalPointer = 13579L;
        ObjectCache.remove(globalPointer);
        when(this.waylandServerCore.wl_global_create(anyLong(),
                                                     anyLong(),
                                                     anyInt(),
                                                     anyLong(),
                                                     anyLong())).thenReturn(globalPointer);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        Whitebox.setInternalState(this.display,
                                  "pointer",
                                  987654321L);
        when(this.waylandServerCore.wl_resource_get_version(anyLong())).thenReturn(2);
        this.wlOutput = new WlOutput(this.display,
                                     this.output);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        when(this.output.notifyGeometry(any())).thenReturn(this.output);
        when(this.output.notifyMode(any())).thenReturn(this.output);

        //when
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final WlOutputResource wlOutputResource = this.wlOutput.onBindClient(client,
                                                                             1,
                                                                             1);
        //then
        assertThat(wlOutputResource).isNotNull();
        assertThat(wlOutputResource.getImplementation()).isSameAs(this.wlOutput);
        verify(this.output).notifyGeometry(wlOutputResource);
        verify(this.output).notifyMode(wlOutputResource);
    }
}