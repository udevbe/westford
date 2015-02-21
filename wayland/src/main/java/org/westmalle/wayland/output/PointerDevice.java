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
package org.westmalle.wayland.output;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.westmalle.wayland.output.events.Motion;
import org.westmalle.wayland.protocol.WlRegion;
import org.westmalle.wayland.protocol.WlSurface;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "PointerDeviceFactory")
public class PointerDevice {
    private final EventBus     inputBus       = new EventBus();
    private final Set<Integer> pressedButtons = new HashSet<>();

    private PointImmutable position = new Point();

    private Optional<WlSurfaceResource> grab  = Optional.empty();
    private Optional<WlSurfaceResource> focus = Optional.empty();

    private int pointerSerial;
    private int buttonsPressed;

    private final Compositor compositor;
    private final Display    display;

    PointerDevice(@Provided final Display display,
                  final Compositor compositor) {
        this.display = display;
        this.compositor = compositor;
    }

    public PointImmutable getPosition() {
        return this.position;
    }

    public void motion(final Set<WlPointerResource> pointerResources,
                       final int time,
                       final int x,
                       final int y) {
        doMotion(pointerResources,
                 time,
                 x,
                 y);
        this.inputBus.post(new Motion(time,
                                      x,
                                      y));
    }

    public void button(final Set<WlPointerResource> pointerResources,
                       final int time,
                       final int button,
                       final WlPointerButtonState buttonState) {
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
        this.inputBus.post(button);
    }

    public boolean isButtonPressed(final int button) {
        return this.pressedButtons.contains(button);
    }

    public void register(final Object listener) {
        this.inputBus.register(listener);
    }

    public void unregister(final Object listener) {
        this.inputBus.unregister(listener);
    }

    public void move(final WlSurfaceResource surfaceResource,
                     final int serial) {
        final WlSurface wlSurface = (WlSurface) surfaceResource.getImplementation();
        //keep reference to surface position position, relative to the pointer position
        final PointImmutable pointerStartPosition = getPosition();
        final PointImmutable surfaceStartPosition = wlSurface.getSurface()
                                                             .getPosition();
        final PointImmutable pointerSurfaceDelta = new Point(pointerStartPosition.getX() - surfaceStartPosition.getX(),
                                                             pointerStartPosition.getY() - surfaceStartPosition.getY());

        final Listener motionListener = new Listener() {
            @Subscribe
            public void handle(final Motion motion) {
                if (PointerDevice.this.grab.get()
                                           .equals(surfaceResource)
                    && getPointerSerial() == serial) {
                    //there is pointer motion, move surface
                    move(surfaceResource,
                         pointerSurfaceDelta,
                         motion);
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

    private void move(final WlSurfaceResource surfaceResource,
                      final PointImmutable pointerSurfaceDelta,
                      final Motion motion) {
        final WlSurface wlSurface = (WlSurface) surfaceResource.getImplementation();
        final Surface surface = wlSurface.getSurface();
        surface.setPosition(new Point(motion.getX() - pointerSurfaceDelta.getX(),
                                      motion.getY() - pointerSurfaceDelta.getY()));
        this.compositor.requestRender(surfaceResource);
    }

    private void doButton(final Set<WlPointerResource> pointerResources,
                          final int time,
                          final int button,
                          final WlPointerButtonState buttonState) {
        if (buttonState == WlPointerButtonState.PRESSED) {
            this.buttonsPressed++;
        }
        else if (this.buttonsPressed > 0) {
            //make sure we only decrement if we had a least one increment.
            //Is such a thing even possible? Yes it is. (Aliens pressing a button before starting compositor)
            this.buttonsPressed--;
        }

        if (this.grab.isPresent()) {
            //always report all buttons if we have a grabbed surface.
            reportButton(pointerResources,
                         time,
                         button,
                         buttonState);
        }

        if (this.buttonsPressed == 0) {
            this.grab = Optional.empty();
        }
        else if (!this.grab.isPresent() && this.focus.isPresent()) {
            //no grab, but we do have a focus and a pressed button. Focused surface becomes grab.
            this.grab = this.focus;
            reportButton(pointerResources,
                         time,
                         button,
                         buttonState);
        }
    }

    private void reportButton(final Set<WlPointerResource> pointerResources,
                              final int time,
                              final int button,
                              final WlPointerButtonState buttonState) {
        final WlSurfaceResource wlSurfaceResource = this.grab.get();
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

    private Optional<WlSurfaceResource> findWlSurfaceResource() {
        final Iterator<WlSurfaceResource> surfaceIterator = this.compositor.getSurfacesStack()
                                                                           .descendingIterator();
        while (surfaceIterator.hasNext()) {
            final WlSurfaceResource surfaceResource = surfaceIterator.next();
            final WlSurfaceRequests implementation = surfaceResource.getImplementation();
            final Surface surface = ((WlSurface) implementation).getSurface();

            final Optional<WlRegionResource> inputRegion = surface.getInputRegion();
            if (inputRegion.isPresent()) {

                final PointImmutable position = surface.getPosition();
                final int surfaceX = position.getX();
                final int surfaceY = position.getY();

                final int positionX = getPosition().getX();
                final int positionY = getPosition().getY();

                WlRegion wlRegion = (WlRegion) inputRegion.get()
                                                          .getImplementation();
                Region region = wlRegion.getRegion();
                if (region.contains(new Point(positionX - surfaceX,
                                              positionY - surfaceY))) {

                    return Optional.of(surfaceResource);
                }
            }
        }

        return Optional.empty();
    }

    public void doMotion(final Set<WlPointerResource> pointerResources,
                         final int time,
                         final int x,
                         final int y) {
        this.position = new Point(x,
                                  y);
        final Optional<WlSurfaceResource> newFocus = findWlSurfaceResource();
        final Optional<WlSurfaceResource> oldFocus = this.focus;
        this.focus = newFocus;

        if (this.grab.isPresent()) {
            if (!oldFocus.equals(newFocus)) {
                if (oldFocus.equals(this.grab)) {
                    reportLeave(pointerResources,
                                this.grab.get());
                }
                else if (newFocus.equals(this.grab)) {
                    reportEnter(pointerResources,
                                this.grab.get());
                }
            }
            reportMotion(pointerResources,
                         time,
                         this.grab.get());
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

    private void reportMotion(final Set<WlPointerResource> pointerResources,
                              final int time,
                              final WlSurfaceResource wlSurfaceResource) {
        final Optional<WlPointerResource> pointerResource = findPointerResource(pointerResources,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final PointImmutable relativePoint =  wlSurface.getSurface().relativeCoordinate(getPosition());

            pointerResource.get()
                           .motion(time,
                                   Fixed.create(relativePoint.getX()),
                                   Fixed.create(relativePoint.getY()));
        }
    }

    private void reportEnter(final Set<WlPointerResource> wlPointer,
                             final WlSurfaceResource wlSurfaceResource) {
        final Optional<WlPointerResource> pointerResource = findPointerResource(wlPointer,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final PointImmutable relativePoint =  wlSurface.getSurface().relativeCoordinate(getPosition());

            pointerResource.get()
                           .enter(nextPointerSerial(),
                                  wlSurfaceResource,
                                  Fixed.create(relativePoint.getX()),
                                  Fixed.create(relativePoint.getY()));
        }
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

    private int getPointerSerial() {
        return this.pointerSerial;
    }
}