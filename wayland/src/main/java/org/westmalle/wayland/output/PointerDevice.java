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
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.westmalle.wayland.output.events.Motion;
import org.westmalle.wayland.protocol.WlSurface;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "PointerDeviceFactory")
public class PointerDevice {
    private final EventBus     inputBus       = new EventBus();
    private final Set<Integer> pressedButtons = new HashSet<>();

    private PointImmutable position = new Point();

    private Optional<WlSurfaceResource> grab       = Optional.empty();
    private Optional<Integer>           grabSerial = Optional.empty();
    private Optional<WlSurfaceResource> focus      = Optional.empty();

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
                     final int grabSerial) {
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
                if (getGrabSerial().isPresent()
                    && getGrabSerial().get()
                                      .equals(grabSerial)) {
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
                         this.grabSerial.get(),
                         time,
                         button,
                         buttonState);
        }

        if (this.buttonsPressed == 0) {
            this.grab = Optional.empty();
            this.grabSerial = Optional.empty();
        }
        else if (!this.grab.isPresent() && this.focus.isPresent()) {
            //no grab, but we do have a focus and a pressed button. Focused surface becomes grab.
            this.grab = this.focus;
            final int serial = this.display.nextSerial();
            this.grabSerial = Optional.of(serial);
            reportButton(pointerResources,
                         serial,
                         time,
                         button,
                         buttonState);
        }
    }

    private void reportButton(final Set<WlPointerResource> pointerResources,
                              final int serial,
                              final int time,
                              final int button,
                              final WlPointerButtonState buttonState) {
        final WlSurfaceResource wlSurfaceResource = this.grab.get();
        final Optional<WlPointerResource> pointerResource = findPointerResource(pointerResources,
                                                                                wlSurfaceResource);
        if (pointerResource.isPresent()) {
            pointerResource.get()
                           .button(serial,
                                   time,
                                   button,
                                   buttonState.getValue());
        }
    }

    public void doMotion(final Set<WlPointerResource> pointerResources,
                         final int time,
                         final int x,
                         final int y) {
        this.position = new Point(x,
                                  y);
        final Optional<WlSurfaceResource> newFocus = this.compositor.getScene()
                                                                    .findSurfaceAtCoordinate(this.position);
        final Optional<WlSurfaceResource> oldFocus = this.focus;
        this.focus = newFocus;

        if(this.grab.isPresent()) {
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
                if(oldFocus.isPresent()) {
                    reportLeave(pointerResources,
                                oldFocus.get());
                }
                if(newFocus.isPresent()) {
                    reportEnter(pointerResources,
                                newFocus.get());
                }
            }
            if(newFocus.isPresent()) {
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
            final PointImmutable relativePoint = this.compositor.getScene()
                                                                .relativeCoordinate(wlSurfaceResource,
                                                                                    this.position);
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
            final PointImmutable relativePoint = this.compositor.getScene()
                                                                .relativeCoordinate(wlSurfaceResource,
                                                                                    this.position);
            pointerResource.get()
                           .enter(this.display.nextSerial(),
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
                           .leave(this.display.nextSerial(),
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

    public Optional<Integer> getGrabSerial() {
        return this.grabSerial;
    }

}
