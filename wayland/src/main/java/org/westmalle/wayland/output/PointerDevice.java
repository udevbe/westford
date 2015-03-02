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
import org.westmalle.wayland.output.events.Button;
import org.westmalle.wayland.output.events.Motion;
import org.westmalle.wayland.protocol.WlRegion;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "PointerDeviceFactory")
public class PointerDevice {
    private final EventBus     inputBus       = new EventBus();
    private final Set<Integer> pressedButtons = new HashSet<>();

    private Point position = Point.ZERO;

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

    public Point getPosition() {
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
        this.inputBus.post(Motion.create(time,
                                         getPosition()));
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
        this.inputBus.post(Button.create(time,
                                         button,
                                         buttonState));
    }

    public boolean isButtonPressed(final int button) {
        return this.pressedButtons.contains(button);
    }

    public void register(@Nonnull final Object listener) {
        this.inputBus.register(listener);
    }

    public void unregister(@Nonnull final Object listener) {
        this.inputBus.unregister(listener);
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

    @Nonnull
    public Optional<WlSurfaceResource> getGrab() {
        return this.grab;
    }

    private void reportButton(final Set<WlPointerResource> pointerResources,
                              final int time,
                              final int button,
                              final WlPointerButtonState buttonState) {
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

    @Nonnull
    public Optional<WlSurfaceResource> over() {
        final Iterator<WlSurfaceResource> surfaceIterator = this.compositor.getSurfacesStack()
                                                                           .descendingIterator();

        while (surfaceIterator.hasNext()) {
            final WlSurfaceResource surfaceResource = surfaceIterator.next();
            final WlSurfaceRequests implementation = surfaceResource.getImplementation();
            final Surface surface = ((WlSurface) implementation).getSurface();

            final Optional<WlRegionResource> inputRegion = surface.getState()
                                                                  .getInputRegion();
            final Region region;
            if (inputRegion.isPresent()) {
                final WlRegion wlRegion = (WlRegion) inputRegion.get()
                                                                .getImplementation();
                region = wlRegion.getRegion();
            }
            else {
                region = Region.INFINITY;
            }

            if (region.contains(surface.getSize(),
                                surface.local(getPosition()))) {
                return Optional.of(surfaceResource);
            }
        }

        return Optional.empty();
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