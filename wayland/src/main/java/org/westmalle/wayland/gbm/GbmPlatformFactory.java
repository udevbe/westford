package org.westmalle.wayland.gbm;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.nativ.libudev.Libudev;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.freedesktop.jaccall.Pointer.nref;
import static org.westmalle.wayland.nativ.libc.Libc.O_RDWR;

public class GbmPlatformFactory {

    @Nonnull
    private final Libudev libudev;
    @Nonnull
    private final Libc    libc;
    @Nonnull
    private final Libgbm  libgbm;

    @Inject
    GbmPlatformFactory(@Nonnull final Libc libc,
                       @Nonnull final Libudev libudev,
                       @Nonnull final Libgbm libgbm) {
        this.libudev = libudev;
        this.libc = libc;
        this.libgbm = libgbm;
    }

    public GbmPlatform create() {

        //TODO seat from config
        final long primaryGpu = findPrimaryGpu(udev,
                                               "seat0");
        final int  drmFd     = initDrm(primaryGpu);
        final long gbmDevice = this.libgbm.gbm_create_device(drmFd);

    }


    private int initDrm(final long device) {
        final long sysnum = this.libudev.udev_device_get_sysnum(device);
        final int  drmId;
        if (sysnum != 0) {
            drmId = Integer.parseInt(Pointer.wrap(String.class,
                                                  sysnum)
                                            .dref());
        }
        else {
            drmId = 0;
        }
        if (sysnum == 0 || drmId < 0) {
            throw new RuntimeException("Failed to open drm device.");
        }

        final long filename = this.libudev.udev_device_get_devnode(device);
        final int fd = this.libc.open(filename,
                                      O_RDWR);
        if (fd < 0) {
            throw new RuntimeException("Failed to open drm device.");
        }

        return fd;
    }

    /*
     * Find primary GPU
     * Some systems may have multiple DRM devices attached to a single seat. This
     * function loops over all devices and tries to find a PCI device with the
     * boot_vga sysfs attribute set to 1.
     * If no such device is found, the first DRM device reported by udev is used.
     */
    private long findPrimaryGpu(final long udev,
                                final String seat) {

        final long udevEnumerate = this.libudev.udev_enumerate_new(udev);
        this.libudev.udev_enumerate_add_match_subsystem(udevEnumerate,
                                                        nref("drm").address);
        this.libudev.udev_enumerate_add_match_sysname(udevEnumerate,
                                                      nref("card[0-9]*").address);

        this.libudev.udev_enumerate_scan_devices(udevEnumerate);
        long drmDevice = 0L;

        for (long entry = this.libudev.udev_enumerate_get_list_entry(udevEnumerate);
             entry != 0L;
             entry = this.libudev.udev_list_entry_get_next(entry)) {

            final long path = this.libudev.udev_list_entry_get_name(entry);
            final long device = this.libudev.udev_device_new_from_syspath(udev,
                                                                          path);
            if (device == 0) {
                //no device, process next entry
                continue;

            }
            final String deviceSeat;
            final long seatId = this.libudev.udev_device_get_property_value(device,
                                                                            nref("ID_SEAT").address);
            if (seatId == 0) {
                //device does not have a seat, assign it a default one.
                deviceSeat = Libudev.DEFAULT_SEAT;
            }
            else {
                deviceSeat = Pointer.wrap(String.class,
                                          seatId)
                                    .dref();
            }
            if (!deviceSeat.equals(seat)) {
                //device has a seat, but not the one we want, process next entry
                this.libudev.udev_device_unref(device);
                continue;
            }

            final long pci = this.libudev.udev_device_get_parent_with_subsystem_devtype(device,
                                                                                        nref("pci").address,
                                                                                        0L);
            if (pci != 0) {
                final long id = this.libudev.udev_device_get_sysattr_value(pci,
                                                                           nref("boot_vga").address);
                if (id != 0L && Pointer.wrap(String.class,
                                             id)
                                       .dref()
                                       .equals("1")) {
                    if (drmDevice != 0L) {
                        this.libudev.udev_device_unref(drmDevice);
                    }
                    drmDevice = device;
                    break;
                }
            }

            if (drmDevice == 0L) {
                drmDevice = device;
            }
            else {
                this.libudev.udev_device_unref(device);
            }
        }

        this.libudev.udev_enumerate_unref(udevEnumerate);
        return drmDevice;
    }

}
