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
import org.westmalle.wayland.nativ.libbcm_host.VC_DISPMANX_ALPHA_T;
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
import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_NO_ROTATE;
import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_PROTECTION_NONE;

//TODO unit test
//TODO refactor once we get all of this working
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
        final Pointer eglDisplay = createEglDisplay();
        final Pointer config     = chooseConfig(eglDisplay);

        //setup dispmanx
        final int display = this.libbcm_host.vc_dispmanx_display_open(device);
        if (display == 0) {
            throw new RuntimeException("Failed to open dispmanx display for device " + device);
        }
        final DISPMANX_MODEINFO_T modeinfo = new DISPMANX_MODEINFO_T();
        final int success = this.libbcm_host.vc_dispmanx_display_get_info(display,
                                                                          modeinfo.getPointer());
        modeinfo.read();
        if (success < 0) {
            throw new RuntimeException("Failed get info for display=" + device);
        }

        final EGL_DISPMANX_WINDOW_T dispmanxWindow = createDispmanxWindow(display,
                                                                          modeinfo);

        //setup egl surface
        final Pointer eglSurface = createEglSurface(eglDisplay,
                                                    config,
                                                    dispmanxWindow);


        final DispmanxEglOutput dispmanxEglOutput = this.privateDispmanxEglOutputFactory.create(eglDisplay,
                                                                                                eglSurface);
        final DispmanxOutput dispmanxOutput = this.privateDispmanxOutputFactory.create(dispmanxEglOutput);
        final Output output = createOutput(dispmanxOutput,
                                           modeinfo);
        return this.wlOutputFactory.create(output);
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

    private Output createOutput(final DispmanxOutput dispmanxOutput,
                                final DISPMANX_MODEINFO_T modeinfo) {
        //TODO this is all guessing. Does dispmanx expose actual values?

        //TODO assume 96dpi for physical width(?)
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
                                                .height(modeinfo.height)
                                                .width(modeinfo.width)
                                                .refresh(60)
                                                .build();
        return this.outputFactory.create(outputGeometry,
                                         outputMode,
                                         dispmanxOutput);
    }

    private EGL_DISPMANX_WINDOW_T createDispmanxWindow(final int display,
                                                       final DISPMANX_MODEINFO_T modeinfo) {

        final VC_RECT_T dst_rect = new VC_RECT_T();
        final VC_RECT_T src_rect = new VC_RECT_T();

        this.libbcm_host.vc_dispmanx_rect_set(dst_rect.getPointer(),
                                              0,
                                              0,
                                              modeinfo.width,
                                              modeinfo.height);
        this.libbcm_host.vc_dispmanx_rect_set(src_rect.getPointer(),
                                              0,
                                              0,
                                              modeinfo.width << 16,
                                              modeinfo.height << 16);
        dst_rect.read();
        src_rect.read();

        final int update = this.libbcm_host.vc_dispmanx_update_start(0);

        final VC_DISPMANX_ALPHA_T alpharules = new VC_DISPMANX_ALPHA_T();
        alpharules.flags = Libbcm_host.DISPMANX_FLAGS_ALPHA_FIXED_ALL_PIXELS;
        alpharules.opacity = 255;
        alpharules.mask = 0;
        alpharules.write();

        final int egl_element = this.libbcm_host.vc_dispmanx_element_add(update,
                                                                         display,
                                                                         0 /* layer */,
                                                                         dst_rect.getPointer(),
                                                                         0 /* src resource */,
                                                                         src_rect.getPointer(),
                                                                         DISPMANX_PROTECTION_NONE,
                                                                         alpharules.getPointer(),
                                                                         null /* clamp */,
                                                                         DISPMANX_NO_ROTATE);
        this.libbcm_host.vc_dispmanx_update_submit_sync(update);

        final EGL_DISPMANX_WINDOW_T nativewindow = new EGL_DISPMANX_WINDOW_T();
        nativewindow.element = egl_element;
        nativewindow.width = modeinfo.width;
        nativewindow.height = modeinfo.height;
        nativewindow.write();

        return nativewindow;
    }
}