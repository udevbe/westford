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

import org.freedesktop.wayland.server.WlOutputResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OutputTest {

    @Mock
    private OutputGeometry outputGeometry;
    @Mock
    private OutputMode     outputMode;
    @Mock
    private Object         platformImplementation;
    @InjectMocks
    private Output         output;

    @Test
    public void testUpdateMode() throws Exception {
        //given
        final Set<WlOutputResource> wlOutputResources = new HashSet<>();
        final WlOutputResource      wlOutputResource0 = mock(WlOutputResource.class);
        final WlOutputResource      wlOutputResource1 = mock(WlOutputResource.class);
        wlOutputResources.add(wlOutputResource0);
        wlOutputResources.add(wlOutputResource1);

        final OutputMode outputMode = mock(OutputMode.class);
        final int        flags      = 12345;
        final int        width      = 987;
        final int        height     = 654;
        final int        refresh    = 123;
        when(outputMode.getFlags()).thenReturn(flags);
        when(outputMode.getWidth()).thenReturn(width);
        when(outputMode.getHeight()).thenReturn(height);
        when(outputMode.getRefresh()).thenReturn(refresh);

        //when
        this.output.update(wlOutputResources,
                           outputMode);

        //then
        verify(wlOutputResource0).mode(flags,
                                       width,
                                       height,
                                       refresh);
        verify(wlOutputResource1).mode(flags,
                                       width,
                                       height,
                                       refresh);
    }

    @Test
    public void testUpdateGeometry() throws Exception {
        //given
        final Set<WlOutputResource> wlOutputResources = new HashSet<>();
        final WlOutputResource      wlOutputResource0 = mock(WlOutputResource.class);
        final WlOutputResource      wlOutputResource1 = mock(WlOutputResource.class);
        wlOutputResources.add(wlOutputResource0);
        wlOutputResources.add(wlOutputResource1);

        final OutputGeometry outputGeometry = mock(OutputGeometry.class);
        final int            x              = 1;
        final int            y              = 2;
        final int            physicalWidth  = 34;
        final int            physicalHeight = 56;
        final int            subpixel       = 7;
        final String         make           = "make";
        final String         model          = "model";
        final int            transform      = 8;
        when(outputGeometry.getX()).thenReturn(x);
        when(outputGeometry.getY()).thenReturn(y);
        when(outputGeometry.getPhysicalWidth()).thenReturn(physicalWidth);
        when(outputGeometry.getPhysicalHeight()).thenReturn(physicalHeight);
        when(outputGeometry.getSubpixel()).thenReturn(subpixel);
        when(outputGeometry.getMake()).thenReturn(make);
        when(outputGeometry.getModel()).thenReturn(model);
        when(outputGeometry.getTransform()).thenReturn(transform);

        //when
        this.output.update(wlOutputResources,
                           outputGeometry);

        //then
        verify(wlOutputResource0).geometry(x,
                                           y,
                                           physicalWidth,
                                           physicalHeight,
                                           subpixel,
                                           make,
                                           model,
                                           transform);
        verify(wlOutputResource1).geometry(x,
                                           y,
                                           physicalWidth,
                                           physicalHeight,
                                           subpixel,
                                           make,
                                           model,
                                           transform);
    }
}