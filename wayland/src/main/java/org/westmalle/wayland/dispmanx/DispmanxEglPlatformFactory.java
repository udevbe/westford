package org.westmalle.wayland.dispmanx;

import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.GlRenderer;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_SURFACE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SWAP_BEHAVIOR_PRESERVED_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;
import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

public class DispmanxEglPlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final LibEGL                            libEGL;
    @Nonnull
    private final PrivateDispmanxEglPlatformFactory privateDispmanxEglOutputFactory;
    @Nonnull
    private final DispmanxPlatform                  dispmanxPlatform;
    @Nonnull
    private final GlRenderer                        glRenderer;

    @Inject
    DispmanxEglPlatformFactory(@Nonnull final LibEGL libEGL,
                               @Nonnull final PrivateDispmanxEglPlatformFactory privateDispmanxEglOutputFactory,
                               @Nonnull final DispmanxPlatform dispmanxPlatform,
                               @Nonnull final GlRenderer glRenderer) {
        this.libEGL = libEGL;
        this.privateDispmanxEglOutputFactory = privateDispmanxEglOutputFactory;
        this.dispmanxPlatform = dispmanxPlatform;
        this.glRenderer = glRenderer;
    }

    public DispmanxEglPlatform create() {

        final DISPMANX_MODEINFO_T modeinfo        = dispmanxPlatform.getModeinfo();
        final int                 dispmanxElement = dispmanxPlatform.getDispmanxElement();

        final EGL_DISPMANX_WINDOW_T nativewindow = new EGL_DISPMANX_WINDOW_T();
        nativewindow.element(dispmanxElement);
        nativewindow.width(modeinfo.width());
        nativewindow.height(modeinfo.height());

        final long nativeDisplay = LibEGL.EGL_DEFAULT_DISPLAY;
        final long eglDisplay    = createEglDisplay(nativeDisplay);
        final long config        = this.glRenderer.eglConfig(eglDisplay);
        // create an EGL rendering eglContext
        final long eglContext = getContext(eglDisplay,
                                           config);
        final long eglSurface = createEglSurface(Pointer.ref(nativewindow).address,
                                                 eglDisplay,
                                                 config,
                                                 eglContext);

        return this.privateDispmanxEglOutputFactory.create(dispmanxPlatform,
                                                           eglDisplay,
                                                           eglSurface,
                                                           eglContext);
    }

    private long createEglSurface(final long nativewindow,
                                  final long display,
                                  final long config,
                                  final long context) {
        // get an appropriate EGL frame buffer configuration
        if (this.libEGL.eglBindAPI(EGL_OPENGL_ES_API) != 0) {
            this.libEGL.throwError("eglBindAPI");
        }

        final long surface = this.libEGL.eglCreateWindowSurface(display,
                                                                config,
                                                                nativewindow,
                                                                0L);
        if (surface == EGL_NO_SURFACE) {
            this.libEGL.throwError("eglCreateWindowSurface");
        }

        // connect the context to the surface
        if (this.libEGL.eglMakeCurrent(display,
                                       surface,
                                       surface,
                                       context) != 0) {
            this.libEGL.throwError("eglMakeCurrent");
        }

        return surface;
    }

    private long getContext(final long display,
                            final long config) {
        final long context = this.libEGL.eglCreateContext(display,
                                                          config,
                                                          EGL_NO_CONTEXT,
                                                          Pointer.nref(EGL_CONTEXT_CLIENT_VERSION,
                                                                       2,
                                                                       EGL_NONE).address);
        if (context == EGL_NO_CONTEXT) {
            this.libEGL.throwError("eglCreateContext");
        }
        return context;
    }

    private long createEglDisplay(final long nativeDisplay) {
        final long display = this.libEGL.eglGetDisplay(nativeDisplay);
        if (display == EGL_NO_DISPLAY) {
            this.libEGL.throwError("eglGetDisplay");
        }

        // initialize the EGL display connection
        if (this.libEGL.eglInitialize(display,
                                      0L,
                                      0L) != 0) {
            this.libEGL.throwError("eglInitialize");
        }

        final String eglClientApis = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(display,
                                                                             LibEGL.EGL_CLIENT_APIS))
                                            .dref();
        final String eglVendor = Pointer.wrap(String.class,
                                              this.libEGL.eglQueryString(display,
                                                                         LibEGL.EGL_VENDOR))
                                        .dref();
        final String eglVersion = Pointer.wrap(String.class,
                                               this.libEGL.eglQueryString(display,
                                                                          LibEGL.EGL_VERSION))
                                         .dref();
        LOGGER.info(format("Creating X11 EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n",
                           eglClientApis,
                           eglVendor,
                           eglVersion));

        return display;
    }
}
