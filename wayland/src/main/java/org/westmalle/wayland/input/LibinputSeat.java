package org.westmalle.wayland.input;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.Seat;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libinput.Libinput;
import org.westmalle.wayland.nativ.libinput.libinput_interface;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Pointer.wrap;
import static org.freedesktop.wayland.shared.WlSeatCapability.KEYBOARD;
import static org.freedesktop.wayland.shared.WlSeatCapability.POINTER;
import static org.freedesktop.wayland.shared.WlSeatCapability.TOUCH;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_DEVICE_CAP_KEYBOARD;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_DEVICE_CAP_POINTER;
import static org.westmalle.wayland.nativ.libinput.Libinput.LIBINPUT_DEVICE_CAP_TOUCH;
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
    private final Display               display;
    @Nonnull
    private final Libudev               libudev;
    @Nonnull
    private final Libinput              libinput;
    @Nonnull
    private final Libc                  libc;
    @Nonnull
    private final LibinputDeviceFactory libinputDeviceFactory;
    @Nonnull
    private final Compositor            compositor;
    @Nonnull
    private final WlSeat                wlSeat;

    private final Set<LibinputDevice> libinputDevices = new HashSet<>();

    LibinputSeat(@Provided @Nonnull final Display display,
                 @Provided @Nonnull final Libudev libudev,
                 @Provided @Nonnull final Libinput libinput,
                 @Provided @Nonnull final Libc libc,
                 @Provided @Nonnull final LibinputDeviceFactory libinputDeviceFactory,
                 @Provided @Nonnull final Compositor compositor,
                 @Nonnull final WlSeat wlSeat) {
        this.display = display;
        this.libudev = libudev;
        this.libinput = libinput;
        this.libc = libc;
        this.libinputDeviceFactory = libinputDeviceFactory;
        this.compositor = compositor;
        this.wlSeat = wlSeat;
    }

    //TODO unit test all possible events that can occur after call to open
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
        final int  eventType = this.libinput.libinput_event_get_type(event);
        final long device    = this.libinput.libinput_event_get_device(event);
        switch (eventType) {
            case LIBINPUT_EVENT_NONE:
                //no more events
                break;
            case LIBINPUT_EVENT_DEVICE_ADDED:
                handleDeviceAdded(device);
                break;
            case LIBINPUT_EVENT_DEVICE_REMOVED:
                handleDeviceRemoved(device);
                break;
            default:
                processDeviceEvent(event,
                                   eventType,
                                   device);
                break;
        }
    }

    private void processDeviceEvent(final long event,
                                    final int eventType,
                                    final long device) {
        final long deviceData = this.libinput.libinput_device_get_user_data(device);
        if (deviceData == 0L) {
            //device was not mapped to a device we can handle
            return;
        }
        final LibinputDevice libinputDevice = wrap(LibinputDevice.class,
                                                   deviceData).dref();

        switch (eventType) {
            case LIBINPUT_EVENT_KEYBOARD_KEY:
                libinputDevice.handleKeyboardKey(this.libinput.libinput_event_get_keyboard_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_MOTION:
                libinputDevice.handlePointerMotion(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE:
                libinputDevice.handlePointerMotionAbsolute(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_BUTTON:
                libinputDevice.handlePointerButton(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_POINTER_AXIS:
                libinputDevice.handlePointerAxis(this.libinput.libinput_event_get_pointer_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_DOWN:
                libinputDevice.handleTouchDown(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_MOTION:
                libinputDevice.handleTouchMotion(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_UP:
                libinputDevice.handleTouchUp(this.libinput.libinput_event_get_touch_event(event));
                break;
            case LIBINPUT_EVENT_TOUCH_FRAME:
                libinputDevice.handleTouchFrame(this.libinput.libinput_event_get_touch_event(event));
                break;
            default:
                //unsupported libinput event
                break;
        }
    }

    private void handleDeviceAdded(final long device) {
        //check device capabilities, if it's not a touch, pointer or keyboard, we're not interested.
        final EnumSet<WlSeatCapability> deviceCapabilities = EnumSet.noneOf(WlSeatCapability.class);

        if (this.libinput.libinput_device_has_capability(device,
                                                         LIBINPUT_DEVICE_CAP_KEYBOARD) != 0) {
            deviceCapabilities.add(KEYBOARD);
        }
        if (this.libinput.libinput_device_has_capability(device,
                                                         LIBINPUT_DEVICE_CAP_POINTER) != 0) {
            deviceCapabilities.add(POINTER);
        }
        if (this.libinput.libinput_device_has_capability(device,
                                                         LIBINPUT_DEVICE_CAP_TOUCH) != 0) {
            deviceCapabilities.add(TOUCH);
        }

        if (deviceCapabilities.isEmpty()) {
            return;
        }

        //TODO configure device

        final LibinputDevice libinputDevice = this.libinputDeviceFactory.create(this.wlSeat,
                                                                                device,
                                                                                deviceCapabilities);
        this.libinput.libinput_device_set_user_data(device,
                                                    Pointer.from(libinputDevice).address);
        this.libinput.libinput_device_ref(device);
        this.libinputDevices.add(libinputDevice);

        emitSeatCapabilities();
    }

    private void handleDeviceRemoved(final long device) {
        final long deviceData = this.libinput.libinput_device_get_user_data(device);
        if (deviceData == 0L) {
            //device is not handled by us
            return;
        }

        final Pointer<LibinputDevice> devicePointer = Pointer.wrap(LibinputDevice.class,
                                                                   deviceData);
        final LibinputDevice libinputDevice = devicePointer.dref();
        this.libinputDevices.remove(libinputDevice);
        devicePointer.close();
        this.libinput.libinput_device_unref(device);

        emitSeatCapabilities();
    }

    private void emitSeatCapabilities() {

        final EnumSet<WlSeatCapability> seatCapabilities = EnumSet.noneOf(WlSeatCapability.class);

        for (final LibinputDevice libinputDevice : this.libinputDevices) {
            seatCapabilities.addAll(libinputDevice.getDeviceCapabilities());
        }

        final Seat seat = this.wlSeat.getSeat();
        seat.setCapabilities(seatCapabilities);
        seat.emitCapabilities(this.wlSeat.getResources());
    }
}
