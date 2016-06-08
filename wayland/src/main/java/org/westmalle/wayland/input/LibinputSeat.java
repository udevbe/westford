package org.westmalle.wayland.input;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlPointerAxis;
import org.freedesktop.wayland.shared.WlPointerAxisSource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.Point;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libinput.Libinput;
import org.westmalle.wayland.nativ.libinput.libinput_interface;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlTouch;

import javax.annotation.Nonnull;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_BUTTON_STATE_PRESSED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_BUTTON_STATE_RELEASED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_DEVICE_ADDED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_DEVICE_REMOVED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_KEYBOARD_KEY;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_NONE;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_AXIS;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_BUTTON;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_MOTION;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_DOWN;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_FRAME;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_MOTION;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_UP;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_KEY_STATE_PRESSED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_KEY_STATE_RELEASED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SOURCE_FINGER;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SOURCE_WHEEL;
import static org.westmalle.wayland.nativ.libinput.Pointerclose_restricted.nref;
import static org.westmalle.wayland.nativ.libinput.Pointeropen_restricted.nref;

@AutoFactory(allowSubclasses = true,
             className = "PrivateLibinputSeatFactory")
public class LibinputSeat {

    @Nonnull
    private final Display    display;
    @Nonnull
    private final Libudev    libudev;
    @Nonnull
    private final Libinput   libinput;
    @Nonnull
    private final Libc       libc;
    @Nonnull
    private final WlSeat     wlSeat;
    @Nonnull
    private final Compositor compositor;

    LibinputSeat(@Provided @Nonnull final Display display,
                 @Provided @Nonnull final Libudev libudev,
                 @Provided @Nonnull final Libinput libinput,
                 @Provided @Nonnull final Libc libc,
                 @Provided @Nonnull final Compositor compositor,
                 @Nonnull final WlSeat wlSeat) {
        this.display = display;
        this.libudev = libudev;
        this.libinput = libinput;
        this.libc = libc;
        this.wlSeat = wlSeat;
        this.compositor = compositor;
    }

    //TODO unit test all possible events that can occur after call to open
    public void open(final String seat) {
        final long libinput = createUdevContext(seat);
        loop(libinput);
        //TODO set/update seat capabilities?
    }

    private long createUdevContext(final String seatId) {
        final long udev = this.libudev.udev_new();
        if (udev == 0L) {
            throw new RuntimeException("Failed to initialize udev");
        }

        final Pointer<libinput_interface> interface_ = malloc(libinput_interface.SIZE,
                                                              libinput_interface.class);
        interface_.dref()
                  .open_restricted(nref(this::openRestricted));
        interface_.dref()
                  .close_restricted(nref(this::closeRestricted));

        final long libinput = this.libinput.libinput_udev_create_context(interface_.address,
                                                                         0,
                                                                         udev);

        if (this.libinput.libinput_udev_assign_seat(libinput,
                                                    Pointer.nref(seatId).address) != 0) {
            this.libinput.libinput_unref(libinput);
            this.libudev.udev_unref(udev);

            throw new RuntimeException(String.format("Failed to set seat=%s",
                                                     seatId));
        }

        return libinput;
    }

    private int openRestricted(@Ptr(String.class) final long path,
                               final int flags,
                               @Ptr(Void.class) final long user_data) {
        final int fd = this.libc.open(path,
                                      flags);

        return fd < 0 ? -this.libc.getErrno() : fd;
    }

    private void closeRestricted(final int fd,
                                 @Ptr(Void.class) final long user_data) {
        this.libc.close(fd);
    }

    private void loop(final long libinput) {
        final int libinputFd = this.libinput.libinput_get_fd(libinput);
        this.display.getEventLoop()
                    .addFileDescriptor(libinputFd,
                                       WaylandServerCore.WL_EVENT_READABLE,
                                       (fd, mask) -> {
                                           if (fd == libinputFd) {
                                               processEvents(libinput);
                                           }
                                           return 0;
                                       });
    }

    private void processEvents(final long libinput) {
        this.libinput.libinput_dispatch(libinput);

        long event;
        while ((event = this.libinput.libinput_get_event(libinput)) != 0) {

            processEvent(event);

            this.libinput.libinput_event_destroy(event);
            this.libinput.libinput_dispatch(libinput);
        }
    }

