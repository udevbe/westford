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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_client_message_data_t;
import org.westmalle.wayland.nativ.libxcb.xcb_enter_notify_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_expose_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_focus_in_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_focus_out_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_keymap_notify_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_leave_notify_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_CLIENT_MESSAGE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_ENTER_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EXPOSE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_FOCUS_IN;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_FOCUS_OUT;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_KEYMAP_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_KEY_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_KEY_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_LEAVE_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_MOTION_NOTIFY;

@AutoFactory(className = "X11EventBusFactory")
public class X11EventBus implements EventLoop.FileDescriptorEventHandler {

    private final Signal<Structure, Slot<Structure>> xEventSignal = new Signal<>();
    @Nonnull
    private final Libxcb  libxcb;
    @Nonnull
    private final Libc    libc;
    @Nonnull
    private final Pointer xcbConnection;

    X11EventBus(@Provided @Nonnull final Libxcb libxcb,
                @Provided @Nonnull final Libc libc,
                @Nonnull final Pointer xcbConnection) {
        this.libxcb = libxcb;
        this.libc = libc;
        this.xcbConnection = xcbConnection;
    }

    public Signal<Structure, Slot<Structure>> getxEventSignal() {
        return this.xEventSignal;
    }

    @Override
    public int handle(final int fd,
                      final int mask) {
        xcb_generic_event_t event;
        while ((event = this.libxcb.xcb_poll_for_event(this.xcbConnection)) != null) {
            post(event);
        }
        return 0;
    }

    private void post(final xcb_generic_event_t event) {
        final int                 responseType = (event.response_type & ~0x80);
        final Optional<Structure> optionalEvent;
        switch (responseType) {
            case XCB_MOTION_NOTIFY: {
                optionalEvent = Optional.of(new xcb_motion_notify_event_t(event.getPointer()));
                break;
            }
            case XCB_BUTTON_PRESS: {
                optionalEvent = Optional.of(new xcb_button_press_event_t(event.getPointer()));
                break;
            }
            case XCB_BUTTON_RELEASE: {
                optionalEvent = Optional.of(new xcb_button_release_event_t(event.getPointer()));
                break;
            }
            case XCB_KEY_PRESS: {
                optionalEvent = Optional.of(new xcb_key_press_event_t(event.getPointer()));
                break;
            }
            case XCB_KEY_RELEASE: {
                optionalEvent = Optional.of(new xcb_key_release_event_t(event.getPointer()));
                break;
            }
            case XCB_EXPOSE: {
                optionalEvent = Optional.of(new xcb_expose_event_t(event.getPointer()));
                break;
            }
            case XCB_ENTER_NOTIFY: {
                optionalEvent = Optional.of(new xcb_enter_notify_event_t(event.getPointer()));
                break;
            }
            case XCB_LEAVE_NOTIFY: {
                optionalEvent = Optional.of(new xcb_leave_notify_event_t(event.getPointer()));
                break;
            }
            case XCB_CLIENT_MESSAGE: {
                optionalEvent = Optional.of(new xcb_client_message_data_t(event.getPointer()));
                break;
            }
            case XCB_FOCUS_IN: {
                optionalEvent = Optional.of(new xcb_focus_in_event_t(event.getPointer()));
                break;
            }
            case XCB_FOCUS_OUT: {
                optionalEvent = Optional.of(new xcb_focus_out_event_t(event.getPointer()));
                break;
            }
            case XCB_KEYMAP_NOTIFY: {
                optionalEvent = Optional.of(new xcb_keymap_notify_event_t(event.getPointer()));
                break;
            }
            default: {
                optionalEvent = Optional.empty();
            }
        }
        if (optionalEvent.isPresent()) {
            final Structure specificEvent = optionalEvent.get();
            specificEvent.read();
            getxEventSignal().emit(specificEvent);
        }
        this.libc.free(event.getPointer());
    }
}
