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
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.nativ.NativeString;
import org.westmalle.wayland.nativ.libX11.LibX11;
import org.westmalle.wayland.nativ.libX11xcb.LibX11xcb;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_client_message_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_intern_atom_cookie_t;
import org.westmalle.wayland.nativ.libxcb.xcb_intern_atom_reply_t;
import org.westmalle.wayland.nativ.libxcb.xcb_screen_t;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libX11xcb.LibX11xcb.XCBOwnsEventQueue;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_ATOM_ATOM;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_CLIENT_MESSAGE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_COPY_FROM_PARENT;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_CW_EVENT_MASK;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_ENTER_WINDOW;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_FOCUS_CHANGE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_KEYMAP_STATE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_KEY_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_KEY_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_LEAVE_WINDOW;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_POINTER_MOTION;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_PROP_MODE_REPLACE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_WINDOW_CLASS_INPUT_OUTPUT;

public class X11OutputFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final Display                 display;
    @Nonnull
    private final Libc                    libc;
    @Nonnull
    private final LibX11                  libX11;
    @Nonnull
    private final Libxcb                  libxcb;
    @Nonnull
    private final LibX11xcb               libX11xcb;
    @Nonnull
    private final PrivateX11OutputFactory privateX11OutputFactory;
    @Nonnull
    private final WlOutputFactory         wlOutputFactory;
    @Nonnull
    private final OutputFactory           outputFactory;
    @Nonnull
    private final X11EventBusFactory      x11EventBusFactory;

    @Inject
    X11OutputFactory(@Nonnull final Display display,
                     @Nonnull final Libc libc,
                     @Nonnull final LibX11 libX11,
                     @Nonnull final Libxcb libxcb,
                     @Nonnull final LibX11xcb libX11xcb,
                     @Nonnull final PrivateX11OutputFactory privateX11OutputFactory,
                     @Nonnull final WlOutputFactory wlOutputFactory,
                     @Nonnull final OutputFactory outputFactory,
                     @Nonnull final X11EventBusFactory x11EventBusFactory) {
        this.display = display;
        this.libc = libc;
        this.libX11 = libX11;
        this.libxcb = libxcb;
        this.libX11xcb = libX11xcb;
        this.privateX11OutputFactory = privateX11OutputFactory;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.x11EventBusFactory = x11EventBusFactory;
    }

    public WlOutput create(@Nonnull final String xDisplay,
                           @Nonnegative final int width,
                           @Nonnegative final int height) {
        LOGGER.info(format("Creating X11 output:\n"
                           + "\tDisplay: %s\n"
                           + "\tWindow geometry: %dx%d",
                           xDisplay,
                           width,
                           height));
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Got negative width or height");
        }

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
        if (this.libxcb.xcb_connection_has_error(xcbConnection) != 0) {
            throw new RuntimeException("error occurred while connecting to X server");
        }

        final Pointer      setup  = this.libxcb.xcb_get_setup(xcbConnection);
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
        this.libxcb.xcb_flush(xcbConnection);
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

        final int     mask   = XCB_CW_EVENT_MASK;
        final Pointer values = new Memory(Integer.BYTES);
        values.write(0,
                     new int[]{
                             XCB_EVENT_MASK_KEY_PRESS |
                             XCB_EVENT_MASK_KEY_RELEASE |
                             XCB_EVENT_MASK_BUTTON_PRESS |
                             XCB_EVENT_MASK_BUTTON_RELEASE |
                             XCB_EVENT_MASK_ENTER_WINDOW |
                             XCB_EVENT_MASK_LEAVE_WINDOW |
                             XCB_EVENT_MASK_POINTER_MOTION |
                             XCB_EVENT_MASK_KEYMAP_STATE |
                             XCB_EVENT_MASK_FOCUS_CHANGE
                     },
                     0,
                     1);
        this.libxcb.xcb_create_window(xcbConnection,
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
                                      mask,
                                      values);
        this.libxcb.xcb_map_window(xcbConnection,
                                   window);

        return window;
    }

    private X11Output createX11Output(final Pointer xDisplay,
                                      final Pointer connection,
                                      final int window) {
        final X11EventBus x11EventBus = this.x11EventBusFactory.create(connection);
        this.display.getEventLoop()
                    .addFileDescriptor(this.libxcb.xcb_get_file_descriptor(connection),
                                       EventLoop.EVENT_READABLE,
                                       x11EventBus)
                    .check();
        final Map<String, Integer> x11Atoms = internX11Atoms(connection);
        setWmProtocol(connection,
                      window,
                      x11Atoms.get("WM_PROTOCOLS"),
                      x11Atoms.get("WM_DELETE_WINDOW"));
        setName(connection,
                window,
                x11Atoms);
        x11EventBus.getXEventSignal()
                   .connect(event -> {
                       final int responseType = (event.response_type & ~0x80);
                       switch (responseType) {
                           case XCB_CLIENT_MESSAGE: {
                               final xcb_client_message_event_t client_message_event = new xcb_client_message_event_t(event.getPointer());
                               client_message_event.read();
                               X11OutputFactory.this.handle(client_message_event,
                                                            x11Atoms,
                                                            window);
                               break;
                           }
                       }
                   });

        return this.privateX11OutputFactory.create(x11EventBus,
                                                   connection,
                                                   xDisplay,
                                                   window,
                                                   x11Atoms);
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

    private Map<String, Integer> internX11Atoms(final Pointer connection) {

        final String[] atomNames = {
                "WM_PROTOCOLS",
                "WM_NORMAL_HINTS",
                "WM_SIZE_HINTS",
                "WM_DELETE_WINDOW",
                "WM_CLASS",
                "_NET_WM_NAME",
                "_NET_WM_ICON",
                "_NET_WM_STATE",
                "_NET_WM_STATE_FULLSCREEN",
                "_NET_SUPPORTING_WM_CHECK",
                "_NET_SUPPORTED",
                "STRING",
                "UTF8_STRING",
                "CARDINAL",
                "_XKB_RULES_NAMES"
        };

        final xcb_intern_atom_cookie_t.ByValue cookies[] = new xcb_intern_atom_cookie_t.ByValue[atomNames.length];
        for (int i = 0; i < atomNames.length; i++) {
            final NativeString nativeAtomName = new NativeString(atomNames[i]);
            cookies[i] = this.libxcb.xcb_intern_atom(connection,
                                                     (byte) 0,
                                                     (short) nativeAtomName.length(),
                                                     nativeAtomName.getPointer());
        }

        final Map<String, Integer> x11Atoms = new HashMap<>(atomNames.length);
        for (int i = 0; i < atomNames.length; i++) {
            final xcb_intern_atom_reply_t reply = this.libxcb.xcb_intern_atom_reply(connection,
                                                                                    cookies[i],
                                                                                    null);
            x11Atoms.put(atomNames[i],
                         reply.atom);
            this.libc.free(reply.getPointer());
        }
        return x11Atoms;
    }

    private void setWmProtocol(final Pointer connection,
                               final int window,
                               final int wmProtocols,
                               final int wmDeleteWindow) {
        final Pointer list = new Memory(Integer.BYTES);
        list.setInt(0,
                    wmDeleteWindow);
        this.libxcb.xcb_change_property(connection,
                                        (byte) XCB_PROP_MODE_REPLACE,
                                        window,
                                        wmProtocols,
                                        XCB_ATOM_ATOM,
                                        (byte) 32,
                                        1,
                                        list);
    }

    private void setName(final Pointer connection,
                         final int window,
                         final Map<String, Integer> x11Atoms) {
        final NativeString titleNativeString = new NativeString("Westmalle");
        this.libxcb.xcb_change_property(connection,
                                        (byte) XCB_PROP_MODE_REPLACE,
                                        window,
                                        x11Atoms.get("_NET_WM_NAME"),
                                        x11Atoms.get("UTF8_STRING"),
                                        (byte) 8,
                                        titleNativeString.length(),
                                        titleNativeString.getPointer());
        this.libxcb.xcb_change_property(connection,
                                        (byte) XCB_PROP_MODE_REPLACE,
                                        window,
                                        x11Atoms.get("WM_CLASS"),
                                        x11Atoms.get("STRING"),
                                        (byte) 8,
                                        titleNativeString.length(),
                                        titleNativeString.getPointer());
    }

    private void handle(final xcb_client_message_event_t event,
                        final Map<String, Integer> x11Atoms,
                        final int window) {
        final int atom         = event.data.data32[0];
        final int sourceWindow = event.window;
        if (atom == x11Atoms.get("WM_DELETE_WINDOW") &&
            window == sourceWindow) {
            System.exit(0);
        }
    }
}
