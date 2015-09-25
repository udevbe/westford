//Copyright 2015 Erik De Rijcke
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
package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.sun.jna.Pointer;
import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.nativ.libEGL.LibEGL;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_ACCESS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_ALLOC;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_CURRENT_SURFACE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_MATCH;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_NATIVE_PIXMAP;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_NATIVE_WINDOW;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BAD_SURFACE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_LOST;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NOT_INITIALIZED;

@AutoFactory(className = "PrivateX11EglOutputFactory",
             allowSubclasses = true)
public class X11EglOutput implements EglOutput {

    @Nonnull
    private final LibEGL  libEGL;
    @Nonnull
    private final Pointer eglDisplay;
    @Nonnull
    private final Pointer eglSurface;
    @Nonnull
    private final Pointer eglContext;

    private boolean eglMakeCurrentSuccess;

    X11EglOutput(@Provided @Nonnull final LibEGL libEGL,
                 @Nonnull final Pointer eglDisplay,
                 @Nonnull final Pointer eglSurface,
                 @Nonnull final Pointer eglContext) {
        this.libEGL = libEGL;
        this.eglDisplay = eglDisplay;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
    }

    @Override
    public void begin() {
        if (!this.eglMakeCurrentSuccess) {
            //context is not current, make it so. (we assume no other threads exist).
            this.eglMakeCurrentSuccess = this.libEGL.eglMakeCurrent(this.eglDisplay,
                                                                    this.eglSurface,
                                                                    this.eglSurface,
                                                                    this.eglContext);
            checkError();
        }
    }

    private void checkError() {
        if (this.eglMakeCurrentSuccess) {
            return;
        }

        //looks like there was an error trying to make the egl context current.
        final int eglError = this.libEGL.eglGetError();
        switch (eglError) {
            case EGL_BAD_DISPLAY: {
                throw new RuntimeException("Egl make current failed - display is not an EGL display connection.");
            }
            case EGL_NOT_INITIALIZED: {
                throw new RuntimeException("Egl make current failed - display has not been initialized.");
            }
            case EGL_BAD_SURFACE: {
                throw new RuntimeException("Egl make current failed - draw or read is not an EGL surface.");
            }
            case EGL_BAD_CONTEXT: {
                throw new RuntimeException("Egl make current failed - context is not an EGL rendering context.");
            }
            case EGL_BAD_MATCH: {
                throw new RuntimeException("Egl make current failed - draw or read are not compatible with context, or " +
                                           "context is set to EGL_NO_CONTEXT and draw or read are not set to " +
                                           "EGL_NO_SURFACE, or draw or read are set to EGL_NO_SURFACE and context is " +
                                           "not set to EGL_NO_CONTEXT.");
            }
            case EGL_BAD_ACCESS: {
                throw new RuntimeException("Egl make current failed - context is current to some other thread.");
            }
            case EGL_BAD_NATIVE_PIXMAP: {
                throw new RuntimeException("Egl make current failed - a native pixmap underlying either draw or read is " +
                                           "no longer valid.");
            }
            case EGL_BAD_NATIVE_WINDOW: {
                throw new RuntimeException("Egl make current failed - a native window underlying either draw or read is " +
                                           "no longer valid.");
            }
            case EGL_BAD_CURRENT_SURFACE: {
                throw new RuntimeException("Egl make current failed - the previous context has unflushed commands and " +
                                           "the previous surface is no longer valid.");
            }
            case EGL_BAD_ALLOC: {
                throw new RuntimeException("Egl make current failed - allocation of ancillary buffers for draw or read " +
                                           "were delayed until eglMakeCurrent is called, and there are not enough " +
                                           "resources to allocate them.");
            }
            case EGL_CONTEXT_LOST: {
                throw new RuntimeException("Egl make current failed - a power management event has occurred. The " +
                                           "application must destroy all contexts and reinitialise OpenGL ES state and " +
                                           "objects to continue rendering.");
            }
        }
    }

    @Override
    public void end() {
        this.libEGL.eglSwapBuffers(this.eglDisplay,
                                   this.eglSurface);
    }
}
