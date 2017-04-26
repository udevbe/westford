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

import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westford.compositor.protocol.WlSurface;

import java.util.Collections;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CursorTest {

    @Mock
    private Surface   surface;
    @Mock
    private WlSurface wlSurface;

    @Mock
    private WlSurfaceResource wlSurfaceResource;
    @Mock
    private Point             hotspot;
    @InjectMocks
    private Cursor            cursor;

    @Before
    public void setUp() {
        when(this.wlSurfaceResource.getImplementation()).thenReturn(this.wlSurface);
        when(this.wlSurface.getSurface()).thenReturn(this.surface);
    }

    @Test
    public void testHide() throws Exception {
        //given
        final SurfaceState surfaceState = SurfaceState.Companion.builder()
                                                                .build();
        when(this.surface.getState()).thenReturn(surfaceState);
        //when
        this.cursor.hide();
        //then
        verify(this.surface).setState(eq(SurfaceState.Companion.builder()
                                                               .buffer(Optional.empty())
                                                               .build()));
        assertThat(this.cursor.isHidden()).isTrue();
    }

    @Test
    public void testUpdatePosition() throws Exception {
        //given
        final Point point = Point.Companion.create(123,
                                                   456);
        final SurfaceView surfaceView = mock(SurfaceView.class);
        when(surface.getViews()).thenReturn(Collections.singleton(surfaceView));
        //when
        this.cursor.updatePosition(point);
        //then
        verify(surfaceView).setPosition(eq(Point.Companion.create(123,
                                                                  456)));
    }
}