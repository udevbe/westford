package org.westmalle.wayland.bootstrap;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Size;
import org.westmalle.wayland.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westmalle.wayland.nativ.libEGL.EglGetPlatformDisplayEXT;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libEGL.LibEGL_Symbols;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2_Symbols;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libc.Libc_Symbols;
import org.westmalle.wayland.nativ.libdrm.DrmModeConnector;
import org.westmalle.wayland.nativ.libdrm.DrmModeEncoder;
import org.westmalle.wayland.nativ.libdrm.DrmModeModeInfo;
import org.westmalle.wayland.nativ.libdrm.DrmModeRes;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libdrm.Libdrm_Symbols;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.nativ.libgbm.Libgbm_Symbols;
import org.westmalle.wayland.nativ.libgbm.Pointerdestroy_user_data;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.nativ.libudev.Libudev_Symbols;

import javax.annotation.Nonnull;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Pointer.nref;
import static org.freedesktop.jaccall.Pointer.wrap;
import static org.freedesktop.jaccall.Size.sizeof;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BACK_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_PLATFORM_GBM_KHR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDER_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COLOR_BUFFER_BIT;
import static org.westmalle.wayland.nativ.libc.Libc.O_RDWR;
import static org.westmalle.wayland.nativ.libdrm.Libdrm.DRM_MODE_CONNECTED;
import static org.westmalle.wayland.nativ.libdrm.Libdrm.DRM_MODE_PAGE_FLIP_EVENT;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_RENDERING;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_SCANOUT;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_FORMAT_XRGB8888;

public class DrmTest {

    private final Libudev   libudev;
    private final Libc      libc;
    private final Libdrm    libdrm;
    private final Libgbm    libgbm;
    private final LibEGL    libEGL;
    private final LibGLESv2 libGLESv2;
    private final int       drmFd;

    public static void main(String[] args) throws InterruptedException {
        new DrmTest();
    }

    public DrmTest() throws InterruptedException {

        new Libudev_Symbols().link();
        this.libudev = new Libudev();
        new Libc_Symbols().link("libc.so.6");
        this.libc = new Libc();
        new Libdrm_Symbols().link();
        this.libdrm = new Libdrm();
        new Libgbm_Symbols().link();
        this.libgbm = new Libgbm();
        new LibEGL_Symbols().link();
        this.libEGL = new LibEGL();
        new LibGLESv2_Symbols().link();
        this.libGLESv2 = new LibGLESv2();

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

        this.drmFd = initDrm(drmDevice);

        final long resources = this.libdrm.drmModeGetResources(this.drmFd);
        if (resources == 0L) {
            throw new RuntimeException("Getting drm resources failed.");
        }
        final DrmModeRes drmModeRes = wrap(DrmModeRes.class,
                                           resources).dref();

        final int countConnectors = drmModeRes.count_connectors();

        DrmModeConnector drmModeConnector = null;
        for (int i = 0; i < countConnectors; i++) {
            final long connector = this.libdrm.drmModeGetConnector(this.drmFd,
                                                                   drmModeRes.connectors()
                                                                             .dref(i));
            if (connector == 0L) {
                continue;
            }

            drmModeConnector = wrap(DrmModeConnector.class,
                                    connector).dref();
            if (drmModeConnector.connection() == DRM_MODE_CONNECTED) {
                break;
            }
            else {
                this.libdrm.drmModeFreeConnector(connector);
            }
        }

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

        DrmModeEncoder drmModeEncoder = null;
        for (int j = 0; j < drmModeConnector.count_encoders(); j++) {
            final long encoder = this.libdrm.drmModeGetEncoder(this.drmFd,
                                                               drmModeConnector.encoders()
                                                                               .dref(j));
            drmModeEncoder = wrap(DrmModeEncoder.class,
                                  encoder).dref();
            if (drmModeEncoder.encoder_id() == drmModeConnector.encoder_id()) {
                break;
            }
            else {
                this.libdrm.drmModeFreeEncoder(encoder);
            }
        }


        final long gbmDevice  = this.libgbm.gbm_create_device(this.drmFd);
        final long eglDisplay = createEglDisplay(gbmDevice);

        final String eglExtensions = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(eglDisplay,
                                                                             EGL_EXTENSIONS))
                                            .dref();

