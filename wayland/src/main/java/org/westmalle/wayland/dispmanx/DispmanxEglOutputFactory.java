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
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BUFFER_PRESERVED;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_DEFAULT_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SWAP_BEHAVIOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SWAP_BEHAVIOR_PRESERVED_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;
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

        final Pointer eglDisplay = createEglDisplay();
        final Pointer eglContext = createEglContext(eglDisplay,
                                                    createEglConfigAttribs());
        final Pointer eglSurface = createEglSurface(eglDisplay,
                                                    eglContext,
                                                    nativewindow);

        return this.privateDispmanxEglOutputFactory.create(eglDisplay,
                                                           eglSurface,
                                                           eglContext);
    }

    private Pointer createEglSurface(final Pointer eglDisplay,
                                     final Pointer config,
                                     final EGL_DISPMANX_WINDOW_T nativeWindow) {

        final Pointer eglSurface = this.libEGL.eglCreateWindowSurface(eglDisplay,
                                                                      config,
                                                                      nativeWindow.getPointer(),
                                                                      null);
        if (eglSurface == null) {
            this.libEGL.throwError("eglCreateWindowSurface()");
        }

        if (!this.libEGL.eglSurfaceAttrib(eglDisplay,
                                          eglSurface,
                                          EGL_SWAP_BEHAVIOR,
                                          EGL_BUFFER_PRESERVED)) {
            this.libEGL.throwError("eglSurfaceAttrib()");
        }

        final Pointer context = createEglContext(eglDisplay,
                                                 config);

        if (!this.libEGL.eglMakeCurrent(eglDisplay,
                                        eglSurface,
                                        eglSurface,
                                        context)) {
            this.libEGL.throwError("eglMakeCurrent()");
        }

        return eglSurface;
    }

    private Pointer createEglContextAttribs() {
        final int[] contextAttributes = {
                //@formatter:off
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
                //@formatter:on
        };
        final Pointer eglContextAttribs = new Memory(Integer.BYTES * contextAttributes.length);
        eglContextAttribs.write(0,
                                contextAttributes,
                                0,
                                contextAttributes.length);
        return eglContextAttribs;
    }

    private Pointer createEglContext(final Pointer eglDisplay,
                                     final Pointer config) {

        if (!this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)) {
            this.libEGL.throwError("eglBindAPI()");
        }

        final Pointer eglContextAttribs = createEglContextAttribs();
        final Pointer context = this.libEGL.eglCreateContext(eglDisplay,
                                                             config,
                                                             EGL_NO_CONTEXT,
                                                             eglContextAttribs);
        if (context == null) {
            this.libEGL.throwError("eglCreateContext()");
        }
        return context;
    }

    private Pointer createEglConfigAttribs() {
        final int[] attributes = {
                //@formatter:off
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT | EGL_SWAP_BEHAVIOR_PRESERVED_BIT,
		        EGL_RED_SIZE, 1,
		        EGL_GREEN_SIZE, 1,
		        EGL_BLUE_SIZE, 1,
		        EGL_ALPHA_SIZE, 0,
		        EGL_RENDERABLE_TYPE,  EGL_OPENGL_ES2_BIT,
		        EGL_NONE
                //@formatter:on
        };
        final Pointer configAttribs = new Memory(Integer.BYTES * attributes.length);
        configAttribs.write(0,
                            attributes,
                            0,
                            attributes.length);
        return configAttribs;
    }

    private Pointer chooseConfig(final Pointer eglDisplay) {
        final int     configs_size = 1;
        final Pointer configs      = new Memory(configs_size * Pointer.SIZE);

        final Pointer num_configs        = new Memory(Integer.BYTES);
        final Pointer egl_config_attribs = createEglConfigAttribs();
        if (!this.libEGL.eglChooseConfig(eglDisplay,
                                         egl_config_attribs,
                                         configs,
                                         configs_size,
                                         num_configs)) {
            this.libEGL.throwError("eglChooseConfig()");
        }
        if (num_configs.getInt(0) == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }

        return configs.getPointer(0);
    }


    private Pointer createEglDisplay() {

        final Pointer eglDisplay = this.libEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);

        if (eglDisplay == null || eglDisplay.equals(EGL_NO_DISPLAY)) {
            this.libEGL.throwError("eglGetDisplay()");
        }

        if (!this.libEGL.eglInitialize(eglDisplay,
                                       null,
                                       null)) {
            this.libEGL.throwError("eglInitialize()");
        }

        final String eglClientApis = this.libEGL.eglQueryString(eglDisplay,
                                                                EGL_CLIENT_APIS)
                                                .getString(0);
        final String eglVendor = this.libEGL.eglQueryString(eglDisplay,
                                                            EGL_VENDOR)
                                            .getString(0);
        final String eglVersion = this.libEGL.eglQueryString(eglDisplay,
                                                             EGL_VERSION)
                                             .getString(0);

        LOGGER.info(format("Creating Dispmanx EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n",
                           eglClientApis,
                           eglVendor,
                           eglVersion));

        return eglDisplay;
    }
}
