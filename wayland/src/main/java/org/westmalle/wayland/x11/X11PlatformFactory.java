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

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.westmalle.wayland.nativ.libX11.LibX11;
import org.westmalle.wayland.nativ.libX11xcb.LibX11xcb;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_intern_atom_reply_t;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libX11xcb.LibX11xcb.XCBOwnsEventQueue;

public class X11PlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final Display                   display;
    @Nonnull
    private final LibX11                    libX11;
    @Nonnull
    private final Libxcb                    libxcb;
    @Nonnull
    private final LibX11xcb                 libX11xcb;
    @Nonnull
    private final PrivateX11PlatformFactory privateX11PlatformFactory;
    @Nonnull
    private final X11EventBusFactory        x11EventBusFactory;

    @Inject
    X11PlatformFactory(@Nonnull final Display display,
                       @Nonnull final LibX11 libX11,
                       @Nonnull final Libxcb libxcb,
                       @Nonnull final LibX11xcb libX11xcb,
                       @Nonnull final PrivateX11PlatformFactory privateX11PlatformFactory,
                       @Nonnull final X11EventBusFactory x11EventBusFactory) {
        this.display = display;
        this.libX11 = libX11;
        this.libxcb = libxcb;
        this.libX11xcb = libX11xcb;
        this.privateX11PlatformFactory = privateX11PlatformFactory;
        this.x11EventBusFactory = x11EventBusFactory;
    }

    public X11Platform create() {
        //TODO from config
        final String xDisplayName = ":0";

        LOGGER.info(format("Creating X11 platform:\n"
                           + "\tDisplay: %s",
                           xDisplayName));

        final long xDisplay = this.libX11.XOpenDisplay(Pointer.nref(xDisplayName).address);
        if (xDisplay == 0L) {
            throw new RuntimeException("XOpenDisplay() failed: " + xDisplayName);
        }

        final long xcbConnection = this.libX11xcb.XGetXCBConnection(xDisplay);
        this.libX11xcb.XSetEventQueueOwner(xDisplay,
                                           XCBOwnsEventQueue);
        if (this.libxcb.xcb_connection_has_error(xcbConnection) != 0) {
            throw new RuntimeException("error occurred while connecting to X server");
        }

        final X11Platform x11Platform = createX11Output(xDisplay,
                                                        xcbConnection);

        this.libxcb.xcb_flush(xcbConnection);
        return x11Platform;
    }

    private X11Platform createX11Output(final long xDisplay,
                                        final long xcbConnection) {
        final X11EventBus x11EventBus = this.x11EventBusFactory.create(xcbConnection);
        this.display.getEventLoop()
                    .addFileDescriptor(this.libxcb.xcb_get_file_descriptor(xcbConnection),
                                       WaylandServerCore.WL_EVENT_READABLE,
                                       x11EventBus)
                    .check();
        final Map<String, Integer> x11Atoms = internX11Atoms(xcbConnection);

        return this.privateX11PlatformFactory.create(x11EventBus,
                                                     xcbConnection,
                                                     xDisplay,
                                                     x11Atoms);
    }

    private Map<String, Integer> internX11Atoms(final long connection) {
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

        final int[] cookies = new int[atomNames.length];
        for (int i = 0; i < atomNames.length; i++) {
            cookies[i] = this.libxcb.xcb_intern_atom(connection,
                                                     (byte) 0,
                                                     (short) atomNames[i].length(),
                                                     Pointer.nref(atomNames[i]).address);
        }

        final Map<String, Integer> x11Atoms = new HashMap<>(atomNames.length);
        for (int i = 0; i < atomNames.length; i++) {
            try (final Pointer<xcb_intern_atom_reply_t> reply = Pointer.wrap(xcb_intern_atom_reply_t.class,
                                                                             this.libxcb.xcb_intern_atom_reply(connection,
                                                                                                               cookies[i],
                                                                                                               0L))) {
                x11Atoms.put(atomNames[i],
                             reply.dref()
                                  .atom());
            }
        }
        return x11Atoms;
    }
}
