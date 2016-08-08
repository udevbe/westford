/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlPointerAxis;
import org.freedesktop.wayland.shared.WlPointerAxisSource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.westmalle.wayland.core.events.Button;
import org.westmalle.wayland.core.events.PointerFocus;
import org.westmalle.wayland.core.events.PointerGrab;
import org.westmalle.wayland.core.events.PointerMotion;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PointerDevice implements Role {

    @Nonnull
    private final Signal<PointerMotion, Slot<PointerMotion>> motionSignal       = new Signal<>();
    @Nonnull
    private final Signal<Button, Slot<Button>>               buttonSignal       = new Signal<>();
    @Nonnull
    private final Signal<PointerGrab, Slot<PointerGrab>>     pointerGrabSignal  = new Signal<>();
    @Nonnull
    private final Signal<PointerFocus, Slot<PointerFocus>>   pointerFocusSignal = new Signal<>();

    @Nonnull
    private final Set<Integer>                   pressedButtons = new HashSet<>();
    @Nonnull
    private final Map<WlPointerResource, Cursor> cursors        = new HashMap<>();
    @Nonnull
    private final CursorFactory cursorFactory;
    @Nonnull
    private final NullRegion    nullRegion;
    @Nonnull
    private final JobExecutor   jobExecutor;
    @Nonnull
    private final Scene         scene;
    @Nonnull
    private final Display       display;

    @Nonnull
    private Point            position     = Point.ZERO;
    @Nonnull
    private Optional<Cursor> activeCursor = Optional.empty();

    @Nonnull
    private Optional<DestroyListener>   grabDestroyListener = Optional.empty();
    @Nonnull
    private Optional<WlSurfaceResource> grab                = Optional.empty();

    @Nonnull
    private Optional<WlSurfaceResource> focus                = Optional.empty();
    @Nonnull
    private Optional<DestroyListener>   focusDestroyListener = Optional.empty();

    private int buttonPressSerial;
    private int buttonReleaseSerial;
    private int enterSerial;
    private int leaveSerial;

    @Nonnegative
    private int buttonsPressed;

    @Inject
    PointerDevice(@Nonnull final Display display,
                  @Nonnull final NullRegion nullRegion,
                  @Nonnull final CursorFactory cursorFactory,
                  @Nonnull final JobExecutor jobExecutor,
                  @Nonnull final Scene scene) {
        this.display = display;
        this.nullRegion = nullRegion;
        this.cursorFactory = cursorFactory;
        this.jobExecutor = jobExecutor;
        this.scene = scene;
    }

    //TODO unit test
    public void axisSource(@Nonnull final Set<WlPointerResource> wlPointerResources,
                           final WlPointerAxisSource wlPointerAxisSource) {
        getFocus().ifPresent(wlSurfaceResource -> filter(wlPointerResources,
                                                         wlSurfaceResource.getClient()).forEach(wlPointerResource -> {
            if (wlPointerResource.getVersion() > 4) {
                wlPointerResource.axisSource(wlPointerAxisSource.value);
            }
        }));
        //TODO emit event?
    }

    //TODO unit test
    public void frame(@Nonnull final Set<WlPointerResource> wlPointerResources) {
        getFocus().ifPresent(wlSurfaceResource -> filter(wlPointerResources,
                                                         wlSurfaceResource.getClient()).forEach((wlPointerResource) -> {
            if (wlPointerResource.getVersion() > 4) {
                wlPointerResource.frame();
            }
        }));
        //TODO emit event?
    }

    //TODO unit test
    public void axisStop(@Nonnull final Set<WlPointerResource> wlPointerResources,
                         final WlPointerAxis wlPointerAxis,
                         final int time) {
        getFocus().ifPresent(wlSurfaceResource -> reportAxisStop(wlPointerResources,
                                                                 wlSurfaceResource,
                                                                 wlPointerAxis,
                                                                 time));
        //TODO emit event?
    }

    private void reportAxisStop(final Set<WlPointerResource> surfaceResource,
                                final WlSurfaceResource wlSurfaceResource,
                                final WlPointerAxis wlPointerAxis,
                                final int time) {
        filter(surfaceResource,
               wlSurfaceResource.getClient()).forEach(wlPointerResource -> {
            if (wlPointerResource.getVersion() > 4) {
                wlPointerResource.axisStop(time,
                                           wlPointerAxis.value);
            }
        });
    }

    //TODO unit test
    public void axisDiscrete(@Nonnull final Set<WlPointerResource> wlPointerResources,
                             final WlPointerAxis wlPointerAxis,
                             final int time,
                             final int discrete,
                             final float value) {
        getFocus().ifPresent(wlSurfaceResource -> filter(wlPointerResources,
                                                         wlSurfaceResource.getClient()).forEach(wlPointerResource -> {

            if (wlPointerResource.getVersion() > 4) {
                wlPointerResource.axisDiscrete(wlPointerAxis.value,
                                               discrete);
            }

            axisOrStop(wlPointerResource,
                       time,
                       wlPointerAxis,
                       value);
        }));
        //TODO emit event?
    }

    //TODO unit test
    public void axisContinuous(@Nonnull final Set<WlPointerResource> wlPointerResources,
                               final int time,
                               final WlPointerAxis wlPointerAxis,
                               final float value) {
        getFocus().ifPresent(wlSurfaceResource -> filter(wlPointerResources,
                                                         wlSurfaceResource.getClient()).forEach(wlPointerResource -> axisOrStop(wlPointerResource,
                                                                                                                                time,
                                                                                                                                wlPointerAxis,
                                                                                                                                value)));
        //TODO emit event?
    }

    private void axisOrStop(final WlPointerResource wlPointerResource,
                            final int time,
                            final WlPointerAxis wlPointerAxis,
                            final float value) {
        if (value != 0) {
            wlPointerResource.axis(time,
                                   wlPointerAxis.value,
                                   Fixed.create(value));
        }
        else if (wlPointerResource.getVersion() > 4) {
            wlPointerResource.axisStop(time,
                                       wlPointerAxis.value);
        }
    }

    /**
     * Move this pointer to a new absolute position and deliver a motion event to the client of the focused surface.
     *
     * @param wlPointerResources a set of pointer resources that will be used to find the client.
     * @param x                  new absolute X
     * @param y                  new absolute Y
     */
    public void motion(@Nonnull final Set<WlPointerResource> wlPointerResources,
                       final int time,
                       final int x,
                       final int y) {
        this.position = Point.create(x,
                                     y);

        if (!getGrab().isPresent()) {
            calculateFocus(wlPointerResources);
        }

        getFocus().ifPresent(wlSurfaceResource ->
                                     reportMotion(wlPointerResources,
                                                  time,
                                                  wlSurfaceResource));
        this.motionSignal.emit(PointerMotion.create(time,
                                                    getPosition()));
    }

    public void calculateFocus(@Nonnull final Set<WlPointerResource> wlPointerResources) {
        final Optional<WlSurfaceResource> oldFocus = getFocus();
        final Optional<WlSurfaceResource> newFocus = this.scene.pickSurface(getPosition());

        if (!oldFocus.equals(newFocus)) {
            updateFocus(wlPointerResources,
                        oldFocus,
                        newFocus);
        }
    }

    private void reportMotion(final Set<WlPointerResource> wlPointerResources,
                              final int time,
                              final WlSurfaceResource wlSurfaceResource) {

        final Point pointerPosition = getPosition();

        filter(wlPointerResources,
               wlSurfaceResource.getClient()).forEach(wlPointerResource -> {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Point relativePoint = wlSurface.getSurface()
                                                 .local(pointerPosition);
            wlPointerResource.motion(time,
                                     Fixed.create(relativePoint.getX()),
                                     Fixed.create(relativePoint.getY()));
        });

        this.activeCursor.ifPresent(cursor -> cursor.updatePosition(pointerPosition));
    }

    private void reportLeave(final Set<WlPointerResource> wlPointerResources,
                             final WlSurfaceResource wlSurfaceResource) {
        wlPointerResources.forEach(wlPointerResource -> wlPointerResource.leave(nextLeaveSerial(),
                                                                                wlSurfaceResource));
    }

    private void reportEnter(final Set<WlPointerResource> wlPointerResources,
                             final WlSurfaceResource wlSurfaceResource) {
        wlPointerResources.forEach(wlPointerResource -> {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Point relativePoint = wlSurface.getSurface()
                                                 .local(getPosition());
            wlPointerResource.enter(nextEnterSerial(),
                                    wlSurfaceResource,
                                    Fixed.create(relativePoint.getX()),
                                    Fixed.create(relativePoint.getY()));
        });
    }

    public int nextLeaveSerial() {
        this.leaveSerial = this.display.nextSerial();
        return getLeaveSerial();
    }

    public int nextEnterSerial() {
        this.enterSerial = this.display.nextSerial();
        return getEnterSerial();
    }

    public int getLeaveSerial() {
        return this.leaveSerial;
    }

    public void button(@Nonnull final Set<WlPointerResource> wlPointerResources,
                       final int time,
                       @Nonnegative final int button,
                       @Nonnull final WlPointerButtonState wlPointerButtonState) {
        if (wlPointerButtonState == WlPointerButtonState.PRESSED) {
            this.pressedButtons.add(button);
        }
        else {
            this.pressedButtons.remove(button);
        }
        doButton(wlPointerResources,
                 time,
                 button,
                 wlPointerButtonState);
        this.buttonSignal.emit(Button.create(time,
                                             button,
                                             wlPointerButtonState));
    }

    private void doButton(final Set<WlPointerResource> wlPointerResources,
                          final int time,
                          final int button,
                          final WlPointerButtonState wlPointerButtonState) {

        if (wlPointerButtonState == WlPointerButtonState.PRESSED) {
            this.buttonsPressed++;
        }
        else if (this.buttonsPressed > 0) {
            //make sure we only decrement if we had a least one increment.
            //Is such a thing even possible? Yes it is. (Aliens pressing a button before starting compositor)
            this.buttonsPressed--;
        }

        final boolean hasPress = this.buttonsPressed != 0;
        final boolean hasFocus = this.focus.isPresent();
        final boolean hasGrab  = getGrab().isPresent();

        if (hasPress &&
            !hasGrab &&
            hasFocus) {
            //no grab, but we do have a focus and a pressed button. Focused surface becomes grab.
            grab();
        }

        getGrab().ifPresent(wlSurfaceResource -> {
            reportButton(wlPointerResources,
                         wlSurfaceResource,
                         time,
                         button,
                         wlPointerButtonState);

            if (!hasPress) {
                ungrab();
            }
        });
    }

    @Nonnull
    public Optional<WlSurfaceResource> getGrab() {
        return this.grab;
    }

    private void grab() {
        this.grab = getFocus();
        this.grabDestroyListener = Optional.of(this::ungrab);

        final WlSurfaceResource wlSurfaceResource = getGrab().get();
        //if the surface having the grab is destroyed, we clear the grab
        wlSurfaceResource.register(this.grabDestroyListener.get());

        this.pointerGrabSignal.emit(PointerGrab.create(getGrab()));
    }

    private void reportButton(final Set<WlPointerResource> wlPointerResources,
                              final WlSurfaceResource wlSurfaceResource,
                              final int time,
                              final int button,
                              final WlPointerButtonState wlPointerButtonState) {
        filter(wlPointerResources,
               wlSurfaceResource.getClient()).forEach(wlPointerResource ->
                                                              wlPointerResource.button(wlPointerButtonState == WlPointerButtonState.PRESSED ?
                                                                                       nextButtonPressSerial() : nextButtonReleaseSerial(),
                                                                                       time,
                                                                                       button,
                                                                                       wlPointerButtonState.value));
    }

    private void ungrab() {
        //grab will be updated, don't listen for previous grab surface destruction.
        getGrab().ifPresent(wlSurfaceResource -> wlSurfaceResource.unregister(this.grabDestroyListener.get()));
        this.grabDestroyListener = Optional.empty();
        this.grab = Optional.empty();
        this.pointerGrabSignal.emit(PointerGrab.create(getGrab()));
    }

    @Nonnull
    public Optional<WlSurfaceResource> getFocus() {
        return this.focus;
    }

    private Set<WlPointerResource> filter(final Set<WlPointerResource> wlPointerResources,
                                          final Client client) {
        //filter out pointer resources that do not belong to the given client.
        return wlPointerResources.stream()
                                 .filter(wlPointerResource -> wlPointerResource.getClient()
                                                                               .equals(client))
                                 .collect(Collectors.toSet());
    }

    public int nextButtonPressSerial() {
        this.buttonPressSerial = this.display.nextSerial();
        return getButtonPressSerial();
    }

    public int nextButtonReleaseSerial() {
        this.buttonReleaseSerial = this.display.nextSerial();
        return getButtonReleaseSerial();
    }

    public int getButtonPressSerial() {
        return this.buttonPressSerial;
    }

    public int getButtonReleaseSerial() {
        return this.buttonReleaseSerial;
    }

    private void updateFocus(final Set<WlPointerResource> wlPointerResources,
                             final Optional<WlSurfaceResource> oldFocus,
                             final Optional<WlSurfaceResource> newFocus) {

        oldFocus.ifPresent(oldFocusResource -> {
            //remove old focus destroy listener
            oldFocusResource.unregister(this.focusDestroyListener.get());
            //notify client of focus lost
            reportLeave(filter(wlPointerResources,
                               oldFocusResource.getClient()),
                        oldFocusResource);
        });
        //clear ref to old destroy listener
        this.focusDestroyListener = Optional.empty();

        newFocus.ifPresent(newFocusResource -> {
            //if focus resource is destroyed, trigger schedule focus update. This guarantees that
            //the compositor removes and updates the list of active surfaces first.
            final DestroyListener destroyListener = () -> this.jobExecutor.submit(() -> calculateFocus(wlPointerResources));
            this.focusDestroyListener = Optional.of(destroyListener);
            //add destroy listener
            newFocusResource.register(destroyListener);
            //notify client of new focus
            reportEnter(filter(wlPointerResources,
                               newFocusResource.getClient()),
                        newFocusResource);
        });

        //update focus to new focus
        this.focus = newFocus;
        //notify listeners focus has changed
        this.pointerFocusSignal.emit(PointerFocus.create());
    }

    public boolean isButtonPressed(@Nonnegative final int button) {
        return this.pressedButtons.contains(button);
    }

    /**
     * Listen for motion as soon as given surface is grabbed.
     * <p/>
     * If another surface already has the grab, the listener
     * is never registered.
     *
     * @param wlSurfaceResource Surface that is grabbed.
     * @param buttonPressSerial Serial that triggered the grab.
     * @param pointerGrabMotion PointerMotion listener.
     *
     * @return true if the listener was registered, false if not.
     */
    public boolean grabMotion(@Nonnull final WlSurfaceResource wlSurfaceResource,
                              final int buttonPressSerial,
                              @Nonnull final PointerGrabMotion pointerGrabMotion) {
        if (!getGrab().isPresent() ||
            !getGrab().get()
                      .equals(wlSurfaceResource) ||
            getButtonPressSerial() != buttonPressSerial) {
            //preconditions not met
            return false;
        }


        final Slot<PointerMotion> motionSlot = new Slot<PointerMotion>() {

            public void handle(@Nonnull final PointerMotion motion) {
                if (getGrab().isPresent() &&
                    getGrab().get()
                             .equals(wlSurfaceResource)) {
                    //there is pointer motion
                    pointerGrabMotion.motion(motion);
                }
                else {
                    //another surface has the grab, stop listening for pointer motion.
                    getMotionSignal().disconnect(this);
                }
            }
        };

        //listen for pointer motion
        getMotionSignal().connect(motionSlot);
        //listen for surface destruction
        wlSurfaceResource.register(() -> {
            //another surface has the grab, stop listening for pointer motion.
            getMotionSignal().disconnect(motionSlot);
        });

        return true;
    }

    @Nonnull
    public Signal<PointerMotion, Slot<PointerMotion>> getMotionSignal() {
        return this.motionSignal;
    }

    @Nonnull
    public Signal<Button, Slot<Button>> getButtonSignal() {
        return this.buttonSignal;
    }

    @Nonnull
    public Signal<PointerFocus, Slot<PointerFocus>> getPointerFocusSignal() {
        return this.pointerFocusSignal;
    }

    @Nonnull
    public Signal<PointerGrab, Slot<PointerGrab>> getPointerGrabSignal() {
        return this.pointerGrabSignal;
    }

    public void removeCursor(@Nonnull final WlPointerResource wlPointerResource,
                             final int serial) {
        if (serial != getEnterSerial()) {
            return;
        }
        Optional.ofNullable(this.cursors.remove(wlPointerResource))
                .ifPresent(Cursor::hide);
    }

    public int getEnterSerial() {
        return this.enterSerial;
    }

    public void setCursor(@Nonnull final WlPointerResource wlPointerResource,
                          final int serial,
                          @Nonnull final WlSurfaceResource wlSurfaceResource,
                          final int hotspotX,
                          final int hotspotY) {

        if (serial != getEnterSerial()) {
            return;
        }

        Cursor cursor = this.cursors.get(wlPointerResource);
        final Point hotspot = Point.create(hotspotX,
                                           hotspotY);

        if (cursor == null) {
            cursor = this.cursorFactory.create(wlSurfaceResource,
                                               hotspot);
            this.cursors.put(wlPointerResource,
                             cursor);
            wlPointerResource.register(() -> Optional.ofNullable(this.cursors.remove(wlPointerResource))
                                                     .ifPresent(Cursor::hide));
        }
        else {
            cursor.setWlSurfaceResource(wlSurfaceResource);
            cursor.setHotspot(hotspot);
        }

        cursor.show();
        updateActiveCursor(wlPointerResource);

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        surface.setState(updateCursorSurfaceState(wlSurfaceResource,
                                                  surface.getState()
                                                         .toBuilder()).build());
    }

    private void updateActiveCursor(final WlPointerResource wlPointerResource) {

        final Cursor           cursor    = this.cursors.get(wlPointerResource);
        final Optional<Cursor> oldCursor = this.activeCursor;
        this.activeCursor = Optional.ofNullable(cursor);

        this.activeCursor.ifPresent(clientCursor -> clientCursor.updatePosition(getPosition()));

        if (!oldCursor.equals(this.activeCursor)) {
            oldCursor.ifPresent(Cursor::hide);
        }
    }

    private SurfaceState.Builder updateCursorSurfaceState(final WlSurfaceResource wlSurfaceResource,
                                                          final SurfaceState.Builder surfaceStateBuilder) {

        surfaceStateBuilder.inputRegion(Optional.of(this.nullRegion));
        surfaceStateBuilder.buffer(Optional.empty());

        this.activeCursor.ifPresent(clientCursor -> {
            if (clientCursor.getWlSurfaceResource()
                            .equals(wlSurfaceResource) &&
                !clientCursor.isHidden()) {
                //set back the buffer we cleared.
                surfaceStateBuilder.buffer(surfaceStateBuilder.build()
                                                              .getBuffer());
                //move visible cursor to top of surface stack
                this.scene.getSurfacesStack()
                          .remove(wlSurfaceResource);
                this.scene.getSurfacesStack()
                          .addLast(wlSurfaceResource);
            }
        });

        return surfaceStateBuilder;
    }

    @Nonnull
    public Point getPosition() {
        return this.position;
    }

    @Override
    public void beforeCommit(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        updateCursorSurfaceState(wlSurfaceResource,
                                 surface.getPendingState());
    }

    @Override
    public void afterDestroy(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        this.cursors.values()
                    .removeIf(cursor -> {
                        if (cursor.getWlSurfaceResource()
                                  .equals(wlSurfaceResource)) {
                            cursor.hide();
                            return true;
                        }
                        else {
                            return false;
                        }
                    });
    }
}