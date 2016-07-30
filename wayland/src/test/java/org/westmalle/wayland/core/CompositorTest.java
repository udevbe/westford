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
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.EventSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventSource.class)
public class CompositorTest {

    @Mock
    private Platform platform;

    @InjectMocks
    private Compositor compositor;

    @Test
    public void testRequestRender() throws Exception {
        //given
        Connector connector0 = mock(Connector.class);
        Connector connector1 = mock(Connector.class);
        final List<? extends Optional<? extends Connector>> connectors = Arrays.asList(Optional.of(connector0),
                                                                                       Optional.empty(),
                                                                                       Optional.of(connector1));
        when(platform.getConnectors()).thenReturn((List) connectors);

        //when
        this.compositor.requestRender();

        //then
        verify(connector0).render();
        verify(connector1).render();
    }
}