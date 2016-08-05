//Copyright 2016 Erik De Rijcke
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
package org.westmalle.wayland.drm;


import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.libdrm.DrmModeConnector;
import org.westmalle.wayland.nativ.libdrm.DrmModeEncoder;
import org.westmalle.wayland.nativ.libdrm.DrmModeModeInfo;
import org.westmalle.wayland.nativ.libdrm.DrmModeRes;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.protocol.WlOutputFactory;
import org.westmalle.wayland.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.freedesktop.jaccall.Pointer.nref;
import static org.freedesktop.jaccall.Pointer.wrap;
import static org.westmalle.wayland.nativ.glibc.Libc.O_RDWR;
import static org.westmalle.wayland.nativ.libdrm.Libdrm.DRM_MODE_CONNECTED;

//TODO tests tests tests!
public class DrmPlatformFactory {

    @Nonnull
    private final Libudev                   libudev;
    @Nonnull
    private final Libc                      libc;
    @Nonnull
    private final Libdrm                    libdrm;
    @Nonnull
    private final Display                   display;
    @Nonnull
    private final DrmConnectorFactory       drmConnectorFactory;
    @Nonnull
    private final DrmEventBusFactory        drmEventBusFactory;
    @Nonnull
    private final PrivateDrmPlatformFactory privateDrmPlatformFactory;
    @Nonnull
    private final WlOutputFactory           wlOutputFactory;
    @Nonnull
    private final OutputFactory             outputFactory;
    @Nonnull
    private final Tty                       tty;

    @Inject
    DrmPlatformFactory(@Nonnull final Libc libc,
                       @Nonnull final Libudev libudev,
                       @Nonnull final Libdrm libdrm,
                       @Nonnull final Display display,
                       @Nonnull final DrmConnectorFactory drmConnectorFactory,
                       @Nonnull final DrmEventBusFactory drmEventBusFactory,
                       @Nonnull final PrivateDrmPlatformFactory privateDrmPlatformFactory,
                       @Nonnull final WlOutputFactory wlOutputFactory,
                       @Nonnull final OutputFactory outputFactory,
                       @Nonnull final Tty tty) {
        this.libudev = libudev;
        this.libc = libc;
        this.libdrm = libdrm;
        this.display = display;
        this.drmConnectorFactory = drmConnectorFactory;
        this.drmEventBusFactory = drmEventBusFactory;
        this.privateDrmPlatformFactory = privateDrmPlatformFactory;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.tty = tty;
    }

    public DrmPlatform create() {
        final long udev = this.libudev.udev_new();
        if (udev == 0L) {
            throw new RuntimeException("Failed to initialize udev");
        }
        //TODO seat from config
        final long drmDevice = findPrimaryGpu(udev,
                                              "seat0");
        if (drmDevice == 0L) {
            throw new RuntimeException("No drm capable gpu device found.");
        }

        final int drmFd = initDrm(drmDevice);

        final List<Optional<DrmConnector>> drmConnectors = createDrmConnectors(drmFd);

        final DrmEventBus drmEventBus = this.drmEventBusFactory.create(drmFd);
        this.display.getEventLoop()
                    .addFileDescriptor(drmFd,
                                       WaylandServerCore.WL_EVENT_READABLE,
                                       drmEventBus);

        this.tty.getVtEnterSignal()
                .connect(event -> setDrmMaster(drmFd));
        this.tty.getVtLeaveSignal()
                .connect(event -> dropDrmMaster(drmFd));

        setDrmMaster(drmFd);

        return this.privateDrmPlatformFactory.create(drmDevice,
                                                     drmFd,
                                                     drmEventBus,
                                                     drmConnectors);
    }

    private void setDrmMaster(final int drmFd) {
        if (this.libdrm.drmSetMaster(drmFd) != 0) {
            throw new RuntimeException("failed to set drm master.");
        }
    }

    private void dropDrmMaster(final int drmFd) {
        if (this.libdrm.drmDropMaster(drmFd) != 0) {
            throw new RuntimeException("failed to drop drm master.");
        }
    }


    private List<Optional<DrmConnector>> createDrmConnectors(final int drmFd) {
        final long resources = this.libdrm.drmModeGetResources(drmFd);
        if (resources == 0L) {
            throw new RuntimeException("Getting drm resources failed.");
        }

        final DrmModeRes drmModeRes = wrap(DrmModeRes.class,
                                           resources).dref();

        final int                          countConnectors = drmModeRes.count_connectors();
        final List<Optional<DrmConnector>> drmConnectors   = new ArrayList<>(countConnectors);
        final Set<Integer>                 usedCrtcs       = new HashSet<>();

        for (int i = 0; i < countConnectors; i++) {
            final long connector = this.libdrm.drmModeGetConnector(drmFd,
                                                                   drmModeRes.connectors()
                                                                             .dref(i));
            if (connector == 0L) {
                continue;
            }

            final DrmModeConnector drmModeConnector = wrap(DrmModeConnector.class,
                                                           connector).dref();
            final Optional<DrmConnector> drmConnector;
            if (drmModeConnector.connection() == DRM_MODE_CONNECTED) {
                drmConnector = createDrmConnector(drmFd,
                                                  drmModeRes,
                                                  drmModeConnector,
                                                  usedCrtcs);
            }
            else {
                drmConnector = Optional.empty();
            }

            drmConnectors.add(drmConnector);
        }

        return drmConnectors;
    }

