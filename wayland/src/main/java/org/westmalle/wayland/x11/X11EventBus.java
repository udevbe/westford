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

import com.github.zubnix.jaccall.Pointer;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;

import javax.annotation.Nonnull;

@AutoFactory(className = "X11EventBusFactory",
             allowSubclasses = true)
public class X11EventBus implements EventLoop.FileDescriptorEventHandler {

    private final Signal<Pointer<xcb_generic_event_t>, Slot<Pointer<xcb_generic_event_t>>> xEventSignal = new Signal<>();
    @Nonnull
    private final Libxcb libxcb;
    private final long   xcbConnection;

    X11EventBus(@Provided @Nonnull final Libxcb libxcb,
                final long xcbConnection) {
        this.libxcb = libxcb;
        this.xcbConnection = xcbConnection;
    }

    @Override
    public int handle(final int fd,
                      final int mask) {
        long event;
        while ((event = this.libxcb.xcb_poll_for_event(this.xcbConnection)) != 0L) {
            try (final Pointer<xcb_generic_event_t> generic_event = Pointer.wrap(xcb_generic_event_t.class,
                                                                                 event)) {
                getXEventSignal().emit(generic_event);
            }
        }
        return 0;
    }

    public Signal<Pointer<xcb_generic_event_t>, Slot<Pointer<xcb_generic_event_t>>> getXEventSignal() {
        return this.xEventSignal;
    }
}