        final long eglConfig = eglConfig(eglDisplay,
                                         eglExtensions);
        final long eglContext = createEglContext(eglDisplay,
                                                 eglConfig);

        final long gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                               mode.hdisplay(),
                                                               mode.vdisplay(),
                                                               GBM_FORMAT_XRGB8888,
                                                               GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING);

        if (gbmSurface == 0) {
            throw new RuntimeException("failed to create gbm surface");
        }

        final long eglSurface = createEglSurface(eglDisplay,
                                                 eglConfig,
                                                 gbmSurface);

        this.libEGL.eglMakeCurrent(eglDisplay,
                                   eglSurface,
                                   eglSurface,
                                   eglContext);
        this.libGLESv2.glClearColor(0.5f,
                                    0.5f,
                                    0.5f,
                                    0.5f);
        this.libGLESv2.glClear(GL_COLOR_BUFFER_BIT);
        this.libEGL.eglSwapBuffers(eglDisplay,
                                   eglSurface);
        long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface);
        int  fbId  = getFbId(gbmBo);
        final int error = this.libdrm.drmModeSetCrtc(this.drmFd,
                                                     drmModeEncoder.crtc_id(),
                                                     fbId,
                                                     0,
                                                     0,
                                                     Pointer.nref(drmModeConnector.connector_id()).address,
                                                     1,
                                                     Pointer.ref(mode).address);

        while (true) {
            int waiting_for_flip = 1;

            draw();

            this.libEGL.eglSwapBuffers(eglDisplay,
                                       eglSurface);
            long next_bo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface);
            fbId = getFbId(next_bo);

            int ret = this.libdrm.drmModePageFlip(this.drmFd,
                                                  drmModeEncoder.crtc_id(),
                                                  fbId,
                                                  DRM_MODE_PAGE_FLIP_EVENT,
                                                  0L);
            if (ret != 0) {
                throw new RuntimeException(String.format("failed to queue page flip: %d\n",
                                                         this.libc.getErrno()));
            }

            //fugly, normally we listen for fd events,  now we just draw at 10fps and assume the pageflip occurred.
            Thread.sleep(100);

