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
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerCore.class)
public class WlCallbackTest {

    @Mock
    private WaylandServerCore waylandServerCore;
    private WlCallback        wlCallback;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class);
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        this.wlCallback = new WlCallback();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  new Long(987654));
        final int version = 2;
        final int id      = 3;
        //when
        final WlCallbackResource wlCallbackResource = this.wlCallback.create(client,
                                                                             version,
                                                                             id);
        //then
        assertThat(wlCallbackResource).isNotNull();
        assertThat(wlCallbackResource.getImplementation()).isSameAs(this.wlCallback);
    }
}