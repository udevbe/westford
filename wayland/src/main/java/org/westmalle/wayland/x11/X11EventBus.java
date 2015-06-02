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
import com.google.common.eventbus.EventBus;

import com.sun.jna.Pointer;

import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.nativ.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.xcb_generic_event_t;
import org.westmalle.wayland.nativ.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.xcb_motion_notify_event_t;

import javax.annotation.Nonnull;

@AutoFactory
public class X11EventBus implements EventLoop.FileDescriptorEventHandler {

    private final EventBus eventBus = new EventBus();
    @Nonnull
    private final Libxcb libxcb;
    @Nonnull
    private final Pointer xcbConnection;

    X11EventBus(@Provided @Nonnull final Libxcb libxcb,
                @Nonnull final Pointer xcbConnection) {
        this.libxcb = libxcb;
        this.xcbConnection = xcbConnection;
    }

    private void post(xcb_generic_event_t event) {
        final int responseType = (event.response_type & ~0x80);
        switch (responseType) {
            case Libxcb.XCB_KEY_PRESS: {
                this.eventBus.post(new xcb_key_press_event_t(event.getPointer()));
                break;
            }
            case Libxcb.XCB_KEY_RELEASE: {
                this.eventBus.post(new xcb_key_release_event_t(event.getPointer()));
                break;
            }
            case Libxcb.XCB_BUTTON_PRESS: {
                this.eventBus.post(new xcb_button_press_event_t(event.getPointer()));
                break;
            }
            case Libxcb.XCB_BUTTON_RELEASE: {
                this.eventBus.post(new xcb_button_release_event_t(event.getPointer()));
                break;
            }
            case Libxcb.XCB_MOTION_NOTIFY: {
                this.eventBus.post(new xcb_motion_notify_event_t(event.getPointer()));
                break;
            }
            case Libxcb.XCB_ENTER_NOTIFY: {
                break;
            }
            case Libxcb.XCB_LEAVE_NOTIFY: {
                break;
            }
        }
    }

    public void register(Object listener) {
        eventBus.register(listener);
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
}
