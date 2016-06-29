package org.westmalle.wayland.drm;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libdrm.DrmEventContext;
import org.westmalle.wayland.nativ.libdrm.DrmModeConnector;
import org.westmalle.wayland.nativ.libdrm.DrmModeEncoder;
import org.westmalle.wayland.nativ.libdrm.DrmModeModeInfo;
import org.westmalle.wayland.nativ.libdrm.DrmModeRes;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libdrm.Pointerpage_flip_handler;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.freedesktop.jaccall.Pointer.nref;
import static org.freedesktop.jaccall.Pointer.wrap;
import static org.westmalle.wayland.nativ.libc.Libc.O_RDWR;
import static org.westmalle.wayland.nativ.libdrm.Libdrm.DRM_MODE_CONNECTED;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_RENDERING;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_SCANOUT;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_FORMAT_XRGB8888;

//TODO drm platform factory, remove all gbm dependencies
public class GbmPlatformFactory {

    @Nonnull
    private final Libudev             libudev;
    @Nonnull
    private final Libc                libc;
    @Nonnull
    private final Libdrm              libdrm;
    @Nonnull
    private final Libgbm              libgbm;
    @Nonnull
    private final GbmConnectorFactory gbmConnectorFactory;
    @Nonnull
    private final WlOutputFactory     wlOutputFactory;
    @Nonnull
    private final OutputFactory       outputFactory;

    @Inject
    GbmPlatformFactory(@Nonnull final Libc libc,
                       @Nonnull final Libudev libudev,
                       @Nonnull final Libdrm libdrm,
                       @Nonnull final Libgbm libgbm,
                       @Nonnull final GbmConnectorFactory gbmConnectorFactory,
                       @Nonnull final WlOutputFactory wlOutputFactory,
                       @Nonnull final OutputFactory outputFactory) {
        this.libudev = libudev;
        this.libc = libc;
        this.libdrm = libdrm;
        this.libgbm = libgbm;
        this.gbmConnectorFactory = gbmConnectorFactory;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
    }

    public GbmPlatform create() {

        //setup platform rendering handles
        //TODO seat from config
        final long drmDevice = findPrimaryGpu(udev,
                                              "seat0");
        if (drmDevice == 0L) {
            throw new RuntimeException("No drm capable gpu device found.");
        }

        final int  drmFd     = initDrm(drmDevice);
        final long gbmDevice = this.libgbm.gbm_create_device(drmFd);

        final GbmConnector[] gbmConnectors = createGbmConnectors(gbmDevice,
                                                                 drmFd);

        //setup page flipping mechanism
        final Pointer<DrmEventContext> drmEventContextP = Pointer.malloc(DrmEventContext.SIZE,
                                                                         DrmEventContext.class);
        final DrmEventContext drmEventContext = drmEventContextP.dref();
        drmEventContext.version(Libdrm.DRM_EVENT_CONTEXT_VERSION);
        drmEventContext.page_flip_handler(Pointerpage_flip_handler.nref(this::page_flip_handler));


    }



    private GbmConnector[] createGbmConnectors(final long gbmDevice,
                                               final int drmFd) {
        final long resources = this.libdrm.drmModeGetResources(drmFd);
        if (resources == 0L) {
            throw new RuntimeException("Getting drm resources failed.");
        }

        final DrmModeRes drmModeRes = wrap(DrmModeRes.class,
                                           resources).dref();

        final int            countConnectors = drmModeRes.count_connectors();
        final GbmConnector[] gbmConnectors   = new GbmConnector[countConnectors];
        final Set<Integer>   usedCrtcs       = new HashSet<>();

        for (int i = 0; i < countConnectors; i++) {
            final long connector = this.libdrm.drmModeGetConnector(drmFd,
                                                                   drmModeRes.connectors()
                                                                             .dref(i));
            if (connector == 0L) {
                continue;
            }

            final DrmModeConnector drmModeConnector = wrap(DrmModeConnector.class,
                                                           connector).dref();
            final Optional<GbmConnector> gbmConnector;
            if (drmModeConnector.connection() == DRM_MODE_CONNECTED) {
                gbmConnector = createGbmConnector(drmFd,
                                                  drmModeRes,
                                                  drmModeConnector,
                                                  usedCrtcs,
                                                  gbmDevice);
            }
            else {
                gbmConnector = Optional.empty();

            }

            gbmConnectors[i] = gbmConnector.orElseGet(() -> {
                this.libdrm.drmModeFreeConnector(connector);
                return this.gbmConnectorFactory.create(Optional.empty(),
                                                       null,
                                                       null,
                                                       -1,
                                                       null,
                                                       0L);
            });
        }

        return gbmConnectors;
    }

    private Optional<GbmConnector> createGbmConnector(final int drmFd,
                                                      final DrmModeRes drmModeRes,
                                                      final DrmModeConnector drmModeConnector,
                                                      final Set<Integer> crtcAllocations,
                                                      final long gbmDevice) {
        return findCrtcForConnector(drmFd,
                                    drmModeRes,
                                    drmModeConnector,
                                    crtcAllocations).flatMap(crtcId -> createGbmConnector(drmModeRes,
                                                                                          drmModeConnector,
                                                                                          crtcId,
                                                                                          gbmDevice));
    }

    private Optional<GbmConnector> createGbmConnector(final DrmModeRes drmModeRes,
                                                      final DrmModeConnector drmModeConnector,
                                                      final int crtcId,
                                                      final long gbmDevice) {

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

        final long gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                               mode.hdisplay(),
                                                               mode.vdisplay(),
                                                               GBM_FORMAT_XRGB8888,
                                                               GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING);
        //TODO gather more geo & mode info
        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .physicalWidth(drmModeConnector.mmWidth())
                                                            .physicalHeight(drmModeConnector.mmHeight())
                                                            .make("unknown")
                                                            .model("unknown")
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .height(mode.hdisplay())
                                                .width(mode.vdisplay())
                                                .refresh(mode.vrefresh())
                                                .flags(mode.flags())
                                                .build();

        return Optional.of(this.gbmConnectorFactory.create(Optional.of(this.wlOutputFactory.create(this.outputFactory.create(outputGeometry,
                                                                                                                             outputMode))),
                                                           drmModeRes,
                                                           drmModeConnector,
                                                           crtcId,
                                                           mode,
                                                           gbmSurface));
    }

    private Optional<Integer> findCrtcForConnector(final int drmFd,
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
                    !crtcAllocations.contains(drmModeRes.crtcs()
                                                        .dref(i))) {
                    return Optional.of(i);
                }
            }
        }

        return Optional.empty();
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
