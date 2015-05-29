package org.westmalle.wayland.x11;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import org.westmalle.wayland.nativ.Libegl;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.Libegl.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_BACK_BUFFER;
import static org.westmalle.wayland.nativ.Libegl.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_BUFFER_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_COLOR_BUFFER_TYPE;
import static org.westmalle.wayland.nativ.Libegl.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.Libegl.EGL_DEPTH_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_NONE;
import static org.westmalle.wayland.nativ.Libegl.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.Libegl.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.Libegl.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.Libegl.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.Libegl.EGL_PLATFORM_X11_KHR;
import static org.westmalle.wayland.nativ.Libegl.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.Libegl.EGL_RENDER_BUFFER;
import static org.westmalle.wayland.nativ.Libegl.EGL_RGB_BUFFER;
import static org.westmalle.wayland.nativ.Libegl.EGL_SAMPLES;
import static org.westmalle.wayland.nativ.Libegl.EGL_SAMPLE_BUFFERS;
import static org.westmalle.wayland.nativ.Libegl.EGL_STENCIL_SIZE;
import static org.westmalle.wayland.nativ.Libegl.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.Libegl.EGL_WINDOW_BIT;

public class XEglOutputFactory {

    @Nonnull
    private final Libegl libegl;

    @Inject
    XEglOutputFactory(@Nonnull final Libegl libegl) {
        this.libegl = libegl;
    }

    @Nonnull
    public XEglOutput create(@Nonnull final Pointer display,
                             final int window) {
        if (!this.libegl.eglBindAPI(EGL_OPENGL_ES_API)) {
            throw new RuntimeException("eglBindAPI failed");
        }
        final Pointer eglDisplay = createEglDisplay(display);
        final int configs_size = 256 * Pointer.SIZE;
        final Pointer configs = new Memory(configs_size);
        chooseConfig(eglDisplay,
                     configs,
                     configs_size);
        final Pointer config = configs.getPointer(0);
        final Pointer context = createEglContext(eglDisplay,
                                                 config);
        return new XEglOutput(this.libegl,
                              eglDisplay,
                              createEglSurface(eglDisplay,
                                               config,
                                               context,
                                               window));
    }

    private Pointer createEglDisplay(Pointer nativeDisplay) {
        final Pointer eglDisplay = this.libegl.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
                                                                        nativeDisplay,
                                                                        null);
        if (eglDisplay == EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (!this.libegl.eglInitialize(eglDisplay,
                                       null,
                                       null)) {
            throw new RuntimeException("eglInitialize() failed");
        }
        return eglDisplay;
    }

    private void chooseConfig(final Pointer eglDisplay,
                              final Pointer configs,
                              final int configs_size) {
        final Pointer num_configs = new Memory(4);
        final Pointer egl_config_attribs = createEglConfigAttribs();
        if (!this.libegl.eglChooseConfig(eglDisplay,
                                         egl_config_attribs,
                                         configs,
                                         configs_size,
                                         num_configs)) {
            throw new RuntimeException("eglChooseConfig() failed");
        }
        if (num_configs.getInt(0) == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }
    }

    private Pointer createEglConfigAttribs() {
        final Pointer egl_config_attribs = new Memory(Integer.BYTES * ((12 * 2) + 1));
        egl_config_attribs.setInt(0, EGL_COLOR_BUFFER_TYPE);
        egl_config_attribs.setInt(4, EGL_RGB_BUFFER);

        egl_config_attribs.setInt(8, EGL_BUFFER_SIZE);
        egl_config_attribs.setInt(12, 32);

        egl_config_attribs.setInt(16, EGL_RED_SIZE);
        egl_config_attribs.setInt(20, 8);

        egl_config_attribs.setInt(24, EGL_GREEN_SIZE);
        egl_config_attribs.setInt(28, 8);

        egl_config_attribs.setInt(32, EGL_BLUE_SIZE);
        egl_config_attribs.setInt(36, 8);

        egl_config_attribs.setInt(40, EGL_ALPHA_SIZE);
        egl_config_attribs.setInt(44, 8);

        egl_config_attribs.setInt(48, EGL_DEPTH_SIZE);
        egl_config_attribs.setInt(52, 24);

        egl_config_attribs.setInt(56, EGL_STENCIL_SIZE);
        egl_config_attribs.setInt(60, 8);

        egl_config_attribs.setInt(64, EGL_SAMPLE_BUFFERS);
        egl_config_attribs.setInt(68, 0);

        egl_config_attribs.setInt(72, EGL_SAMPLES);
        egl_config_attribs.setInt(76, 0);

        egl_config_attribs.setInt(80, EGL_SURFACE_TYPE);
        egl_config_attribs.setInt(84, EGL_WINDOW_BIT);

        egl_config_attribs.setInt(88, EGL_RENDERABLE_TYPE);
        egl_config_attribs.setInt(92, EGL_OPENGL_ES2_BIT);

        egl_config_attribs.setInt(96, EGL_NONE);

        return egl_config_attribs;
    }

    private Pointer createEglContext(final Pointer eglDisplay,
                                     final Pointer config) {
        final Pointer egl_context_attribs = createEglContextAttribs();
        final Pointer context = this.libegl.eglCreateContext(eglDisplay,
                                                             config,
                                                             EGL_NO_CONTEXT,
                                                             egl_context_attribs);
        if (context == null) {
            throw new RuntimeException("eglCreateContext() failed");
        }
        return context;
    }

    private Pointer createEglContextAttribs() {
        final Pointer egl_context_attribs = new Memory(Integer.BYTES * 3);
        egl_context_attribs.setInt(0, EGL_CONTEXT_CLIENT_VERSION);
        egl_context_attribs.setInt(4, 2);
        egl_context_attribs.setInt(8, EGL_NONE);
        return egl_context_attribs;
    }

    private Pointer createEglSurface(final Pointer eglDisplay,
                                     final Pointer config,
                                     final Pointer context,
                                     final int nativeWindow) {
        final Pointer egl_surface_attribs = createSurfaceAttribs();
        final Pointer eglSurface = this.libegl.eglCreatePlatformWindowSurfaceEXT(eglDisplay,
                                                                                 config,
                                                                                 Pointer.createConstant(nativeWindow),
                                                                                 egl_surface_attribs);
        if (eglSurface == null) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }
        if (!this.libegl.eglMakeCurrent(eglDisplay,
                                        eglSurface,
                                        eglSurface,
                                        context)) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }
        return eglSurface;
    }

    private Pointer createSurfaceAttribs() {
        final Pointer egl_surface_attribs = new Memory(3 * Integer.BYTES);
        egl_surface_attribs.setInt(0, EGL_RENDER_BUFFER);
        egl_surface_attribs.setInt(4, EGL_BACK_BUFFER);
        egl_surface_attribs.setInt(12, EGL_NONE);
        return egl_surface_attribs;
    }
}
