package org.westmalle.wayland.dispmanx;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;
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
        nativewindow.element = dispmanxElement;
        nativewindow.width = width;
        nativewindow.height = height;
        nativewindow.write();

        final Pointer nativeDisplay = LibEGL.EGL_DEFAULT_DISPLAY;
        final Pointer display       = createEglDisplay(nativeDisplay);
        final Pointer config        = createDispmanxConfig(display);
        // create an EGL rendering context
        final Pointer context = getContext(display,
                                           config);
        final Pointer surface = createEglSurface(nativewindow.getPointer(),
                                                 display,
                                                 config,
                                                 context);

        return this.privateDispmanxEglOutputFactory.create(display,
                                                           surface,
                                                           context);
    }

    private Pointer createEglSurface(final Pointer nativewindow,
                                     final Pointer display,
                                     final Pointer config,
                                     final Pointer context) {
        // get an appropriate EGL frame buffer configuration
        if (!this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)) {
            this.libEGL.throwError("eglBindAPI");
        }

        final Pointer surface = this.libEGL.eglCreateWindowSurface(display,
                                                                   config,
                                                                   nativewindow,
                                                                   null);
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

    private Pointer getContext(final Pointer display,
                               final Pointer config) {
        final int[] context_attributes_values = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        final Pointer context_attributes = new Memory(Integer.BYTES * context_attributes_values.length);
        context_attributes.write(0,
                                 context_attributes_values,
                                 0,
                                 context_attributes_values.length);
        final Pointer context = this.libEGL.eglCreateContext(display,
                                                             config,
                                                             EGL_NO_CONTEXT,
                                                             context_attributes);
        if (context == EGL_NO_CONTEXT) {
            this.libEGL.throwError("eglCreateContext");
        }
        return context;
    }

    private Pointer createDispmanxConfig(final Pointer display) {
        final Pointer num_config = new Memory(Integer.BYTES);

        final int[] attribute_list_values =
                {
                        EGL_SURFACE_TYPE, EGL_WINDOW_BIT | EGL_SWAP_BEHAVIOR_PRESERVED_BIT,
                        EGL_RED_SIZE, 8,
                        EGL_GREEN_SIZE, 8,
                        EGL_BLUE_SIZE, 8,
                        EGL_ALPHA_SIZE, 8,
                        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL_NONE

                };
        final Pointer attribute_list = new Memory(Integer.BYTES * attribute_list_values.length);
        attribute_list.write(0,
                             attribute_list_values,
                             0,
                             attribute_list_values.length);

        final Pointer configs = new Memory(Pointer.SIZE);
        // get an appropriate EGL frame buffer configuration
        if (!this.libEGL.eglChooseConfig(display,
                                         attribute_list,
                                         configs,
                                         1,
                                         num_config)) {
            this.libEGL.throwError("eglChooseConfig");
        }

        return configs.getPointer(0);
    }

    private Pointer createEglDisplay(final Pointer nativeDisplay) {
        final Pointer display = this.libEGL.eglGetDisplay(nativeDisplay);
        if (display == EGL_NO_DISPLAY)

        {
            this.libEGL.throwError("eglGetDisplay");
        }

        // initialize the EGL display connection
        if (!this.libEGL.eglInitialize(display,
                                       null,
                                       null)) {
            this.libEGL.throwError("eglInitialize");
        }

        final String eglClientApis = this.libEGL.eglQueryString(display,
                                                                LibEGL.EGL_CLIENT_APIS)
                                                .getString(0);
        final String eglVendor = this.libEGL.eglQueryString(display,
                                                            LibEGL.EGL_VENDOR)
                                            .getString(0);
        final String eglVersion = this.libEGL.eglQueryString(display,
                                                             LibEGL.EGL_VERSION)
                                             .getString(0);

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
