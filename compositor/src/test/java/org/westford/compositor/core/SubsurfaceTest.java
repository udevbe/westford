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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westford.compositor.protocol.WlSurface;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubsurfaceTest {

    @Mock
    private Scene             scene;
    @Mock
    private WlSurfaceResource parentWlSurfaceResource;
    @Mock
    private WlSurfaceResource wlSurfaceResource;
    @Mock
    private SurfaceState      surfaceState;

    private Subsurface subsurface;

    @Before
    public void setUp() {
        //mockito doesn't properly inject the member so we have to create our subsurface manually.
        this.subsurface = new Subsurface(this.parentWlSurfaceResource,
                                         Sibling.create(this.wlSurfaceResource),
                                         this.surfaceState,
                                         this.surfaceState);
    }

    @Test
    public void testSetPositionInert() throws Exception {
        //given: an inert subsurface
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface   surface   = mock(Surface.class);

        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);

        this.subsurface.setInert();
        final Point position = Point.create(123,
                                            456);

        //when: a new position is set
        this.subsurface.setPosition(position);

        //then: nothing happens
        assertThat(this.subsurface.getSibling()
                                  .getPosition()).isNotEqualTo(position);
    }

    //TODO fix
//    @Test
//    public void testApplyPosition() throws Exception {
//        //given: a subsurface, a parent surface
//        final WlSurface wlSurface = mock(WlSurface.class);
//        final Surface   surface   = mock(Surface.class);
//
//        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//
//        final WlSurface   parentWlSurface   = mock(WlSurface.class);
//        final Surface     parentSurface     = mock(Surface.class);
//        final SurfaceView parentSurfaceView = mock(SurfaceView.class);
//
//        when(this.parentWlSurfaceResource.getImplementation()).thenReturn(parentWlSurface);
//        when(parentWlSurface.getSurface()).thenReturn(parentSurface);
//        when(parentSurface.getViews()).thenReturn(Collections.singleton(parentSurfaceView));
//
//        final Point globalSubsurfacePosition = mock(Point.class);
//        when(parentSurfaceView.global(this.subsurface.getSibling()
//                                                     .getPosition())).thenReturn(globalSubsurfacePosition);
//
//        //when: the subsurface position is applied
//        this.subsurface.applyPosition();
//
//        //then: the subsurface position is applied based on the parent surface space.
//        verify(surface).setPosition(globalSubsurfacePosition);
//    }

    //TODO fix
//    @Test
//    public void testApplyPositionInert() throws Exception {
//        //given: an inert subsurface
//        final WlSurface wlSurface = mock(WlSurface.class);
//        final Surface   surface   = mock(Surface.class);
//
//        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//
//        this.subsurface.setInert();
//
//        //when: the subsurface position is applied
//        this.subsurface.applyPosition();
//
//        //then: nothing happens
//        verifyZeroInteractions(surface);
//    }

    @Test
    public void testBeforeCommitSync() throws Exception {
        //given: a subsurface in sync mode
        final WlSurface    wlSurface    = mock(WlSurface.class);
        final Surface      surface      = mock(Surface.class);
        final SurfaceState surfaceState = mock(SurfaceState.class);

        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getState()).thenReturn(surfaceState);

        final SurfaceState newSurfaceState = mock(SurfaceState.class);
        this.subsurface.apply(newSurfaceState);

        //when: the before commit hook is called
        this.subsurface.beforeCommit(this.wlSurfaceResource);

        //then: the cached surface state is restored
        verify(surface).setState(newSurfaceState);
    }

    @Test
    public void testBeforeCommitDesync() throws Exception {
        //given: a subsurface in desync mode
        final WlSurface    wlSurface    = mock(WlSurface.class);
        final Surface      surface      = mock(Surface.class);
        final SurfaceState surfaceState = mock(SurfaceState.class);


        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getState()).thenReturn(surfaceState);

        final WlSurface  parentWlSurface  = mock(WlSurface.class);
        final Surface    parentSurface    = mock(Surface.class);
        final Subsurface parentSubsurface = mock(Subsurface.class);

        when(this.parentWlSurfaceResource.getImplementation()).thenReturn(parentWlSurface);
        when(parentWlSurface.getSurface()).thenReturn(parentSurface);
        when(parentSurface.getRole()).thenReturn(Optional.of(parentSubsurface));
        when(parentSubsurface.isEffectiveSync()).thenReturn(false);

        final SurfaceState newSurfaceState = mock(SurfaceState.class);
        this.subsurface.apply(newSurfaceState);

        this.subsurface.setSync(false);

        //when: the before commit hook is called
        this.subsurface.beforeCommit(this.wlSurfaceResource);

        //then: no cached state was set
        verify(surface,
               times(0)).setState(newSurfaceState);
    }

    @Test
    public void testBeforeCommitInert() throws Exception {
        //given: an inert subsurface
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface   surface   = mock(Surface.class);

        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);

        this.subsurface.setInert();

        //when: the before commit hook is called
        this.subsurface.beforeCommit(this.wlSurfaceResource);

        //then: nothing happens
        verifyZeroInteractions(surface);
    }

    @Test
    public void testCommitSync() throws Exception {
        //given: a subsurface in sync mode
        final WlSurface    wlSurface    = mock(WlSurface.class);
        final Surface      surface      = mock(Surface.class);
        final SurfaceState surfaceState = mock(SurfaceState.class);

        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getState()).thenReturn(surfaceState);

        final SurfaceState newSurfaceState = mock(SurfaceState.class);

        //when: the commit hook is called
        this.subsurface.apply(newSurfaceState);

        //then: the surface new state is cached and the surface is reset to the pre-commit state.
        verify(surface).apply(this.surfaceState);
    }

    //TODO fix
