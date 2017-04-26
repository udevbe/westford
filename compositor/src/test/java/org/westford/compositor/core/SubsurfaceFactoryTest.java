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

import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.protocol.WlCompositor;
import org.westford.compositor.protocol.WlSurface;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubsurfaceFactoryTest {

    @Mock
    private PrivateSubsurfaceFactory privateSubsurfaceFactory;
    @Mock
    private Scene                    scene;
    @InjectMocks
    private SubsurfaceFactory        subsurfaceFactory;

    @Test
    public void testCreate() throws Exception {
        //given: subsurface factory, parent surface, surface
        final WlSurfaceResource                        parentWlSurfaceResource       = mock(WlSurfaceResource.class);
        final WlSurface                                parentWlSurface               = mock(WlSurface.class);
        final Surface                                  parentSurface                 = mock(Surface.class);
        final Signal<SurfaceState, Slot<SurfaceState>> parentApplySurfaceStateSignal = mock(Signal.class);
        final Subsurface                               parentRole                    = mock(Subsurface.class);
        final Signal<Boolean, Slot<Boolean>>           parentEffectiveSyncSignal     = mock(Signal.class);

        when(parentWlSurfaceResource.getImplementation()).thenReturn(parentWlSurface);
        when(parentWlSurface.getSurface()).thenReturn(parentSurface);
        when(parentSurface.getApplySurfaceStateSignal()).thenReturn(parentApplySurfaceStateSignal);
        when(parentSurface.getRole()).thenReturn(Optional.of(parentRole));
        doAnswer(invocation -> {
            invocation.getArgumentAt(0,
                                     RoleVisitor.class)
                      .visit(parentRole);
            return null;
        }).when(parentRole)
          .accept(any());
        when(parentRole.getEffectiveSyncSignal()).thenReturn(parentEffectiveSyncSignal);
        when(parentRole.isEffectiveSync()).thenReturn(true);

        final WlSurfaceResource                        wlSurfaceResource       = mock(WlSurfaceResource.class);
        final WlSurface                                wlSurface               = mock(WlSurface.class);
        final Surface                                  surface                 = mock(Surface.class);
        final SurfaceState                             oldSurfaceState         = mock(SurfaceState.class);
        final SurfaceState                             newSurfaceState         = mock(SurfaceState.class);
        final Signal<SurfaceState, Slot<SurfaceState>> applySurfaceStateSignal = mock(Signal.class);
        final WlCompositorResource                     wlCompositorResource    = mock(WlCompositorResource.class);
        final WlCompositor                             wlCompositor            = mock(WlCompositor.class);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getState()).thenReturn(oldSurfaceState);
        when(surface.getApplySurfaceStateSignal()).thenReturn(applySurfaceStateSignal);
        when(wlCompositorResource.getImplementation()).thenReturn(wlCompositor);

        final Subsurface subsurface = mock(Subsurface.class);

        when(this.privateSubsurfaceFactory.create(parentWlSurfaceResource,
                                                  Sibling.Companion.create(wlSurfaceResource),
                                                  oldSurfaceState,
                                                  oldSurfaceState)).thenReturn(subsurface);

        //when: create is called
        this.subsurfaceFactory.create(parentWlSurfaceResource,
                                      wlSurfaceResource);

        //and when: the surface commit is called
        final ArgumentCaptor<Slot> applySurfaceStateSlotArgumentCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(applySurfaceStateSignal).connect(applySurfaceStateSlotArgumentCaptor.capture());
        final Slot<SurfaceState> applySurfaceStateSlot = applySurfaceStateSlotArgumentCaptor.getValue();
        applySurfaceStateSlot.handle(newSurfaceState);

        //then: the subsurface is notified of the commit
        verify(subsurface).apply(newSurfaceState);

        //and when: the parent is a subsurface's mode changes
        final ArgumentCaptor<Slot> updateEffectiveSyncSlotArgumentCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(parentEffectiveSyncSignal).connect(updateEffectiveSyncSlotArgumentCaptor.capture());
        final Slot<Boolean> effectiveSyncSlot = updateEffectiveSyncSlotArgumentCaptor.getValue();
        effectiveSyncSlot.handle(false);

        //then: the subsurface is notified of this sync mode change
        verify(subsurface).updateEffectiveSync(false);

        //and when: the parent commit is called
        final ArgumentCaptor<Slot> parentApplySurfaceStateSlotArgumentCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(parentApplySurfaceStateSignal).connect(parentApplySurfaceStateSlotArgumentCaptor.capture());
        final Slot<SurfaceState> parentApplySurfaceStateSlot = parentApplySurfaceStateSlotArgumentCaptor.getValue();
        parentApplySurfaceStateSlot.handle(mock(SurfaceState.class));

        //then: the subsurface is notified
        verify(subsurface).onParentApply();

        //and when: the parent is destroyed
        final ArgumentCaptor<DestroyListener> parentDestroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(parentWlSurfaceResource).register(parentDestroyListenerArgumentCaptor.capture());
        final DestroyListener parentDestroyListener = parentDestroyListenerArgumentCaptor.getValue();
        parentDestroyListener.handle();

        //then: the subsurface must become inert
        verify(subsurface).setInert();
    }
}