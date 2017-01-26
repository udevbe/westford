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
package org.westford.compositor.core;

import org.freedesktop.wayland.server.EventSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventSource.class)
public class CompositorTest {

    @Mock
    private RenderPlatform renderPlatform;

    @InjectMocks
    private Compositor compositor;

    @Test
    public void testRequestRender() throws Exception {
        //given
        final RenderOutput renderOutput0 = mock(RenderOutput.class);
        final RenderOutput renderOutput1 = mock(RenderOutput.class);
        final List<? extends RenderOutput> renderOutputs = Arrays.asList(renderOutput0,
                                                                         renderOutput1);
        when(this.renderPlatform.getWlOutputs()).thenReturn((List) renderOutputs);

        //when
        this.compositor.requestRender();

        //then
        verify(renderOutput0).render();
        verify(renderOutput1).render();
    }
}