package org.westmalle.wayland.input;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libinput.Libinput;
import org.westmalle.wayland.nativ.libinput.libinput_interface;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_DEVICE_ADDED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_DEVICE_REMOVED;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_KEYBOARD_KEY;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_NONE;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_AXIS;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_BUTTON;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_MOTION;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_DOWN;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_FRAME;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_MOTION;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_EVENT_TOUCH_UP;
import static org.westmalle.wayland.nativ.libinput.Pointerclose_restricted.nref;
import static org.westmalle.wayland.nativ.libinput.Pointeropen_restricted.nref;

@AutoFactory(allowSubclasses = true,
             className = "PrivateLibinputSeatFactory")
public class LibinputSeat {

    @Nonnull
    private final Display  display;
    @Nonnull
    private final Libudev  libudev;
    @Nonnull
    private final Libinput libinput;
    @Nonnull
    private final Libc     libc;
    @Nonnull
    private final WlSeat   wlSeat;

    LibinputSeat(@Provided @Nonnull final Display display,
                 @Provided @Nonnull final Libudev libudev,
                 @Provided @Nonnull final Libinput libinput,
                 @Provided @Nonnull final Libc libc,
                 @Nonnull final WlSeat wlSeat) {
        this.display = display;
        this.libudev = libudev;
        this.libinput = libinput;
        this.libc = libc;
        this.wlSeat = wlSeat;
    }

    public void open(final String seat) {
        final long libinput = createUdevContext(seat);
        loop(libinput);
        //TODO set/update seat capabilities?
    }

    private long createUdevContext(final String seatId) {
        final long udev = this.libudev.udev_new();
        if (udev == 0L) {
            throw new RuntimeException("Failed to initialize udev");
        }

        final Pointer<libinput_interface> interface_ = malloc(libinput_interface.SIZE,
                                                              libinput_interface.class);
        interface_.dref()
                  .open_restricted(nref(this::openRestricted));
        interface_.dref()
                  .close_restricted(nref(this::closeRestricted));

        final long libinput = this.libinput.libinput_udev_create_context(interface_.address,
                                                                         0,
                                                                         udev);

        if (this.libinput.libinput_udev_assign_seat(libinput,
                                                    Pointer.nref(seatId).address) != 0) {
            this.libinput.libinput_unref(libinput);
            this.libudev.udev_unref(udev);

            throw new RuntimeException(String.format("Failed to set seat=%s",
                                                     seatId));
        }

        return libinput;
    }

    private int openRestricted(@Ptr(String.class) final long path,
                               final int flags,
                               @Ptr(Void.class) final long user_data) {
        final int fd = this.libc.open(path,
                                      flags);

        return fd < 0 ? -this.libc.getErrno() : fd;
    }

    private void closeRestricted(final int fd,
                                 @Ptr(Void.class) final long user_data) {
        this.libc.close(fd);
    }

    private void loop(final long libinput) {
        final int libinputFd = this.libinput.libinput_get_fd(libinput);
        this.display.getEventLoop()
                    .addFileDescriptor(libinputFd,
                                       WaylandServerCore.WL_EVENT_READABLE,
                                       (fd, mask) -> {
                                           if (fd == libinputFd) {
                                               processEvents(libinput);
                                           }
                                           return 0;
                                       });
    }

    private void processEvents(final long libinput) {
        this.libinput.libinput_dispatch(libinput);

        long event;
        while ((event = this.libinput.libinput_get_event(libinput)) != 0) {

            processEvent(event);

            this.libinput.libinput_event_destroy(event);
            this.libinput.libinput_dispatch(libinput);
        }
    }

    private void processEvent(final long event) {
        switch (this.libinput.libinput_event_get_type(event)) {
            case LIBINPUT_EVENT_NONE:
                break;
            case LIBINPUT_EVENT_DEVICE_ADDED:
                //TODO add seat capability
                break;
            case LIBINPUT_EVENT_DEVICE_REMOVED:
                //TODO remove seat capability
                break;
            case LIBINPUT_EVENT_KEYBOARD_KEY:
                handleKeyboardKey(this.libinput.libinput_event_get_keyboard_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_MOTION:
                handlePointerMotion(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE:
                handlePointerMotionAbsolute(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_BUTTON:
                handlePointerButton(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_AXIS:
                handlePointerAxis(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_DOWN:
                handleTouchDown(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_MOTION:
                handleTouchMotion(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_UP:
                handleTouchUp(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_FRAME:
                handleTouchFrame(this.libinput.libinput_event_get_touch_event(event));
                break;
            default:
                //unsupported libinput event
                break;
        }
    }

    private void handleTouchFrame(final long event) {

    }

    private void handleTouchUp(final long event) {

    }

    private void handleTouchMotion(final long event) {

    }

    private void handleTouchDown(final long event) {

    }

    private void handlePointerAxis(final long event) {

    }

    private void handlePointerButton(final long event) {

    }

    private void handlePointerMotionAbsolute(final long event) {

    }

    private void handlePointerMotion(final long event) {

    }

    private void handleKeyboardKey(final long event) {

    }
}
