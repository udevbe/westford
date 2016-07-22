package org.westmalle.wayland.html5;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5SeatFactory")
public class Html5Seat {

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
                final WlPointerButtonState buttonState = WlPointerButtonState.PRESSED;

                final int t = message.indexOf('t');
                final int button = Integer.parseInt(message.substring(2,
                                                                      t));
                final int time = (int) Long.parseLong(message.substring(t + 1));

                final Set<WlPointerResource> wlPointerResources = this.wlSeat.getWlPointer()
                                                                             .getResources();
                this.wlSeat.getWlPointer()
                           .getPointerDevice()
                           .button(wlPointerResources,
                                   time,
                                   button,
                                   buttonState);
                break;
            }
            case POINTER_UP: {
                final WlPointerButtonState buttonState = WlPointerButtonState.RELEASED;

                final int t = message.indexOf('t');
                final int button = Integer.parseInt(message.substring(2,
                                                                      t));
                final int time = (int) Long.parseLong(message.substring(t + 1));

                final Set<WlPointerResource> wlPointerResources = this.wlSeat.getWlPointer()
                                                                             .getResources();
                this.wlSeat.getWlPointer()
                           .getPointerDevice()
                           .button(wlPointerResources,
                                   time,
                                   button,
                                   buttonState);
                break;
            }
            case POINTER_MOTION: {
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
                break;
            }
            case KEY_DOWN: {

            }
            case KEY_UP: {

            }
            default: {
                //TODO log
                System.err.println("Unknown client message: " + message);
            }
        }
    }

}