    private Optional<DrmConnector> createDrmConnector(final int drmFd,
                                                      final DrmModeRes drmModeRes,
                                                      final DrmModeConnector drmModeConnector,
                                                      final Set<Integer> crtcAllocations) {
        return findCrtcIdForConnector(drmFd,
                                      drmModeRes,
                                      drmModeConnector,
                                      crtcAllocations).flatMap(crtcId -> createDrmConnector(drmModeRes,
                                                                                            drmModeConnector,
                                                                                            crtcId));
    }

    private Optional<Integer> findCrtcIdForConnector(final int drmFd,
                                                     final DrmModeRes drmModeRes,
                                                     final DrmModeConnector drmModeConnector,
                                                     final Set<Integer> crtcAllocations) {

        for (int j = 0; j < drmModeConnector.count_encoders(); j++) {
            final long encoder = this.libdrm.drmModeGetEncoder(drmFd,
                                                               drmModeConnector.encoders()
                                                                               .dref(j));
            if (encoder == 0L) {
                return Optional.empty();
            }

            //bitwise flag of available crtcs, each bit represents the index of crtcs in drmModeRes
            final int possibleCrtcs = wrap(DrmModeEncoder.class,
                                           encoder).dref()
                                                   .possible_crtcs();
            this.libdrm.drmModeFreeEncoder(encoder);

            for (int i = 0; i < drmModeRes.count_crtcs(); i++) {
                if ((possibleCrtcs & (1 << i)) != 0 &&
                    crtcAllocations.add(drmModeRes.crtcs()
                                                  .dref(i))) {
                    return Optional.of(drmModeRes.crtcs()
                                                 .dref(i));
                }
            }
        }

        return Optional.empty();
    }

    private Optional<DrmConnector> createDrmConnector(final DrmModeRes drmModeRes,
                                                      final DrmModeConnector drmModeConnector,
                                                      final int crtcId) {
        /* find highest resolution mode: */
        int             area = 0;
        DrmModeModeInfo mode = null;
        for (int i = 0; i < drmModeConnector.count_modes(); i++) {
            final DrmModeModeInfo currentMode = drmModeConnector.modes()
                                                                .dref(i);
            final int current_area = currentMode.hdisplay() * currentMode.vdisplay();
            if (current_area > area) {
                mode = currentMode;
                area = current_area;
            }
        }

        if (mode == null) {
            throw new RuntimeException("Could not find a valid mode.");
        }

        final int fallBackDpi = 96;

        int         mmWidth  = drmModeConnector.mmWidth();
        final short hdisplay = mode.hdisplay();
        if (mmWidth == 0) {
            mmWidth = (int) ((hdisplay * 25.4) / fallBackDpi);
        }

        int         mmHeight = drmModeConnector.mmHeight();
        final short vdisplay = mode.vdisplay();
        if (mmHeight == 0) {
            mmHeight = (int) ((vdisplay * 25.4) / fallBackDpi);
        }

        //TODO gather more geo & mode info
        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .physicalWidth(mmWidth)
                                                            .physicalHeight(mmHeight)
                                                            .make("unknown")
                                                            .model("unknown")
                                                            .x(0)
                                                            .y(0)
                                                            .subpixel(drmModeConnector.drmModeSubPixel())
                                                            .transform(0)
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .width(hdisplay)
                                                .height(vdisplay)
                                                .refresh(mode.vrefresh())
                                                .flags(mode.flags())
                                                .build();

        //FIXME decuse an output name from the drm connector
        return Optional.of(this.drmConnectorFactory.create(this.wlOutputFactory.create(this.outputFactory.create("dummy",
                                                                                                                 outputGeometry,
                                                                                                                 outputMode)),
                                                           drmModeRes,
                                                           drmModeConnector,
                                                           crtcId,
                                                           mode));
    }


    private int initDrm(final long device) {
        final long sysnum = this.libudev.udev_device_get_sysnum(device);
        final int  drmId;
        if (sysnum != 0) {
            drmId = Integer.parseInt(wrap(String.class,
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
                deviceSeat = wrap(String.class,
                                  seatId).dref();
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
                if (id != 0L && wrap(String.class,
                                     id).dref()
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