    private void processEvent(final long event) {
        switch (this.libinput.libinput_event_get_type(event)) {
            case LIBINPUT_EVENT_NONE:
                //no more events
                break;
            case LIBINPUT_EVENT_DEVICE_ADDED:
                //TODO add seat capability
                break;
            case LIBINPUT_EVENT_DEVICE_REMOVED:
                //TODO remove seat capability
                break;
            case LIBINPUT_EVENT_KEYBOARD_KEY:
                handleKeyboardKey(this.libinput.libinput_event_get_keyboard_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_MOTION:
                handlePointerMotion(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE:
                handlePointerMotionAbsolute(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_BUTTON:
                handlePointerButton(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_AXIS:
                handlePointerAxis(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_DOWN:
                handleTouchDown(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_MOTION:
                handleTouchMotion(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_UP:
                handleTouchUp(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_FRAME:
                handleTouchFrame(this.libinput.libinput_event_get_touch_event(event));
                break;
            default:
                //unsupported libinput event
                break;
        }
    }

    private void handleKeyboardKey(final long keyboardEvent) {

        final int time         = this.libinput.libinput_event_keyboard_get_time(keyboardEvent);
        final int key          = this.libinput.libinput_event_keyboard_get_key(keyboardEvent);
        final int keyState     = this.libinput.libinput_event_keyboard_get_key_state(keyboardEvent);
        final int seatKeyCount = this.libinput.libinput_event_keyboard_get_seat_key_count(keyboardEvent);

        if ((keyState == LIBINPUT_KEY_STATE_PRESSED &&
             seatKeyCount != 1) ||
            (keyState == LIBINPUT_KEY_STATE_RELEASED &&
             seatKeyCount != 0)) {
            //don't send key events when we have an additional press or release of the same key on the same seat from a different device.
            return;
        }

        final WlKeyboard wlKeyboard = this.wlSeat.getWlKeyboard();
        wlKeyboard.getKeyboardDevice()
                  .key(wlKeyboard.getResources(),
                       time,
                       key,
                       wlKeyboardKeyState(keyState));
    }

    private WlKeyboardKeyState wlKeyboardKeyState(final int keyState) {
        final WlKeyboardKeyState wlKeyboardKeyState;
        if (keyState == LIBINPUT_KEY_STATE_PRESSED) {
            wlKeyboardKeyState = WlKeyboardKeyState.PRESSED;
        }
        else {
            wlKeyboardKeyState = WlKeyboardKeyState.RELEASED;
        }
        return wlKeyboardKeyState;
    }

    private void handlePointerMotion(final long pointerEvent) {

        final int    time = this.libinput.libinput_event_pointer_get_time(pointerEvent);
        final double dx   = this.libinput.libinput_event_pointer_get_dx(pointerEvent);
        final double dy   = this.libinput.libinput_event_pointer_get_dy(pointerEvent);

        final WlPointer     wlPointer             = this.wlSeat.getWlPointer();
        final PointerDevice pointerDevice         = wlPointer.getPointerDevice();
        final Point         pointerDevicePosition = pointerDevice.getPosition();

        pointerDevice.motion(wlPointer.getResources(),
                             time,
                             pointerDevicePosition.getX() + (int) dx,
                             pointerDevicePosition.getY() + (int) dy);
        pointerDevice.frame(wlPointer.getResources());
    }

    private void handlePointerMotionAbsolute(final long pointerEvent) {
        final WlOutput wlOutput = this.compositor.getWlOutputs()
                                                 .getFirst();
        if (wlOutput != null) {
            //FIXME we should to take into account that output pixel size is not always the same as compositor coordinates but for now it is.

            final OutputGeometry geometry = wlOutput.getOutput()
                                                    .getGeometry();
            final int physicalWidth  = geometry.getPhysicalWidth();
            final int physicalHeight = geometry.getPhysicalHeight();

            final int time = this.libinput.libinput_event_pointer_get_time(pointerEvent);
            final double x = this.libinput.libinput_event_pointer_get_absolute_x_transformed(pointerEvent,
                                                                                             physicalWidth);
            final double y = this.libinput.libinput_event_pointer_get_absolute_y_transformed(pointerEvent,
                                                                                             physicalHeight);

            final WlPointer     wlPointer     = this.wlSeat.getWlPointer();
            final PointerDevice pointerDevice = wlPointer.getPointerDevice();

            pointerDevice.motion(wlPointer.getResources(),
                                 time,
                                 (int) x,
                                 (int) y);
            pointerDevice.frame(wlPointer.getResources());
        }//else ignore event
    }

    private void handlePointerButton(final long pointerEvent) {

        final int time            = this.libinput.libinput_event_pointer_get_time(pointerEvent);
        final int buttonState     = this.libinput.libinput_event_pointer_get_button_state(pointerEvent);
        final int seatButtonCount = this.libinput.libinput_event_pointer_get_seat_button_count(pointerEvent);
        final int button          = this.libinput.libinput_event_pointer_get_button(pointerEvent);

        if ((buttonState == LIBINPUT_BUTTON_STATE_PRESSED &&
             seatButtonCount != 1) ||
            (buttonState == LIBINPUT_BUTTON_STATE_RELEASED &&
             seatButtonCount != 0)) {
            //don't send button events when we have an additional press or release of the same key on the same seat from a different device.
            return;
        }

        final WlPointer     wlPointer     = this.wlSeat.getWlPointer();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        pointerDevice.button(wlPointer.getResources(),
                             time,
                             button,
                             wlPointerButtonState(buttonState));
        pointerDevice.frame(wlPointer.getResources());
    }

    private WlPointerButtonState wlPointerButtonState(final int buttonState) {
        if (buttonState == LIBINPUT_BUTTON_STATE_PRESSED) {
            return WlPointerButtonState.PRESSED;
        }
        else {
            return WlPointerButtonState.RELEASED;
        }
    }

    private void handlePointerAxis(final long pointerEvent) {

        final int hasVertical = this.libinput.libinput_event_pointer_has_axis(pointerEvent,
                                                                              LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL);
        final int hasHorizontal = this.libinput.libinput_event_pointer_has_axis(pointerEvent,
                                                                                LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL);

        if (hasVertical == 0 && hasHorizontal == 0) { return; }

        final int                 source = this.libinput.libinput_event_pointer_get_axis_source(pointerEvent);
        final WlPointerAxisSource wlPointerAxisSource;

        switch (source) {
            case LIBINPUT_POINTER_AXIS_SOURCE_WHEEL:
                wlPointerAxisSource = WlPointerAxisSource.WHEEL;
                break;
            case LIBINPUT_POINTER_AXIS_SOURCE_FINGER:
                wlPointerAxisSource = WlPointerAxisSource.FINGER;
                break;
            case LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS:
                wlPointerAxisSource = WlPointerAxisSource.CONTINUOUS;
                break;
            default:
                //unknown scroll source
                return;
        }

        final WlPointer     wlPointer     = this.wlSeat.getWlPointer();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        pointerDevice.axisSource(wlPointer.getResources(),
                                 wlPointerAxisSource);

        if (hasVertical != 0) {
            final int vertDiscrete = getAxisDiscrete(pointerEvent,
                                                     LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL);
            final double vert = normalizeScroll(pointerEvent,
                                                LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL);

            final int time = this.libinput.libinput_event_pointer_get_time(pointerEvent);

            if (vertDiscrete == 0) {
                pointerDevice.axisContinuous(wlPointer.getResources(),
                                             time,
                                             WlPointerAxis.VERTICAL_SCROLL,
                                             (float) vert);
            }
            else {
                pointerDevice.axisDiscrete(wlPointer.getResources(),
                                           WlPointerAxis.VERTICAL_SCROLL,
                                           time,
                                           vertDiscrete,
                                           (float) vert);
            }
        }

        if (hasHorizontal != 0) {
            final int horizDiscrete = getAxisDiscrete(pointerEvent,
                                                      LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL);
            final double horiz = normalizeScroll(pointerEvent,
                                                 LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL);

            final int time = this.libinput.libinput_event_pointer_get_time(pointerEvent);

            if (horizDiscrete == 0) {
                pointerDevice.axisContinuous(wlPointer.getResources(),
                                             time,
                                             WlPointerAxis.HORIZONTAL_SCROLL,
                                             (float) horiz);
            }
            else {
                pointerDevice.axisDiscrete(wlPointer.getResources(),
                                           WlPointerAxis.HORIZONTAL_SCROLL,
                                           time,
                                           horizDiscrete,
                                           (float) horiz);
            }
        }

        pointerDevice.frame(wlPointer.getResources());
    }

    private int getAxisDiscrete(final long pointerEvent,
                                final int axis) {
        final int source = this.libinput.libinput_event_pointer_get_axis_source(pointerEvent);

        if (source != LIBINPUT_POINTER_AXIS_SOURCE_WHEEL) { return 0; }

        return (int) this.libinput.libinput_event_pointer_get_axis_value_discrete(pointerEvent,
                                                                                  axis);
    }

    private double normalizeScroll(final long pointerEvent,
                                   final int axis) {
        double value = 0.0;

        final int source = this.libinput.libinput_event_pointer_get_axis_source(pointerEvent);
    /* libinput < 0.8 sent wheel click events with value 10. Since 0.8
       the value is the angle of the click in degrees. To keep
	   backwards-compat with existing clients, we just send multiples of
	   the click count.
	 */
        switch (source) {
            case LIBINPUT_POINTER_AXIS_SOURCE_WHEEL:
                value = 10 * this.libinput.libinput_event_pointer_get_axis_value_discrete(pointerEvent,
                                                                                          axis);
                break;
            case LIBINPUT_POINTER_AXIS_SOURCE_FINGER:
            case LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS:
                value = this.libinput.libinput_event_pointer_get_axis_value(pointerEvent,
                                                                            axis);
                break;
        }

        return value;
    }

    private void handleTouchDown(final long touchEvent) {

        final WlOutput wlOutput = this.compositor.getWlOutputs()
                                                 .getFirst();
        if (wlOutput != null) {
            //FIXME we should to take into account that output pixel size != compositor coordinates

            final OutputGeometry outputGeometry = wlOutput.getOutput()
                                                          .getGeometry();
            final int physicalWidth  = outputGeometry.getPhysicalWidth();
            final int physicalHeight = outputGeometry.getPhysicalHeight();

            final int time = this.libinput.libinput_event_touch_get_time(touchEvent);
            final int slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent);
            final int x = (int) this.libinput.libinput_event_touch_get_x_transformed(touchEvent,
                                                                                     physicalWidth);
            final int y = (int) this.libinput.libinput_event_touch_get_y_transformed(touchEvent,
                                                                                     physicalHeight);

            final WlTouch wlTouch = this.wlSeat.getWlTouch();
            wlTouch.getTouchDevice()
                   .down(wlTouch.getResources(),
                         slot,
                         time,
                         x,
                         y);
        }
    }

    private void handleTouchMotion(final long touchEvent) {
        final WlOutput wlOutput = this.compositor.getWlOutputs()
                                                 .getFirst();
        if (wlOutput != null) {
            //FIXME we should to take into account that output pixel size is not always the same as compositor coordinates but for now it is.

            final OutputGeometry outputGeometry = wlOutput.getOutput()
                                                          .getGeometry();
            final int physicalWidth  = outputGeometry.getPhysicalWidth();
            final int physicalHeight = outputGeometry.getPhysicalHeight();

            final int time = this.libinput.libinput_event_touch_get_time(touchEvent);
            final int slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent);
            final int x = (int) this.libinput.libinput_event_touch_get_x_transformed(touchEvent,
                                                                                     physicalWidth);
            final int y = (int) this.libinput.libinput_event_touch_get_y_transformed(touchEvent,
                                                                                     physicalHeight);

            final WlTouch wlTouch = this.wlSeat.getWlTouch();
            wlTouch.getTouchDevice()
                   .motion(wlTouch.getResources(),
                           slot,
                           time,
                           x,
                           y);
        }
    }

    private void handleTouchUp(final long touchEvent) {
        final int time = this.libinput.libinput_event_touch_get_time(touchEvent);
        final int slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent);

        final WlTouch wlTouch = this.wlSeat.getWlTouch();
        wlTouch.getTouchDevice()
               .up(wlTouch.getResources(),
                   slot,
                   time);
    }

    private void handleTouchFrame(final long touchEvent) {
        final WlTouch wlTouch = this.wlSeat.getWlTouch();
        wlTouch.getTouchDevice()
               .frame(wlTouch.getResources());
    }
}
