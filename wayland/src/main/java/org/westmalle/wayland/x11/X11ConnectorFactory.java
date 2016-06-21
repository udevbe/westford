package org.westmalle.wayland.x11;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_client_message_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_screen_iterator_t;
import org.westmalle.wayland.nativ.libxcb.xcb_screen_t;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

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

public class X11ConnectorFactory {

    @Nonnull
    private final Libxcb                     libxcb;
    @Nonnull
    private final WlOutputFactory            wlOutputFactory;
    @Nonnull
    private final OutputFactory              outputFactory;
    @Nonnull
    private final X11Platform                x11Platform;
    @Nonnull
    private final PrivateX11ConnectorFactory privateX11ConnectorFactory;

    @Inject
    X11ConnectorFactory(@Nonnull final Libxcb libxcb,
                        @Nonnull final WlOutputFactory wlOutputFactory,
                        @Nonnull final OutputFactory outputFactory,
                        @Nonnull final X11Platform x11Platform,
                        @Nonnull final PrivateX11ConnectorFactory privateX11ConnectorFactory) {
        this.libxcb = libxcb;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.x11Platform = x11Platform;
        this.privateX11ConnectorFactory = privateX11ConnectorFactory;
    }

    public X11Connector create(final int width,
                               final int height) {

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Got negative width or height");
        }

        final long xcbConnection = this.x11Platform.getXcbConnection();

        final long         setup = this.libxcb.xcb_get_setup(xcbConnection);
        final xcb_screen_t screen;
        try (final Pointer<xcb_screen_iterator_t> xcb_screen_iterator = Pointer.wrap(xcb_screen_iterator_t.class,
                                                                                     this.libxcb.xcb_setup_roots_iterator(setup))) {
            screen = xcb_screen_iterator.dref()
                                        .data()
                                        .dref();
        }

        final int window = this.libxcb.xcb_generate_id(xcbConnection);
        if (window <= 0) {
            throw new RuntimeException("failed to generate X window id");
        }

        final int mask = XCB_CW_EVENT_MASK;
        final Pointer<Integer> values = Pointer.nref(XCB_EVENT_MASK_KEY_PRESS |
                                                     XCB_EVENT_MASK_KEY_RELEASE |
                                                     XCB_EVENT_MASK_BUTTON_PRESS |
                                                     XCB_EVENT_MASK_BUTTON_RELEASE |
                                                     XCB_EVENT_MASK_ENTER_WINDOW |
                                                     XCB_EVENT_MASK_LEAVE_WINDOW |
                                                     XCB_EVENT_MASK_POINTER_MOTION |
                                                     XCB_EVENT_MASK_KEYMAP_STATE |
                                                     XCB_EVENT_MASK_FOCUS_CHANGE);
        this.libxcb.xcb_create_window(xcbConnection,
                                      (byte) XCB_COPY_FROM_PARENT,
                                      window,
                                      screen.root(),
                                      (short) 0,
                                      (short) 0,
                                      (short) width,
                                      (short) height,
                                      (short) 0,
                                      (short) XCB_WINDOW_CLASS_INPUT_OUTPUT,
                                      screen.root_visual(),
                                      mask,
                                      values.address);

        final Map<String, Integer> x11Atoms    = this.x11Platform.getX11Atoms();
        final X11EventBus          x11EventBus = this.x11Platform.getX11EventBus();

        setWmProtocol(xcbConnection,
                      window,
                      x11Atoms.get("WM_PROTOCOLS"),
                      x11Atoms.get("WM_DELETE_WINDOW"));
        setName(xcbConnection,
                window,
                x11Atoms);
        x11EventBus.getXEventSignal()
                   .connect(event -> {
                       final int responseType = (event.dref()
                                                      .response_type() & ~0x80);
                       switch (responseType) {
                           case XCB_CLIENT_MESSAGE: {
                               X11ConnectorFactory.this.handle(event.castp(xcb_client_message_event_t.class),
                                                               x11Atoms,
                                                               window);
                               break;
                           }
                       }
                   });

        this.libxcb.xcb_map_window(xcbConnection,
                                   window);

        final Output output = createOutput(width,
                                           height,
                                           screen);
        final WlOutput wlOutput = this.wlOutputFactory.create(output);

        return this.privateX11ConnectorFactory.create(window,
                                                      Optional.of(wlOutput));
    }

    private Output createOutput(final int width,
                                final int height,
                                final xcb_screen_t screen) {
        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .x(0)
                                                            .y(0)
                                                            .subpixel(0)
                                                            .make("Westmalle xcb")
                                                            .model("X11")
                                                            .physicalWidth((width / screen.width_in_pixels())
                                                                           * screen.width_in_millimeters())
                                                            .physicalHeight((height / screen.height_in_pixels())
                                                                            * screen.height_in_millimeters())
                                                            .transform(WlOutputTransform.NORMAL.value)
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .flags(0)
                                                .height(height)
                                                .width(width)
                                                .refresh(60)
                                                .build();
        return this.outputFactory.create(outputGeometry,
                                         outputMode);
    }

    private void setWmProtocol(final long connection,
                               final int window,
                               final int wmProtocols,
                               final int wmDeleteWindow) {
        this.libxcb.xcb_change_property(connection,
                                        (byte) XCB_PROP_MODE_REPLACE,
                                        window,
                                        wmProtocols,
                                        XCB_ATOM_ATOM,
                                        (byte) 32,
                                        1,
                                        Pointer.nref(wmDeleteWindow).address);
    }

    private void setName(final long connection,
                         final int window,
                         final Map<String, Integer> x11Atoms) {
        final String name       = "Westmalle";
        final int    nameLength = name.length();
        final long   nameNative = Pointer.nref(name).address;

        this.libxcb.xcb_change_property(connection,
                                        (byte) XCB_PROP_MODE_REPLACE,
                                        window,
                                        x11Atoms.get("_NET_WM_NAME"),
                                        x11Atoms.get("UTF8_STRING"),
                                        (byte) 8,
                                        nameLength,
                                        nameNative);
        this.libxcb.xcb_change_property(connection,
                                        (byte) XCB_PROP_MODE_REPLACE,
                                        window,
                                        x11Atoms.get("WM_CLASS"),
                                        x11Atoms.get("STRING"),
                                        (byte) 8,
                                        nameLength,
                                        nameNative);
    }

    private void handle(final Pointer<xcb_client_message_event_t> event,
                        final Map<String, Integer> x11Atoms,
                        final int window) {
        final int atom = event.dref()
                              .data()
                              .data32()
                              .dref();
        final int sourceWindow = event.dref()
                                      .window();
        if (atom == x11Atoms.get("WM_DELETE_WINDOW") &&
            window == sourceWindow) {
            System.exit(0);
        }
    }

}