//    @Test
//    public void testCommitDesync() throws Exception {
//        //given: a subsurface in desync mode
//        final WlSurface wlSurface = mock(WlSurface.class);
//        final Surface   surface   = mock(Surface.class);
//
//        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
//        when(wlSurface.getSurface()).thenReturn(surface);
//
//        final WlSurface  parentWlSurface  = mock(WlSurface.class);
//        final Surface    parentSurface    = mock(Surface.class);
//        final Subsurface parentSubsurface = mock(Subsurface.class);
//
//        when(this.parentWlSurfaceResource.getImplementation()).thenReturn(parentWlSurface);
//        when(parentWlSurface.getSurface()).thenReturn(parentSurface);
//        when(parentSurface.getRole()).thenReturn(Optional.of(parentSubsurface));
//        when(parentSubsurface.isEffectiveSync()).thenReturn(false);
//
//        this.subsurface.setSync(false);
//
//        //when: the commit hook is called
//        final SurfaceState newSurfaceState = mock(SurfaceState.class);
//        this.subsurface.apply(newSurfaceState);
//
//        //then: the cached state is updated to the current surface state
//        assertThat(this.subsurface.getCachedSurfaceState()).isEqualTo(newSurfaceState);
//    }

    @Test
    public void testCommitInert() throws Exception {
        //given: an inert subsurface
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface   surface   = mock(Surface.class);

        when(this.wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);

        this.subsurface.setInert();

        //when: the commit hook is called
        final SurfaceState newSurfaceState = mock(SurfaceState.class);
        this.subsurface.apply(newSurfaceState);

        //then: nothing happens
        verifyZeroInteractions(surface);
    }

    @Test
    public void testParentCommitSync() throws Exception {
        //TODO

        //given: a subsurface in sync mode, a parent surface
        //when: the parent commit hook is called
        //then: the cached state is applied on the surface, surface position is applied
    }

    @Test
    public void testParentCommitSyncNoSurfaceStateUpdate() throws Exception {
        //TODO

        //given: a subsurface in sync mode, a parent surface, no surface state changes
        //when: the parent commit hook is called
        //then: no surface state update is applied, surface position is applied
    }

    @Test
    public void testParentCommitDesync() throws Exception {
        //TODO

        //given: a subsurface in desync mode, a parent surface
        //when: the parent commit hook is called
        //then: no surface state update is applied, surface position is applied
    }

    @Test
    public void testParentCommitInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: the parent commit hook is called
        //then: nothing happens
    }

    @Test
    public void testSetSyncInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: set sync is called
        //then: nothing happens
    }

    @Test
    public void testSetDesyncInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: set desync is called
        //then: nothing happens
    }

    @Test
    public void testAbove() throws Exception {
        //TODO

        //given: a subsurface, a sibling or parents surface
        //when: set above is called
        //then: the pending subsurface stack is updated.
    }

    @Test
    public void testAboveInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: set above is called
        //then: nothing happens
    }

    @Test
    public void testBelow() throws Exception {
        //TODO

        //given: a subsurface, a sibling or parents surface
        //when: set below is called
        //then: the pending subsurface stack is updated.
    }

    @Test
    public void testBelowInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: set below is called
        //then: nothing happens
    }

    @Test
    public void testIsInert() throws Exception {
        //TODO

        //given: a subsurface, a parent surface
        //when: the parent surface is destroyed
        //then: the subsurface becomes inert.
    }
}