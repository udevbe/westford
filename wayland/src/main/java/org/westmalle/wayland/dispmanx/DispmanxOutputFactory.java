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
package org.westmalle.wayland.dispmanx;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.nativ.libbcm_host.VC_RECT_T;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
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
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SWAP_BEHAVIOR_PRESERVED_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;
import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_PROTECTION_NONE;

//TODO unit test
public class DispmanxOutputFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final Libbcm_host                     libbcm_host;
    @Nonnull
    private final LibEGL                          libEGL;
    @Nonnull
    private final WlOutputFactory                 wlOutputFactory;
    @Nonnull
    private final OutputFactory                   outputFactory;
    @Nonnull
    private final PrivateDispmanxOutputFactory    privateDispmanxOutputFactory;
    @Nonnull
    private final PrivateDispmanxEglOutputFactory privateDispmanxEglOutputFactory;
    @Nonnull
    private final WlCompositor                    wlCompositor;

    @Inject
    DispmanxOutputFactory(@Nonnull final Libbcm_host libbcm_host,
                          @Nonnull final LibEGL libEGL,
                          @Nonnull final WlOutputFactory wlOutputFactory,
                          @Nonnull final OutputFactory outputFactory,
                          @Nonnull final PrivateDispmanxOutputFactory privateDispmanxOutputFactory,
                          @Nonnull final PrivateDispmanxEglOutputFactory privateDispmanxEglOutputFactory,
                          @Nonnull final WlCompositor wlCompositor) {
        this.libbcm_host = libbcm_host;
        this.libEGL = libEGL;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.privateDispmanxOutputFactory = privateDispmanxOutputFactory;
        this.privateDispmanxEglOutputFactory = privateDispmanxEglOutputFactory;
        this.wlCompositor = wlCompositor;
    }

    public WlOutput create(final int device) {
        final WlOutput wlOutput = createDispmanXPlatformOutput(device);
        this.wlCompositor.getCompositor()
                         .getWlOutputs()
                         .addLast(wlOutput);

        return wlOutput;
    }

    private WlOutput createDispmanXPlatformOutput(final int device) {

        //setup egl display
        final Pointer eglDisplay   = createEglDisplay();
        final int     configs_size = 1;
        final Pointer configs      = new Memory(configs_size * Pointer.SIZE);
        chooseConfig(eglDisplay,
                     configs,
                     configs_size);
        final Pointer config = configs.getPointer(0);

        //setup dispmanx
        final int displayHandle = this.libbcm_host.vc_dispmanx_display_open(device);
        if (displayHandle == 0) {
            throw new RuntimeException("Failed to open dispmanx display for device " + device);
        }
        DISPMANX_MODEINFO_T dispmanx_modeinfo = new DISPMANX_MODEINFO_T();
        final int success = this.libbcm_host.vc_dispmanx_display_get_info(displayHandle,
                                                                          dispmanx_modeinfo);
        if (success < 0) {
            throw new RuntimeException("Failed get info for display=" + device);
        }
        final int width  = dispmanx_modeinfo.width;
        final int height = dispmanx_modeinfo.height;
        final EGL_DISPMANX_WINDOW_T dispmanxWindow = createDispmanxWindow(device,
                                                                          displayHandle,
                                                                          width,
                                                                          height);

        //setup egl surface
        final Pointer context = createEglContext(eglDisplay,
                                                 config);
        final Pointer eglSurface = createEglSurface(eglDisplay,
                                                    config,
                                                    context,
                                                    dispmanxWindow);


        final DispmanxEglOutput dispmanxEglOutput = this.privateDispmanxEglOutputFactory.create(eglDisplay,
                                                                                                eglSurface,
                                                                                                context);
        final DispmanxOutput dispmanxOutput = this.privateDispmanxOutputFactory.create(dispmanxEglOutput);
        final Output output = createOutput(dispmanxOutput,
                                           width,
                                           height);
        return this.wlOutputFactory.create(output);
    }

    private Pointer createEglSurface(final Pointer eglDisplay,
                                     final Pointer config,
                                     final Pointer context,
                                     final EGL_DISPMANX_WINDOW_T nativeWindow) {
        final Pointer eglSurface = libEGL.eglCreateWindowSurface(eglDisplay,
                                                                 config,
                                                                 nativeWindow.getPointer(),
                                                                 null);
        if (eglSurface == null) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }
        if (!this.libEGL.eglMakeCurrent(eglDisplay,
                                        eglSurface,
                                        eglSurface,
                                        context)) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }

        this.libEGL.eglBindAPI(EGL_OPENGL_ES_API);

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
        final Pointer eglContextAttribs = createEglContextAttribs();
        final Pointer context = this.libEGL.eglCreateContext(eglDisplay,
                                                             config,
                                                             EGL_NO_CONTEXT,
                                                             eglContextAttribs);
        if (context == null) {
            throw new RuntimeException("eglCreateContext() failed");
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

    private void chooseConfig(final Pointer eglDisplay,
                              final Pointer configs,
                              final int configs_size) {
        final Pointer num_configs        = new Memory(Integer.BYTES);
        final Pointer egl_config_attribs = createEglConfigAttribs();
        if (!this.libEGL.eglChooseConfig(eglDisplay,
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


    private Pointer createEglDisplay() {

        Pointer eglDisplay = this.libEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);

        if (eglDisplay == null || eglDisplay.equals(EGL_NO_DISPLAY)) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (!this.libEGL.eglInitialize(eglDisplay,
                                       null,
                                       null)) {
            throw new RuntimeException("eglInitialize() failed");
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

    private Output createOutput(final DispmanxOutput dispmanxOutput,
                                final int width,
                                final int height) {
        //TODO this is all guessing. Does dispmanx expose actual values?
        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .x(0)
                                                            .y(0)
                                                            .subpixel(0)
                                                            .make("Westmalle bcm_host")
                                                            .model("dispmanx")
                                                            .physicalWidth(0)
                                                            .physicalHeight(0)
                                                            .transform(WlOutputTransform.NORMAL.getValue())
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .flags(0)
                                                .height(height)
                                                .width(width)
                                                .refresh(60)
                                                .build();
        return this.outputFactory.create(outputGeometry,
                                         outputMode,
                                         dispmanxOutput);
    }

    private EGL_DISPMANX_WINDOW_T createDispmanxWindow(final int device,
                                                       final int displayHandle,
                                                       final int width,
                                                       final int height) {

        final VC_RECT_T dst_rect = new VC_RECT_T();
        final VC_RECT_T src_rect = new VC_RECT_T();

        dst_rect.x = 0;
        dst_rect.y = 0;
        dst_rect.width = width;
        dst_rect.height = height;
        dst_rect.write();

        src_rect.x = 0;
        src_rect.y = 0;
        src_rect.width = width << 16;
        src_rect.height = height << 16;
        src_rect.write();

        final int dispman_update = this.libbcm_host.vc_dispmanx_update_start(device);

        final int dispman_element = this.libbcm_host.vc_dispmanx_element_add(dispman_update,
                                                                             displayHandle,
                                                                             0/*layer*/,
                                                                             dst_rect.getPointer(),
                                                                             0/*src*/,
                                                                             src_rect.getPointer(),
                                                                             DISPMANX_PROTECTION_NONE,
                                                                             null /*alpha*/,
                                                                             null/*clamp*/,
                                                                             0/*transform*/);

        final EGL_DISPMANX_WINDOW_T nativewindow = new EGL_DISPMANX_WINDOW_T();
        nativewindow.element = dispman_element;
        nativewindow.width = width;
        nativewindow.height = height;
        nativewindow.write();

        this.libbcm_host.vc_dispmanx_update_submit_sync(dispman_update);

        return nativewindow;
    }
}
