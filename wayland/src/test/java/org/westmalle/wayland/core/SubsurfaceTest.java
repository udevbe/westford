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

import org.junit.Test;


public class SubsurfaceTest {

    @Test
    public void testSetPosition() throws Exception {
        //TODO

        //given: a subsurface
        //when: a new position is set
        //then: the position is stored but not applied.
    }

    @Test
    public void testSetPositionInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: a new position is set
        //then: nothing happens
    }

    @Test
    public void testApplyPosition() throws Exception {
        //TODO

        //given: a subsurface, a parent surface
        //when: the subsurface position is applied
        //then: the subsurface position is applied based on the parent surface space.
    }

    @Test
    public void testApplyPositionInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: the subsurface position is applied
        //then: nothing happens
    }

    @Test
    public void testBeforeCommitSync() throws Exception {
        //TODO

        //given: a subsurface in sync mode
        //when: the before commit hook is called
        //then: the pending surface state is restored
    }

    @Test
    public void testBeforeCommitDesync() throws Exception {
        //TODO

        //given: a subsurface in desync mode
        //when: the before commit hook is called
        //then: nothing happens
    }

    @Test
    public void testBeforeCommitInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: the before commit hook is called
        //then: nothing happens
    }

    @Test
    public void testCommitSync() throws Exception {
        //TODO

        //given: a subsurface in sync mode
        //when: the commit hook is called
        //then: the surface new state is cached and the surface is reset to the pre-commit state.
    }

    @Test
    public void testCommitDesync() throws Exception {
        //TODO

        //given: a subsurface in desync mode
        //when: the commit hook is called
        //then: the cached state is updated to the current surface state
    }

    @Test
    public void testCommitInert() throws Exception {
        //TODO

        //given: an inert subsurface
        //when: the commit hook is called
        //then: nothing happens
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