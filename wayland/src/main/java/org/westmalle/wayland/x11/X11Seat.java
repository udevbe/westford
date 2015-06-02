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
package org.westmalle.wayland.x11;

import com.google.common.eventbus.Subscribe;

import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westmalle.wayland.nativ.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.xcb_motion_notify_event_t;
import org.westmalle.wayland.output.JobExecutor;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

public class X11Seat {

    @Nonnull
    private final WlSeat wlSeat;
    @Nonnull
    private final JobExecutor jobExecutor;

    X11Seat(@Nonnull final WlSeat wlSeat,
            @Nonnull final JobExecutor jobExecutor) {
        this.wlSeat = wlSeat;
        this.jobExecutor = jobExecutor;
    }

    @Subscribe
    public void handle(final xcb_key_press_event_t event) {

    }

    @Subscribe
    public void handle(final xcb_button_press_event_t event) {
        final long time = event.time;
        final short button = event.detail;

        this.wlSeat.getOptionalWlPointer()
                .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                        .button(wlPointer.getResources(),
                                (int) time,
                                button,
                                WlPointerButtonState.PRESSED)));
    }

    @Subscribe
    public void handle(xcb_key_release_event_t event) {

    }

    @Subscribe
    public void handle(final xcb_button_release_event_t event) {
        final long time = event.time;
        final short button = event.detail;

        this.wlSeat.getOptionalWlPointer()
                .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                        .button(wlPointer.getResources(),
                                (int) time,
                                button,
                                WlPointerButtonState.RELEASED)));
    }

    @Subscribe
    public void handle(final xcb_motion_notify_event_t event) {
        final long time = event.time;
        final int x = event.event_x;
        final int y = event.event_y;

        this.wlSeat.getOptionalWlPointer()
                .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                        .motion(wlPointer.getResources(),
                                (int) time,
                                x,
                                y)));
    }
}