//            this.libdrm.drmHandleEvent(this.drmFd,
//                                       0L);

		/* release last buffer to render on again: */
            this.libgbm.gbm_surface_release_buffer(gbmSurface,
                                                   gbmBo);
            gbmBo = next_bo;
        }
    }

    private void draw() {
        //TODO
    }

    public int getFbId(final long gbmBo) {
        final long fbIdP = this.libgbm.gbm_bo_get_user_data(gbmBo);
        if (fbIdP != 0L) {
            return Pointer.wrap(Integer.class,
                                fbIdP)
                          .dref();
        }

        final Pointer<Integer> fb = Pointer.calloc(1,
                                                   Size.sizeof((Integer) null),
                                                   Integer.class);
        final int width  = this.libgbm.gbm_bo_get_width(gbmBo);
        final int height = this.libgbm.gbm_bo_get_height(gbmBo);
        final int stride = this.libgbm.gbm_bo_get_stride(gbmBo);
        final int handle = (int) this.libgbm.gbm_bo_get_handle(gbmBo);
        final int ret = this.libdrm.drmModeAddFB(this.drmFd,
                                                 width,
                                                 height,
                                                 (byte) 24,
                                                 (byte) 32,
                                                 stride,
                                                 handle,
                                                 fb.address);
        if (ret != 0) {
            throw new RuntimeException("failed to create fb");
        }


        this.libgbm.gbm_bo_set_user_data(gbmBo,
                                         fb.address,
                                         Pointerdestroy_user_data.nref(this::destroyUserData).address);

        return fb.dref();
    }

    private void destroyUserData(@Ptr final long bo,
                                 @Ptr final long data) {
        final Pointer<Integer> fbIdP = Pointer.wrap(Integer.class,
                                                    data);
        final Integer fbId = fbIdP.dref();
        this.libdrm.drmModeRmFB(this.drmFd,
                                fbId);
        fbIdP.close();
    }

    public long createEglSurface(final long eglDisplay,
                                 final long config,
                                 final long gbmSurface) {
        final Pointer<Integer> eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                                                EGL_BACK_BUFFER,
                                                                EGL_NONE);

        final Pointer<EglCreatePlatformWindowSurfaceEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglCreatePlatformWindowSurfaceEXT.class,
                                                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address));
        final long eglSurface = eglGetPlatformDisplayEXT.dref()
                                                        .$(eglDisplay,
                                                           config,
                                                           gbmSurface,
                                                           eglSurfaceAttribs.address);
        if (eglSurface == 0L) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }

        return eglSurface;
    }

    private long createEglContext(final long eglDisplay,
                                  final long config) {
        final Pointer<?> eglContextAttribs = Pointer.nref(
                //@formatter:off
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
                //@formatter:on
                                                         );
        final long context = this.libEGL.eglCreateContext(eglDisplay,
                                                          config,
                                                          EGL_NO_CONTEXT,
                                                          eglContextAttribs.address);
        if (context == 0L) {
            throw new RuntimeException("eglCreateContext() failed");
        }
        return context;
    }

    public long eglConfig(final long eglDisplay,
                          @Nonnull final String eglExtensions) {
        assert (eglDisplay != EGL_NO_DISPLAY);

        if (this.libEGL.eglBindAPI(EGL_OPENGL_ES_API) == 0L) {
            throw new RuntimeException("eglBindAPI failed");
        }

        final int configs_size = 256 * sizeof((Pointer<?>) null);
        final Pointer<Pointer> configs = malloc(configs_size,
                                                Pointer.class);
        final Pointer<Integer> num_configs = Pointer.nref(0);
        final Pointer<Integer> egl_config_attribs = Pointer.nref(
                //@formatter:off
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
	            EGL_RED_SIZE, 1,
	            EGL_GREEN_SIZE, 1,
	            EGL_BLUE_SIZE, 1,
	            EGL_ALPHA_SIZE, 0,
	            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
	            EGL_NONE
                //@formatter:on
                                                                );
        if (this.libEGL.eglChooseConfig(eglDisplay,
                                        egl_config_attribs.address,
                                        configs.address,
                                        configs_size,
                                        num_configs.address) == 0) {
            throw new RuntimeException("eglChooseConfig() failed");
        }
        if (num_configs.dref() == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }

        return configs.dref().address;
    }

    private long createEglDisplay(final long gbmDevice) {

        final Pointer<String> noDisplayExtensions = Pointer.wrap(String.class,
                                                                 this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                            EGL_EXTENSIONS));
        if (noDisplayExtensions.address == 0L) {
            throw new RuntimeException("Could not query egl extensions.");
        }
        final String extensions = noDisplayExtensions.dref();

        if (!extensions.contains("EGL_MESA_platform_gbm")) {
            throw new RuntimeException("Required extension EGL_MESA_platform_gbm not available.");
        }

        final Pointer<EglGetPlatformDisplayEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglGetPlatformDisplayEXT.class,
                                                                                        this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address));

        final long eglDisplay = eglGetPlatformDisplayEXT.dref()
                                                        .$(EGL_PLATFORM_GBM_KHR,
                                                           gbmDevice,
                                                           0L);
        if (eglDisplay == 0L) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (this.libEGL.eglInitialize(eglDisplay,
                                      0L,
                                      0L) == 0) {
            throw new RuntimeException("eglInitialize() failed");
        }

        return eglDisplay;
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
