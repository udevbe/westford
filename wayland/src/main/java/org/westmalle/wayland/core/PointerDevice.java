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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceRequests;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.westmalle.wayland.core.events.Button;
import org.westmalle.wayland.core.events.Motion;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "PointerDeviceFactory")
public class PointerDevice implements Role {
    @Nonnull
    private final EventBus                            inputBus       = new EventBus();
    @Nonnull
    private final Set<Integer>                        pressedButtons = new HashSet<>();
    @Nonnull
    private final Multimap<WlPointerResource, Cursor> cursors        = HashMultimap.create();
    @Nonnull
    private final InfiniteRegion infiniteRegion;
    private final NullRegion     nullRegion;
    @Nonnull
    private final CursorFactory  cursorFactory;
    @Nonnull
    private final Compositor     compositor;
    @Nonnull
    private final Display        display;
    @Nonnull
    private Point position = Point.ZERO;
    @Nonnull
    private Optional<WlSurfaceResource> grab  = Optional.empty();
    @Nonnull
    private Optional<WlSurfaceResource> focus = Optional.empty();
    private int pointerSerial;
    @Nonnegative
    private int buttonsPressed;

    PointerDevice(@Provided @Nonnull final Display display,
                  @Provided @Nonnull final InfiniteRegion infiniteRegion,
                  @Provided @Nonnull final NullRegion nullRegion,
                  @Provided @Nonnull final CursorFactory cursorFactory,
                  @Nonnull final Compositor compositor) {
        this.display = display;
        this.infiniteRegion = infiniteRegion;
        this.nullRegion = nullRegion;
        this.cursorFactory = cursorFactory;
        this.compositor = compositor;
    }

    public void motion(final Set<WlPointerResource> pointerResources,
                       final int time,
                       final int x,
                       final int y) {
        doMotion(pointerResources,
                 time,
                 x,
                 y);
        this.inputBus.post(Motion.create(time,
                                         getPosition()));
    }

    public void doMotion(@Nonnull final Set<WlPointerResource> pointerResources,
                         final int time,
                         final int x,
                         final int y) {
        this.position = Point.create(x,
                                     y);
        final Optional<WlSurfaceResource> newFocus = over();
        final Optional<WlSurfaceResource> oldFocus = this.focus;
        this.focus = newFocus;

        if (getGrab().isPresent()) {
            if (!oldFocus.equals(newFocus)) {
                if (oldFocus.equals(getGrab())) {
                    reportLeave(pointerResources,
                                getGrab().get());
                }
                else if (newFocus.equals(getGrab())) {
                    reportEnter(pointerResources,
                                getGrab().get());
                }
            }
            reportMotion(pointerResources,
                         time,
                         getGrab().get());
        }
        else {
            if (!oldFocus.equals(newFocus)) {
                if (oldFocus.isPresent()) {
                    reportLeave(pointerResources,
                                oldFocus.get());
                }
                if (newFocus.isPresent()) {
                    reportEnter(pointerResources,
                                newFocus.get());
                }
            }
            if (newFocus.isPresent()) {
                reportMotion(pointerResources,
                             time,
                             newFocus.get());
            }
        }
    }

    @Nonnull
    public Point getPosition() {
        return this.position;
    }

    @Nonnull
    public Optional<WlSurfaceResource> over() {
        final Iterator<WlSurfaceResource> surfaceIterator = this.compositor.getSurfacesStack()
                                                                           .descendingIterator();

        while (surfaceIterator.hasNext()) {
            final WlSurfaceResource surfaceResource = surfaceIterator.next();
            final WlSurfaceRequests implementation = surfaceResource.getImplementation();
            final Surface surface = ((WlSurface) implementation).getSurface();

            final Optional<Region> inputRegion = surface.getState()
                                                        .getInputRegion();
            final Region region = inputRegion.orElseGet(() -> this.infiniteRegion);
            if (region.contains(surface.getSize(),
                                surface.local(getPosition()))) {
                return Optional.of(surfaceResource);
            }
        }

        return Optional.empty();
    }

