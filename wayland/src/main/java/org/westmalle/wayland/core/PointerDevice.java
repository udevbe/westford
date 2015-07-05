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
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlBufferResource;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "PointerDeviceFactory")
public class PointerDevice implements Role {
    @Nonnull
    private final EventBus            inputBus       = new EventBus();
    @Nonnull
    private final Set<Integer>        pressedButtons = new HashSet<>();
    @Nonnull
    private final Map<Client, Cursor> cursors        = new HashMap<>();
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
    private Point                       position = Point.ZERO;
    @Nonnull
    private Optional<WlSurfaceResource> grab     = Optional.empty();
    @Nonnull
    private Optional<WlSurfaceResource> focus    = Optional.empty();
    @Nonnull
    private Optional<Cursor>            cursor   = Optional.empty();
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
        updateCursor();

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

    private void updateCursor() {
        //hide the 'old' cursor, we'll make it visible again after this method has finished.
        this.cursor.ifPresent(Cursor::hide);

        if (this.focus.isPresent()) {
            final Cursor cursor = this.cursors.get(this.focus.get()
                                                             .getClient());
            this.cursor = Optional.ofNullable(cursor);
        }
        else {
            this.cursor = Optional.empty();
        }

        //we've choosen a (new) cursor, make that one visible.
        this.cursor.ifPresent(clientCursor -> {
            clientCursor.updatePosition(getPosition());
            clientCursor.show();
        });
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
        this.cursors.remove(wlPointerResource.getClient())
                    .hide();
    }

    public void setCursor(final WlPointerResource wlPointerResource,
                          final int serial,
                          final WlSurfaceResource wlSurfaceResource,
                          final int hotspotX,
                          final int hotspotY) {

        Cursor cursor = this.cursors.get(wlPointerResource.getClient());

        if (cursor == null) {
            cursor = create(wlSurfaceResource);
            wlPointerResource.addDestroyListener(new Listener() {
                @Override
                public void handle() {
                    remove();
                    PointerDevice.this.cursors.get(wlPointerResource.getClient())
                                              .hide();
                }
            });
            wlSurfaceResource.addDestroyListener(new Listener() {
                @Override
                public void handle() {
                    remove();
                    PointerDevice.this.cursors.remove(wlSurfaceResource.getClient());
                }
            });
            this.cursors.put(wlPointerResource.getClient(),
                             cursor);
        }

        cursor.update(wlSurfaceResource,
                      Point.create(hotspotX,
                                   hotspotY));
        cursor.show();
        //TODO request render?
    }

    private Cursor create(final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        surface.setState(surface.getState()
                                .toBuilder()
                                .inputRegion(Optional.of(this.nullRegion))
                                .build());

        final Cursor cursor = this.cursorFactory.create();
        cursor.updatePosition(getPosition());
        return cursor;
    }

    @Override
    public void beforeCommit(final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final SurfaceState         pendingState        = surface.getPendingState();
        final SurfaceState.Builder pendingStateBuilder = pendingState.toBuilder();

        pendingStateBuilder.inputRegion(Optional.of(this.nullRegion));
        pendingStateBuilder.buffer(Optional.<WlBufferResource>empty());

        this.cursor.ifPresent(clientCursor -> {
            if (clientCursor.getWlSurfaceResource()
                            .equals(wlSurfaceResource) && !clientCursor.isHidden()) {
                //set back the buffer we cleared.
                pendingStateBuilder.buffer(pendingState.getBuffer());
            }
        });

        surface.setPendingState(pendingStateBuilder.build());
    }
}