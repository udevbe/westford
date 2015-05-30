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
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.nativ.*;
import org.westmalle.wayland.output.OutputFactory;
import org.westmalle.wayland.output.OutputGeometry;
import org.westmalle.wayland.output.OutputMode;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;

public class X11OutputFactory {

    @Nonnull
    private final LibX11          libX11;
    @Nonnull
    private final Libxcb          libxcb;
    @Nonnull
    private final LibX11xcb       libX11xcb;
    @Nonnull
    private final WlOutputFactory wlOutputFactory;
    @Nonnull
    private final OutputFactory   outputFactory;
    @Nonnull
    private final XOutputFactory  xOutputFactory;

    @Inject
    X11OutputFactory(@Nonnull final LibX11 libX11,
                     @Nonnull final Libxcb libxcb,
                     @Nonnull final LibX11xcb libX11xcb,
                     @Nonnull final WlOutputFactory wlOutputFactory,
                     @Nonnull final OutputFactory outputFactory,
                     @Nonnull final XOutputFactory xOutputFactory) {
        this.libX11 = libX11;
        this.libxcb = libxcb;
        this.libX11xcb = libX11xcb;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.xOutputFactory = xOutputFactory;
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


    private WlOutput createXPlatformOutput(final String xDisplay,
                                           final int width,
                                           final int height) {

        final Pointer display = this.libX11.XOpenDisplay(xDisplay);
        if (display == null) {
            throw new RuntimeException("XOpenDisplay() failed: " + xDisplay);
        }

        final Pointer connection = this.libX11xcb.XGetXCBConnection(display);
        if (connection == null) {
            throw new RuntimeException("XGetXCBConnection() failed");
        }
        if (this.libxcb.xcb_connection_has_error(connection) != 0) {
            throw new RuntimeException("errors occured in connecting to X server");
        }

        final Pointer      setup  = this.libxcb.xcb_get_setup(connection);
        final xcb_screen_iterator_t screenIterator = this.libxcb.xcb_setup_roots_iterator(setup);
        final xcb_screen_t screen = screenIterator.data;

        final int window = this.libxcb.xcb_generate_id(connection);
        if (window <= 0) {
            throw new RuntimeException("failed to generate X window id");
        }

        final int     xcbWindowAttribMask = Libxcb.XCB_CW_EVENT_MASK;
        final Pointer xcbWindowAttribList = new Memory(Integer.BYTES * 3);
        //TODO just write an int array...
        xcbWindowAttribList.setInt(0,
                                   Libxcb.XCB_EVENT_MASK_BUTTON_PRESS);
        xcbWindowAttribList.setInt(4,
                                   Libxcb.XCB_EVENT_MASK_EXPOSURE);
        xcbWindowAttribList.setInt(8,
                                   Libxcb.XCB_EVENT_MASK_KEY_PRESS);
        final xcb_void_cookie_t createCookie = this.libxcb.xcb_create_window_checked(connection,
                                                                                     (byte) Libxcb.XCB_COPY_FROM_PARENT,
                                                                                     window,
                                                                                     screen.root,
                                                                                     (short) 0,
                                                                                     (short) 0,
                                                                                     (short) width,
                                                                                     (short) height,
                                                                                     (short) 0,
                                                                                     (short) Libxcb.XCB_WINDOW_CLASS_INPUT_OUTPUT,
                                                                                     screen.root_visual,
                                                                                     xcbWindowAttribMask,
                                                                                     xcbWindowAttribList);
        final xcb_void_cookie_t mapCookie = this.libxcb.xcb_map_window_checked(connection,
                                                                               window);
        xcb_generic_error_t error;
        error = this.libxcb.xcb_request_check(connection,
                                              createCookie);
        if (error != null) {
            throw new RuntimeException("failed to create X window: " + error.error_code);
        }
        error = this.libxcb.xcb_request_check(connection,
                                              mapCookie);
        if (error != null) {
            throw new RuntimeException("failed to map X window: " + error.error_code);
        }

        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .x(0)
                                                            .y(0)
                                                            .subpixel(0)
                                                            .make("Westmalle xcb")
                                                            .model("X11")
                                                            .physicalWidth((width / screen.width_in_pixels) * screen.width_in_millimeters)
                                                            .physicalHeight((height / screen.height_in_pixels) * screen.height_in_millimeters)
                                                            .transform(WlOutputTransform.NORMAL.getValue())
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .flags(0)
                                                .height(height)
                                                .width(width)
                                                .refresh(60)
                                                .build();
        return this.wlOutputFactory.create(this.outputFactory.create(outputGeometry,
                                                                     outputMode,
                                                                     this.xOutputFactory.create(display,
                                                                                                window)));
    }
}
