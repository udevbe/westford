package org.westmalle.wayland.input;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateLibinputSeatFactory")
public class LibinputSeat {

    @Nonnull
    private final WlSeat wlSeat;

    LibinputSeat(@Nonnull final WlSeat wlSeat) {
        this.wlSeat = wlSeat;
    }

    public void open(final String seatId) {
        //TODO open libipnut seat & listen for events
        //TODO set/update seat capabilities
    }
}
