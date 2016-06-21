package org.westmalle.wayland.input;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlPointerAxis;
import org.freedesktop.wayland.shared.WlPointerAxisSource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.Point;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.nativ.libinput.Libinput;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlTouch;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Optional;

import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_BUTTON_STATE_PRESSED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_BUTTON_STATE_RELEASED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_KEY_STATE_PRESSED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_KEY_STATE_RELEASED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SOURCE_FINGER;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_POINTER_AXIS_SOURCE_WHEEL;

@AutoFactory(className = "LibinputDeviceFactory",
             allowSubclasses = true)
public class LibinputDevice {
    @Nonnull
    private final Libinput                  libinput;
    private final long                      device;
    @Nonnull
    private final Platform                  platform;
    @Nonnull
    private final WlSeat                    wlSeat;
    @Nonnull
    private final EnumSet<WlSeatCapability> deviceCapabilities;

    public LibinputDevice(@Provided @Nonnull final Libinput libinput,
                          @Provided @Nonnull final Platform platform,
                          @Nonnull final WlSeat wlSeat,
                          final long device,
                          @Nonnull final EnumSet<WlSeatCapability> deviceCapabilities) {
        this.libinput = libinput;
        this.platform = platform;
        this.wlSeat = wlSeat;
        this.device = device;
        this.deviceCapabilities = deviceCapabilities;
    }

    @Nonnull
    public EnumSet<WlSeatCapability> getDeviceCapabilities() {
        return this.deviceCapabilities;
    }

    public Optional<WlOutput> findBoundOutput() {
        //TODO we can cache the output that is mapped to this device and listen for output detsruction/addition so we save a few nanoseconds

        final long outputNamePointer = this.libinput.libinput_device_get_output_name(this.device);
        if (outputNamePointer == 0L) {
            return Optional.empty();
        }

        final String deviceOutputName = Pointer.wrap(String.class,
                                                     outputNamePointer)
                                               .dref();
//        for (final WlOutput wlOutput : this.platform.getWlOutput()) {
        //TODO give outputs a name
//            if (deviceOutputName.equals(platform.getOutput()
//                                                .getName())) {
        return this.platform.getWlOutput();
//            }
        //     }

        //      return Optional.empty();
    }

    public void handleKeyboardKey(final long keyboardEvent) {

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

    public void handlePointerMotion(final long pointerEvent) {

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

    public void handlePointerMotionAbsolute(final long pointerEvent) {
        findBoundOutput().ifPresent(wlOutput -> {
            //FIXME we should to take into account that boundOutput pixel size is not always the same as compositor coordinates but for now it is.

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
        });
    }

    public void handlePointerButton(final long pointerEvent) {

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

    public void handlePointerAxis(final long pointerEvent) {

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

    public void handleTouchDown(final long touchEvent) {
        findBoundOutput().ifPresent(wlOutput -> {
            //FIXME we should to take into account that boundOutput pixel size != compositor coordinates

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
        });
    }

    public void handleTouchMotion(final long touchEvent) {
        findBoundOutput().ifPresent(wlOutput -> {
            //FIXME we should to take into account that boundOutput pixel size is not always the same as compositor coordinates but for now it is.

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
        });
    }

    public void handleTouchUp(final long touchEvent) {
        final int time = this.libinput.libinput_event_touch_get_time(touchEvent);
        final int slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent);

        final WlTouch wlTouch = this.wlSeat.getWlTouch();
        wlTouch.getTouchDevice()
               .up(wlTouch.getResources(),
                   slot,
                   time);
    }

    public void handleTouchFrame(final long touchEvent) {
        final WlTouch wlTouch = this.wlSeat.getWlTouch();
        wlTouch.getTouchDevice()
               .frame(wlTouch.getResources());
    }
}
