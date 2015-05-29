package org.westmalle.wayland.x11;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import org.westmalle.wayland.nativ.Libegl;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class XEglOutputFactory {

    @Nonnull
    private final Libegl libegl;

    @Inject
    XEglOutputFactory(@Nonnull final Libegl libegl) {
        this.libegl = libegl;
    }

    public XEglOutput create(final Pointer display,
                             final int window) {

        Pointer eglDisplay = this.libegl.eglGetPlatformDisplayEXT(Libegl.EGL_PLATFORM_X11_KHR,
                                                                  display,
                                                                  null);
        if (eglDisplay == Libegl.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay() failed");
        }

        boolean ok = this.libegl.eglInitialize(eglDisplay,
                                               null,
                                               null);
        if (!ok) {
            throw new RuntimeException("eglInitialize() failed");
        }

        int configs_size = 256;
        Pointer configs = new Memory(configs_size);
        ByteBuffer.allocateDirect(4).asIntBuffer();
        Pointer num_configs = new Memory(4);

        Pointer egl_config_attribs = new Memory(4 * (12 * 2 + 1));
        egl_config_attribs.setInt(0, Libegl.EGL_COLOR_BUFFER_TYPE);
        egl_config_attribs.setInt(4, Libegl.EGL_RGB_BUFFER);

        egl_config_attribs.setInt(8, Libegl.EGL_BUFFER_SIZE);
        egl_config_attribs.setInt(12, 32);

        egl_config_attribs.setInt(16, Libegl.EGL_RED_SIZE);
        egl_config_attribs.setInt(20, 8);

        egl_config_attribs.setInt(24, Libegl.EGL_GREEN_SIZE);
        egl_config_attribs.setInt(28, 8);

        egl_config_attribs.setInt(32, Libegl.EGL_BLUE_SIZE);
        egl_config_attribs.setInt(36, 8);

        egl_config_attribs.setInt(40, Libegl.EGL_ALPHA_SIZE);
        egl_config_attribs.setInt(44, 8);

        egl_config_attribs.setInt(0, Libegl.EGL_DEPTH_SIZE);
        egl_config_attribs.setInt(0, 24);

        egl_config_attribs.setInt(0, Libegl.EGL_STENCIL_SIZE);
        egl_config_attribs.setInt(0, 8);

        egl_config_attribs.setInt(0, Libegl.EGL_SAMPLE_BUFFERS);
        egl_config_attribs.setInt(0, 0);

        egl_config_attribs.setInt(0, Libegl.EGL_SAMPLES);
        egl_config_attribs.setInt(0, 0);

        egl_config_attribs.setInt(0, Libegl.EGL_SURFACE_TYPE);
        egl_config_attribs.setInt(0, Libegl.EGL_WINDOW_BIT);

        egl_config_attribs.setInt(0, Libegl.EGL_RENDERABLE_TYPE);
        egl_config_attribs.setInt(0, Libegl.EGL_OPENGL_ES2_BIT);

        egl_config_attribs.setInt(0, Libegl.EGL_NONE);

        ok = this.libegl.eglChooseConfig(
                eglDisplay,
                egl_config_attribs,
                configs,
                configs_size, // num requested configs
                num_configs); // num returned configs
        if (!ok) {
            throw new RuntimeException("eglChooseConfig() failed");
        }
        if (num_configs.getInt(0) == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }
        Pointer config = new Memory(Pointer.SIZE);

        Pointer egl_context_attribs = new Memory(4 * 3);
        egl_context_attribs.setInt(0, Libegl.EGL_CONTEXT_CLIENT_VERSION);
        egl_context_attribs.setInt(4, 2);
        egl_context_attribs.setInt(8, Libegl.EGL_NONE);
        Pointer context = this.libegl.eglCreateContext(
                eglDisplay,
                config,
                Libegl.EGL_NO_CONTEXT,
                egl_context_attribs);
        if (context == null) {
            throw new RuntimeException("eglCreateContext() failed");
        }

        Pointer egl_surface_attribs = new Memory(3 * 4);
        egl_surface_attribs.setInt(0, Libegl.EGL_RENDER_BUFFER);
        egl_surface_attribs.setInt(4, Libegl.EGL_BACK_BUFFER);
        egl_surface_attribs.setInt(12, Libegl.EGL_NONE);

        Pointer eglSurface = this.libegl.eglCreatePlatformWindowSurfaceEXT(
                eglDisplay,
                config,
                Pointer.createConstant(window),
                egl_surface_attribs);
        if (eglSurface == null) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }

        ok = this.libegl.eglMakeCurrent(eglDisplay,
                                        eglSurface,
                                        eglSurface,
                                        context);
        if (!ok) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }

        // Check if eglSurface is double buffered.
        Pointer render_buffer = new Memory(4);
        ok = this.libegl.eglQueryContext(
                eglDisplay,
                context,
                Libegl.EGL_RENDER_BUFFER,
                render_buffer);
        if (!ok) {
            throw new RuntimeException("eglQueyContext(EGL_RENDER_BUFFER) failed");
        }
        if (render_buffer.getInt(0) == Libegl.EGL_SINGLE_BUFFER) {
            System.err.println("warn: EGL eglSurface is single buffered");
        }

        return new XEglOutput(this.libegl,
                              eglDisplay,
                              eglSurface);
    }
}