    @Nonnull
    public Optional<WlSurfaceResource> getGrab() {
        return this.grab;
    }

    private void reportLeave(final Set<WlPointerResource> wlPointer,
                             final WlSurfaceResource wlSurfaceResource) {
        final Optional<WlPointerResource> pointerResource = findPointerResource(wlPointer,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            pointerResource.get()
                           .leave(nextPointerSerial(),
                                  wlSurfaceResource);
        }
    }

    private void reportEnter(final Set<WlPointerResource> wlPointer,
                             final WlSurfaceResource wlSurfaceResource) {
        final Optional<WlPointerResource> pointerResource = findPointerResource(wlPointer,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Point relativePoint = wlSurface.getSurface()
                                                 .local(getPosition());
            pointerResource.get()
                           .enter(nextPointerSerial(),
                                  wlSurfaceResource,
                                  Fixed.create(relativePoint.getX()),
                                  Fixed.create(relativePoint.getY()));
        }
    }

    private void reportMotion(final Set<WlPointerResource> pointerResources,
                              final int time,
                              final WlSurfaceResource wlSurfaceResource) {
        final Optional<WlPointerResource> pointerResource = findPointerResource(pointerResources,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Point relativePoint = wlSurface.getSurface()
                                                 .local(getPosition());
            pointerResource.get()
                           .motion(time,
                                   Fixed.create(relativePoint.getX()),
                                   Fixed.create(relativePoint.getY()));
        }
    }

    private Optional<WlPointerResource> findPointerResource(final Set<WlPointerResource> pointerResources,
                                                            final WlSurfaceResource wlSurfaceResource) {
        for (final WlPointerResource wlPointerResource : pointerResources) {
            if (wlSurfaceResource.getClient()
                                 .equals(wlPointerResource.getClient())) {
                return Optional.of(wlPointerResource);
            }
        }
        return Optional.empty();
    }

    private int nextPointerSerial() {
        this.pointerSerial = this.display.nextSerial();
        return this.pointerSerial;
    }

    public void button(@Nonnull final Set<WlPointerResource> pointerResources,
                       final int time,
                       @Nonnegative final int button,
                       @Nonnull final WlPointerButtonState buttonState) {
        if (buttonState == WlPointerButtonState.PRESSED) {
            this.pressedButtons.add(button);
        }
        else {
            this.pressedButtons.remove(button);
        }
        doButton(pointerResources,
                 time,
                 button,
                 buttonState);
        this.inputBus.post(Button.create(time,
                                         button,
                                         buttonState));
    }

    private void doButton(@Nonnull final Set<WlPointerResource> pointerResources,
                          final int time,
                          @Nonnegative final int button,
                          @Nonnull final WlPointerButtonState buttonState) {
        if (buttonState == WlPointerButtonState.PRESSED) {
            this.buttonsPressed++;
        }
        else if (this.buttonsPressed > 0) {
            //make sure we only decrement if we had a least one increment.
            //Is such a thing even possible? Yes it is. (Aliens pressing a button before starting compositor)
            this.buttonsPressed--;
        }
        if (getGrab().isPresent()) {
            //always report all buttons if we have a grabbed surface.
            reportButton(pointerResources,
                         time,
                         button,
                         buttonState);
        }
        if (this.buttonsPressed == 0) {
            this.grab = Optional.empty();
        }
        else if (!getGrab().isPresent() && this.focus.isPresent()) {
            //no grab, but we do have a focus and a pressed button. Focused surface becomes grab.
            this.grab = this.focus;
            reportButton(pointerResources,
                         time,
                         button,
                         buttonState);
        }
    }

    private void reportButton(@Nonnull final Set<WlPointerResource> pointerResources,
                              final int time,
                              @Nonnegative final int button,
                              @Nonnull final WlPointerButtonState buttonState) {
        final WlSurfaceResource wlSurfaceResource = getGrab().get();
        final Optional<WlPointerResource> pointerResource = findPointerResource(pointerResources,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            pointerResource.get()
                           .button(nextPointerSerial(),
                                   time,
                                   button,
                                   buttonState.getValue());
        }
    }

