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

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.nativ.LibX11;
import org.westmalle.wayland.nativ.LibX11xcb;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.nativ.xcb_generic_error_t;
import org.westmalle.wayland.nativ.xcb_screen_t;
import org.westmalle.wayland.nativ.xcb_void_cookie_t;
import org.westmalle.wayland.output.Output;
import org.westmalle.wayland.output.OutputFactory;
import org.westmalle.wayland.output.OutputGeometry;
import org.westmalle.wayland.output.OutputMode;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static org.westmalle.wayland.nativ.LibX11xcb.XCBOwnsEventQueue;
import static org.westmalle.wayland.nativ.Libxcb.XCB_COPY_FROM_PARENT;
import static org.westmalle.wayland.nativ.Libxcb.XCB_CW_EVENT_MASK;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_ENTER_WINDOW;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_KEY_PRESS;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_KEY_RELEASE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_LEAVE_WINDOW;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EVENT_MASK_POINTER_MOTION;
import static org.westmalle.wayland.nativ.Libxcb.XCB_WINDOW_CLASS_INPUT_OUTPUT;

public class X11OutputFactory {

    @Nonnull
    private final Display display;
    @Nonnull
    private final LibX11 libX11;
    @Nonnull
    private final Libxcb libxcb;
    @Nonnull
    private final LibX11xcb libX11xcb;
    @Nonnull
    private final X11EglOutputFactory x11EglOutputFactory;
    @Nonnull
    private final WlOutputFactory wlOutputFactory;
    @Nonnull
    private final OutputFactory outputFactory;
    @Nonnull
    private final X11EventBusFactory x11EventBusFactory;

    @Inject
    X11OutputFactory(@Nonnull final Display display,
                     @Nonnull final LibX11 libX11,
                     @Nonnull final Libxcb libxcb,
                     @Nonnull final LibX11xcb libX11xcb,
                     @Nonnull final X11EglOutputFactory x11EglOutputFactory,
                     @Nonnull final WlOutputFactory wlOutputFactory,
                     @Nonnull final OutputFactory outputFactory,
                     @Nonnull final X11EventBusFactory x11EventBusFactory) {
        this.display = display;
        this.libX11 = libX11;
        this.libxcb = libxcb;
        this.libX11xcb = libX11xcb;
        this.x11EglOutputFactory = x11EglOutputFactory;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.x11EventBusFactory = x11EventBusFactory;
    }

    public WlOutput create(@Nonnull final String xDisplay,
                           @Nonnegative final int width,
                           @Nonnegative final int height) {
        checkArgument(width > 0);
        checkArgument(height > 0);

        return createXPlatformOutput(xDisplay,
                                     width,
                                     height);
    }


    private WlOutput createXPlatformOutput(final String xDisplayName,
                                           final int width,
                                           final int height) {

        final Pointer xDisplay = this.libX11.XOpenDisplay(xDisplayName);
        if (xDisplay == null) {
            throw new RuntimeException("XOpenDisplay() failed: " + xDisplayName);
        }

        final Pointer xcbConnection = this.libX11xcb.XGetXCBConnection(xDisplay);
        this.libX11xcb.XSetEventQueueOwner(xDisplay,
                                           XCBOwnsEventQueue);
        if (xcbConnection == null) {
            throw new RuntimeException("XGetXCBConnection() failed");
        }
        if (this.libxcb.xcb_connection_has_error(xcbConnection) != 0) {
            throw new RuntimeException("error occurred while connecting to X server");
        }

        final Pointer setup = this.libxcb.xcb_get_setup(xcbConnection);
        final xcb_screen_t screen = this.libxcb.xcb_setup_roots_iterator(setup).data;

        final int window = createXWindow(xcbConnection,
                                         screen,
                                         width,
                                         height);
        final X11Output x11Output = createX11Output(xDisplay,
                                                    xcbConnection,
                                                    window);
        final Output output = createOutput(x11Output,
                                           width,
                                           height,
                                           screen);
        return this.wlOutputFactory.create(output);
    }

