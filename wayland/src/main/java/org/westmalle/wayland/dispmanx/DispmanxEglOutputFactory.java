package org.westmalle.wayland.dispmanx;

import com.github.zubnix.jaccall.Pointer;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
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

public class DispmanxEglOutputFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final LibEGL                          libEGL;
    @Nonnull
    private final PrivateDispmanxEglOutputFactory privateDispmanxEglOutputFactory;

    @Inject
    DispmanxEglOutputFactory(@Nonnull final LibEGL libEGL,
                             @Nonnull final PrivateDispmanxEglOutputFactory privateDispmanxEglOutputFactory) {
        this.libEGL = libEGL;
        this.privateDispmanxEglOutputFactory = privateDispmanxEglOutputFactory;
    }

    public DispmanxEglOutput create(final int dispmanxElement,
                                    final int width,
                                    final int height) {
        final EGL_DISPMANX_WINDOW_T nativewindow = new EGL_DISPMANX_WINDOW_T();
        nativewindow.element(dispmanxElement);
        nativewindow.width(width);
        nativewindow.height(height);

        final long nativeDisplay = LibEGL.EGL_DEFAULT_DISPLAY;
        final long display       = createEglDisplay(nativeDisplay);
        final long config        = createDispmanxConfig(display);
        // create an EGL rendering context
        final long context = getContext(display,
                                        config);
        final long surface = createEglSurface(Pointer.ref(nativewindow).address,
                                              display,
                                              config,
                                              context);

        return this.privateDispmanxEglOutputFactory.create(display,
                                                           surface,
                                                           context);
    }

    private long createEglSurface(final long nativewindow,
                                  final long display,
                                  final long config,
                                  final long context) {
        // get an appropriate EGL frame buffer configuration
        if (!this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)) {
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
        if (!this.libEGL.eglMakeCurrent(display,
                                        surface,
                                        surface,
                                        context)) {
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

    private long createDispmanxConfig(final long display) {
        final Pointer<Integer> num_config = Pointer.nref(0);
        final Pointer<Pointer> configs    = Pointer.nref(Pointer.nref(0));
        // get an appropriate EGL frame buffer configuration
        if (!this.libEGL.eglChooseConfig(display,
                                         Pointer.nref(EGL_SURFACE_TYPE,
                                                      EGL_WINDOW_BIT | EGL_SWAP_BEHAVIOR_PRESERVED_BIT,
                                                      EGL_RED_SIZE,
                                                      8,
                                                      EGL_GREEN_SIZE,
                                                      8,
                                                      EGL_BLUE_SIZE,
                                                      8,
                                                      EGL_ALPHA_SIZE,
                                                      8,
                                                      EGL_RENDERABLE_TYPE,
                                                      EGL_OPENGL_ES2_BIT,
                                                      EGL_NONE).address,
                                         configs.address,
                                         1,
                                         num_config.address)) {
            this.libEGL.throwError("eglChooseConfig");
        }

        return configs.dref().address;
    }

    private long createEglDisplay(final long nativeDisplay) {
        final long display = this.libEGL.eglGetDisplay(nativeDisplay);
        if (display == EGL_NO_DISPLAY) {
            this.libEGL.throwError("eglGetDisplay");
        }

        // initialize the EGL display connection
        if (!this.libEGL.eglInitialize(display,
                                       0L,
                                       0L)) {
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