    public boolean isButtonPressed(@Nonnegative final int button) {
        return this.pressedButtons.contains(button);
    }

    /**
     * Listen for motion as long as given surface is grabbed.
     *
     * @param surfaceResource   Surface that is grabbed.
     * @param serial            Serial that triggered the grab.
     * @param pointerGrabMotion Motion listener.
     */
    public void grabMotion(@Nonnull final WlSurfaceResource surfaceResource,
                           final int serial,
                           @Nonnull final PointerGrabMotion pointerGrabMotion) {
        if (!getGrab().isPresent() ||
            !getGrab().get()
                      .equals(surfaceResource) ||
            getPointerSerial() != serial) {
            //preconditions not met
            return;
        }

        final Listener motionListener = new Listener() {
            @Subscribe
            public void handle(final Motion motion) {
                if (getGrab().isPresent() &&
                    getGrab().get()
                             .equals(surfaceResource)) {
                    //there is pointer motion
                    pointerGrabMotion.motion(motion);
                }
                else {
                    //another surface has the grab, stop listening for pointer motion.
                    PointerDevice.this.unregister(this);
                    //stop listening for destroy event
                    remove();
                }
            }

            @Override
            public void handle() {
                //the surface was destroyed, stop listening for destroy event
                remove();
                //stop listening for pointer motion.
                PointerDevice.this.unregister(this);
            }
        };
        //listen for pointer motion
        register(motionListener);
        //listen for surface destruction
        surfaceResource.addDestroyListener(motionListener);
    }

    private int getPointerSerial() {
        return this.pointerSerial;
    }

    public void unregister(@Nonnull final Object listener) {
        this.inputBus.unregister(listener);
    }

    public void register(@Nonnull final Object listener) {
        this.inputBus.register(listener);
    }

    public void removeCursor(final WlPointerResource wlPointerResource,
                             final int serial) {
        final Collection<Cursor> pointerCursors = this.cursors.get(wlPointerResource);
        pointerCursors.removeIf(cursor -> cursor.getWlPointerResource()
                                                .equals(wlPointerResource));
    }

    public void setCursor(final WlPointerResource wlPointerResource,
                          final int serial,
                          final WlSurfaceResource wlSurfaceResource,
                          final int hotspotX,
                          final int hotspotY) {

        final Collection<Cursor> pointerCursors = this.cursors.get(wlPointerResource);

        Cursor cursor = null;
        for (final Cursor pointerCursor : pointerCursors) {
            if (pointerCursor.getWlSurfaceResource()
                             .equals(wlSurfaceResource)) {
                cursor = pointerCursor;
                break;
            }
        }
        if (cursor == null) {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Surface surface = wlSurface.getSurface();
            surface.setState(surface.getState()
                                    .toBuilder()
                                    .inputRegion(Optional.of(this.nullRegion))
                                    .build());

            cursor = this.cursorFactory.create(wlPointerResource,
                                               wlSurfaceResource);
            cursor.updateCursorPosition(getPosition());
            pointerCursors.add(cursor);

            //if the role object is destroyed, the surface should be 'reset' and become invisible until
            //a new role object of the same type is assigned to it.
            wlPointerResource.addDestroyListener(new Listener() {
                @Override
                public void handle() {
                    remove();
                    //TODO hide surface
                    surface.setPosition(Point.ZERO);
                    PointerDevice.this.cursors.removeAll(wlPointerResource);
                }
            });
        }

        cursor.setHotspot(Point.create(hotspotX,
                                       hotspotY));
    }

    @Override
    public void beforeCommit(final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.setPendingState(surface.getPendingState()
                                       .toBuilder()
                                       .inputRegion(Optional.of(this.nullRegion))
                                       .build());

        //TODO if the surface does not match the current cursor, hide the surface
    }
}