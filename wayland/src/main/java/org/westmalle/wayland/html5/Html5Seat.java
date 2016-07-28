package org.westmalle.wayland.html5;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.logging.Logger;

import static org.westmalle.wayland.nativ.linux.InputEventCodes.BTN_LEFT;
import static org.westmalle.wayland.nativ.linux.InputEventCodes.BTN_MIDDLE;
import static org.westmalle.wayland.nativ.linux.InputEventCodes.BTN_RIGHT;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5SeatFactory")
public class Html5Seat {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String POINTER_DOWN   = "pd";
    private static final String POINTER_UP     = "pu";
    private static final String POINTER_MOTION = "pm";
    private static final String KEY_DOWN       = "kd";
    private static final String KEY_UP         = "ku";

    @Nonnull
    private final WlSeat wlSeat;

    Html5Seat(@Nonnull final WlSeat wlSeat) {
        this.wlSeat = wlSeat;
    }

    public void handle(final String message) {
        switch (message.substring(0,
                                  2)) {
            case POINTER_DOWN: {
                handlePointerButton(message,
                                    WlPointerButtonState.PRESSED);
                break;
            }
            case POINTER_UP: {
                handlePointerButton(message,
                                    WlPointerButtonState.RELEASED);
                break;
            }
            case POINTER_MOTION: {
                handlePointerMotion(message);
                break;
            }
            case KEY_DOWN: {
                handkeKeyboardKey(message,
                                  WlKeyboardKeyState.PRESSED);
                break;
            }
            case KEY_UP: {
                handkeKeyboardKey(message,
                                  WlKeyboardKeyState.RELEASED);
                break;
            }
            default: {
                LOGGER.warning("Ignoring unknown client message: " + message);
            }
        }
    }

    private void handkeKeyboardKey(final String message,
                                   final WlKeyboardKeyState wlKeyboardKeyState) {
        final int t = message.indexOf('t');
        final int key = Integer.parseInt(message.substring(2,
                                                           t));
        final int time = (int) Long.parseLong(message.substring(t + 1));

        final Set<WlKeyboardResource> wlKeyboardResources = this.wlSeat.getWlKeyboard()
                                                                       .getResources();

        final int eventCode = Html5ToLinuxKeycode.toLinuxInputEvent(key);
        if (eventCode == 0) {
            LOGGER.warning("Not processing key input. Got unknown html5 key code: " + key);
            return;
        }

        this.wlSeat.getWlKeyboard()
                   .getKeyboardDevice()
                   .key(wlKeyboardResources,
                        time,
                        eventCode,
                        wlKeyboardKeyState);
    }

    private void handlePointerMotion(final String message) {
        final int x = message.indexOf('x');
        final int y = message.indexOf('y');
        final int t = message.indexOf('t');

        final int xCor = Integer.parseInt(message.substring(x + 1,
                                                            y));
        final int yCor = Integer.parseInt(message.substring(y + 1,
                                                            t));
        final int time = (int) Long.parseLong(message.substring(t + 1));

        final Set<WlPointerResource> wlPointerResources = this.wlSeat.getWlPointer()
                                                                     .getResources();
        this.wlSeat.getWlPointer()
                   .getPointerDevice()
                   .motion(wlPointerResources,
                           time,
                           xCor,
                           yCor);
    }

    private void handlePointerButton(final String message,
                                     final WlPointerButtonState buttonState) {
        final int t = message.indexOf('t');
        final int button = Integer.parseInt(message.substring(2,
                                                              t));
        final int time = (int) Long.parseLong(message.substring(t + 1));

        final Set<WlPointerResource> wlPointerResources = this.wlSeat.getWlPointer()
                                                                     .getResources();

        final int linuxButton = toLinuxButton(button);
        if (linuxButton == 0) {
            LOGGER.warning("Not processing pointer button input. Got unknown html5 button code: " + button);
            return;
        }

        this.wlSeat.getWlPointer()
                   .getPointerDevice()
                   .button(wlPointerResources,
                           time,
                           linuxButton,
                           buttonState);
    }

    private int toLinuxButton(final int jsButton) {
        final int button;
        switch (jsButton) {
            case 0:
                button = BTN_LEFT;
                break;
            case 1:
                button = BTN_MIDDLE;
                break;
            case 2:
                button = BTN_RIGHT;
                break;
            default:
                //TODO define more buttons
                button = 0;
        }
        return button;
    }

}
