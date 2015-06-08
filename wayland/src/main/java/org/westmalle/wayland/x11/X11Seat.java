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
import org.westmalle.wayland.nativ.*;
import org.westmalle.wayland.output.JobExecutor;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_ENTER_WINDOW;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_LEAVE_WINDOW;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_POINTER_MOTION;

public class X11Seat {

    @Nonnull
    private final Libxcb libxcb;
    @Nonnull
    private final X11Output x11Output;
    @Nonnull
    private final WlSeat      wlSeat;
    @Nonnull
    private final JobExecutor jobExecutor;

    X11Seat(@Nonnull final Libxcb libxcb,
            @Nonnull final X11Output x11Output,
            @Nonnull final WlSeat wlSeat,
            @Nonnull final JobExecutor jobExecutor) {
        this.libxcb = libxcb;
        this.x11Output = x11Output;
        this.wlSeat = wlSeat;
        this.jobExecutor = jobExecutor;
    }

    @Subscribe
    public void handle(final xcb_key_press_event_t event) {

    }

    @Subscribe
    public void handle(final xcb_button_press_event_t event) {

        final long  time   = event.time;
        final short button = event.detail;

        this.libxcb.xcb_grab_pointer(this.x11Output.getXcbConnection(),
                                     0,
                                     this.x11Output.getxWindow(),
                                     XCB_EVENT_MASK_BUTTON_PRESS |
                                     XCB_EVENT_MASK_BUTTON_RELEASE |
                                     XCB_EVENT_MASK_POINTER_MOTION |
                                     XCB_EVENT_MASK_ENTER_WINDOW |
                                     XCB_EVENT_MASK_LEAVE_WINDOW,
                                     Libxcb.XCB_GRAB_MODE_ASYNC,
                                     Libxcb.XCB_GRAB_MODE_ASYNC,
                                     this.x11Output.getxWindow(),
                                     Libxcb.XCB_CURSOR_NONE,
                                     time);

        switch (button){
            case 1:
            case 2:
            case 3:
                this.wlSeat.getOptionalWlPointer()
                        .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                .button(wlPointer.getResources(),
                                        (int) time,
                                        button,
                                        WlPointerButtonState.PRESSED)));
            default:
        }
    }

    @Subscribe
    public void handle(final xcb_key_release_event_t event) {

    }

    @Subscribe
    public void handle(final xcb_button_release_event_t event) {

        final long  time   = event.time;
        final short button = event.detail;

        this.libxcb.xcb_ungrab_pointer(this.x11Output.getXcbConnection(),
                                       time);

        switch (button) {
            case 1:
            case 2:
            case 3:
                this.wlSeat.getOptionalWlPointer()
                        .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                .button(wlPointer.getResources(),
                                        (int) time,
                                        button,
                                        WlPointerButtonState.RELEASED)));
            default:
        }
    }

    @Subscribe
    public void handle(final xcb_motion_notify_event_t event) {
        final long time = event.time;
        final int  x    = event.event_x;
        final int  y    = event.event_y;

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .motion(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          x,
                                                                                          y)));
    }
}