    private int createXWindow(final Pointer xcbConnection,
                              final xcb_screen_t screen,
                              final int width,
                              final int height) {

        final int window = this.libxcb.xcb_generate_id(xcbConnection);
        if (window <= 0) {
            throw new RuntimeException("failed to generate X window id");
        }

        final int xcbWindowAttribMask = XCB_CW_EVENT_MASK;
        final Pointer xcbWindowAttribList = new Memory(Integer.BYTES * 7);
        xcbWindowAttribList.write(0,
                                  new int[]{
                                          XCB_EVENT_MASK_KEY_PRESS,
                                          XCB_EVENT_MASK_KEY_RELEASE,
                                          XCB_EVENT_MASK_BUTTON_PRESS,
                                          XCB_EVENT_MASK_BUTTON_RELEASE,
                                          XCB_EVENT_MASK_ENTER_WINDOW,
                                          XCB_EVENT_MASK_LEAVE_WINDOW,
                                          XCB_EVENT_MASK_POINTER_MOTION
                                  },
                                  0,
                                  7);
        final xcb_void_cookie_t createCookie = this.libxcb.xcb_create_window_checked(xcbConnection,
                                                                                     (byte) XCB_COPY_FROM_PARENT,
                                                                                     window,
                                                                                     screen.root,
                                                                                     (short) 0,
                                                                                     (short) 0,
                                                                                     (short) width,
                                                                                     (short) height,
                                                                                     (short) 0,
                                                                                     (short) XCB_WINDOW_CLASS_INPUT_OUTPUT,
                                                                                     screen.root_visual,
                                                                                     xcbWindowAttribMask,
                                                                                     xcbWindowAttribList);
        final xcb_void_cookie_t mapCookie = this.libxcb.xcb_map_window_checked(xcbConnection,
                                                                               window);
        xcb_generic_error_t error;
        error = this.libxcb.xcb_request_check(xcbConnection,
                                              createCookie);
        if (error != null) {
            throw new RuntimeException("failed to create X window: " + error.error_code);
        }
        error = this.libxcb.xcb_request_check(xcbConnection,
                                              mapCookie);
        if (error != null) {
            throw new RuntimeException("failed to map X window: " + error.error_code);
        }

        //FIXME instead we want to setup an event loop that is hooked into our own compositor event loop
        this.libxcb.xcb_flush(xcbConnection);
        return window;
    }

    private Output createOutput(final X11Output x11Output,
                                final int width,
                                final int height,
                                final xcb_screen_t screen) {
        final OutputGeometry outputGeometry = OutputGeometry.builder()
                .x(0)
                .y(0)
                .subpixel(0)
                .make("Westmalle xcb")
                .model("X11")
                .physicalWidth((width / screen.width_in_pixels)
                               * screen.width_in_millimeters)
                .physicalHeight((height / screen.height_in_pixels)
                                * screen.height_in_millimeters)
                .transform(WlOutputTransform.NORMAL.getValue())
                .build();
        final OutputMode outputMode = OutputMode.builder()
                .flags(0)
                .height(height)
                .width(width)
                .refresh(60)
                .build();
        return this.outputFactory.create(outputGeometry,
                                         outputMode,
                                         x11Output);
    }

    private X11Output createX11Output(final Pointer xDisplay,
                                      final Pointer connection,
                                      final int window) {
        final X11EventBus x11EventBus = this.x11EventBusFactory.create(connection);
        this.display.getEventLoop()
                .addFileDescriptor(this.libxcb.xcb_get_file_descriptor(connection),
                                   EventLoop.EVENT_READABLE,
                                   x11EventBus);
        return new X11Output(this.x11EglOutputFactory,
                             x11EventBus,
                             connection,
                             xDisplay,
                             window);
    }
}
