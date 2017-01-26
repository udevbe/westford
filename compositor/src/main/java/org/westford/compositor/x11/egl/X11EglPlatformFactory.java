/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.x11.egl;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westford.compositor.core.GlRenderer;
import org.westford.compositor.core.Output;
import org.westford.compositor.core.OutputFactory;
import org.westford.compositor.core.OutputGeometry;
import org.westford.compositor.core.OutputMode;
import org.westford.compositor.core.events.RenderOutputDestroyed;
import org.westford.compositor.protocol.WlOutput;
import org.westford.compositor.protocol.WlOutputFactory;
import org.westford.compositor.x11.X11Output;
import org.westford.compositor.x11.X11Platform;
import org.westford.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westford.nativ.libEGL.EglGetPlatformDisplayEXT;
import org.westford.nativ.libEGL.LibEGL;
import org.westford.nativ.libxcb.Libxcb;
import org.westford.nativ.libxcb.xcb_client_message_event_t;
import org.westford.nativ.libxcb.xcb_screen_t;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westford.nativ.libEGL.LibEGL.EGL_BACK_BUFFER;
import static org.westford.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westford.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westford.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westford.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westford.nativ.libEGL.LibEGL.EGL_PLATFORM_X11_KHR;
import static org.westford.nativ.libEGL.LibEGL.EGL_RENDER_BUFFER;
import static org.westford.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westford.nativ.libEGL.LibEGL.EGL_VERSION;
import static org.westford.nativ.libxcb.Libxcb.XCB_CLIENT_MESSAGE;

public class X11EglPlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final Libxcb                       libxcb;
    @Nonnull
    private final LibEGL                       libEGL;
    @Nonnull
    private final PrivateX11EglPlatformFactory privateX11EglPlatformFactory;
    @Nonnull
    private final X11Platform                  x11Platform;
    @Nonnull
    private final WlOutputFactory              wlOutputFactory;
    @Nonnull
    private final OutputFactory                outputFactory;
    @Nonnull
    private final GlRenderer                   glRenderer;
    @Nonnull
    private final X11EglOutputFactory          x11EglOutputFactory;

    @Inject
    X11EglPlatformFactory(@Nonnull final Libxcb libxcb,
                          @Nonnull final LibEGL libEGL,
                          @Nonnull final PrivateX11EglPlatformFactory privateX11EglPlatformFactory,
                          @Nonnull final X11Platform x11Platform,
                          @Nonnull final WlOutputFactory wlOutputFactory,
                          @Nonnull final OutputFactory outputFactory,
                          @Nonnull final GlRenderer glRenderer,
                          @Nonnull final X11EglOutputFactory x11EglOutputFactory) {
        this.libxcb = libxcb;
        this.libEGL = libEGL;
        this.privateX11EglPlatformFactory = privateX11EglPlatformFactory;
        this.x11Platform = x11Platform;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.glRenderer = glRenderer;
        this.x11EglOutputFactory = x11EglOutputFactory;
    }

    @Nonnull
    public X11EglPlatform create() {

        final long eglDisplay = createEglDisplay(this.x11Platform.getxDisplay());

        final String eglExtensions = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(eglDisplay,
                                                                             EGL_EXTENSIONS))
                                            .dref();
        final String eglClientApis = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(eglDisplay,
                                                                             EGL_CLIENT_APIS))
                                            .dref();
        final String eglVendor = Pointer.wrap(String.class,
                                              this.libEGL.eglQueryString(eglDisplay,
                                                                         EGL_VENDOR))
                                        .dref();
        final String eglVersion = Pointer.wrap(String.class,
                                               this.libEGL.eglQueryString(eglDisplay,
                                                                          EGL_VERSION))
                                         .dref();

        LOGGER.info(format("Creating X11 EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n"
                           + "\tEGL extensions: %s",
                           eglClientApis,
                           eglVendor,
                           eglVersion,
                           eglExtensions));

        final long eglConfig = this.glRenderer.eglConfig(eglDisplay,
                                                         eglExtensions);
        final long eglContext = createEglContext(eglDisplay,
                                                 eglConfig);

        final List<X11Output>    x11Outputs    = this.x11Platform.getRenderOutputs();
        final List<X11EglOutput> x11EglOutputs = new ArrayList<>(x11Outputs.size());
        final List<WlOutput>     wlOutputs     = new ArrayList<>(x11EglOutputs.size());

        x11Outputs.forEach(x11RenderOutput -> x11EglOutputs.add(this.x11EglOutputFactory.create(x11RenderOutput,
                                                                                                createEglSurface(eglDisplay,
                                                                                                                 eglConfig,
                                                                                                                 x11RenderOutput.getXWindow()),
                                                                                                eglContext,
                                                                                                eglDisplay)));
        x11EglOutputs.forEach(x11EglOutput -> wlOutputs.add(this.wlOutputFactory.create(createOutput(x11EglOutput))));

        final X11EglPlatform x11EglPlatform = this.privateX11EglPlatformFactory.create(wlOutputs,
                                                                                       eglDisplay,
                                                                                       eglContext,
                                                                                       eglExtensions);

        this.x11Platform.getX11EventBus()
                        .getXEventSignal()
                        .connect(event -> {
                            final int responseType = (event.dref()
                                                           .response_type() & 0x7f);
                            switch (responseType) {
                                case XCB_CLIENT_MESSAGE: {
                                    handle(event.castp(xcb_client_message_event_t.class),
                                           x11EglPlatform);

                                    break;
                                }
                            }
                        });

        return x11EglPlatform;
    }

    private long createEglDisplay(final long nativeDisplay) {

        final Pointer<String> noDisplayExtensions = Pointer.wrap(String.class,
                                                                 this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                            EGL_EXTENSIONS));
        if (noDisplayExtensions.address == 0L) {
            throw new RuntimeException("Could not query egl extensions.");
        }
        final String extensions = noDisplayExtensions.dref();

        if (!extensions.contains("EGL_EXT_platform_x11")) {
            throw new RuntimeException("Required extension EGL_EXT_platform_x11 not available.");
        }

        final Pointer<EglGetPlatformDisplayEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglGetPlatformDisplayEXT.class,
                                                                                        this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address));

        final long eglDisplay = eglGetPlatformDisplayEXT.dref()
                                                        .$(EGL_PLATFORM_X11_KHR,
                                                           nativeDisplay,
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

    private long createEglSurface(final long eglDisplay,
                                  final long config,
                                  final int nativeWindow) {
        final Pointer<Integer> eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                                                EGL_BACK_BUFFER,
                                                                EGL_NONE);

        final Pointer<EglCreatePlatformWindowSurfaceEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglCreatePlatformWindowSurfaceEXT.class,
                                                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address));
        final long eglSurface = eglGetPlatformDisplayEXT.dref()
                                                        .$(eglDisplay,
                                                           config,
                                                           Pointer.nref(nativeWindow).address,
                                                           eglSurfaceAttribs.address);
        if (eglSurface == 0L) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }

        return eglSurface;
    }

    private Output createOutput(final X11EglOutput x11EglOutput) {
        final X11Output x11Output = x11EglOutput.getX11Output();

        final xcb_screen_t screen = x11Output.getScreen();
        final int          width  = x11Output.getWidth();
        final int          height = x11Output.getHeight();

        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .x(x11Output.getX())
                                                            .y(x11Output.getY())
                                                            .subpixel(0)
                                                            .make("Westford xcb")
                                                            .model("X11")
                                                            .physicalWidth((width / screen.width_in_pixels())
                                                                           * screen.width_in_millimeters())
                                                            .physicalHeight((height / screen.height_in_pixels())
                                                                            * screen.height_in_millimeters())
                                                            .transform(WlOutputTransform.NORMAL.value)
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .flags(0)
                                                .width(width)
                                                .height(height)
                                                .refresh(60)
                                                .build();
        return this.outputFactory.create(x11EglOutput,
                                         x11Output.getName(),
                                         outputGeometry,
                                         outputMode);
    }

    private void handle(final Pointer<xcb_client_message_event_t> event,
                        final X11EglPlatform x11EglPlatform) {
        final int atom = event.dref()
                              .data()
                              .data32()
                              .dref();
        final int sourceWindow = event.dref()
                                      .window();

        if (atom == this.x11Platform.getX11Atoms()
                                    .get("WM_DELETE_WINDOW")) {

            final Iterator<WlOutput> wlOutputIterator = x11EglPlatform.getWlOutputs()
                                                                      .iterator();
            while (wlOutputIterator.hasNext()) {
                final WlOutput wlOutput = wlOutputIterator.next();
                X11EglOutput x11EglOutput = (X11EglOutput) wlOutput.getOutput()
                                                                   .getRenderOutput();
                final X11Output x11Output = x11EglOutput.getX11Output();

                if (x11Output.getXWindow() == sourceWindow) {
                    this.libxcb.xcb_destroy_window(this.x11Platform.getXcbConnection(),
                                                   sourceWindow);
                    wlOutputIterator.remove();
                    x11EglPlatform.getRenderOutputDestroyedSignal()
                                  .emit(RenderOutputDestroyed.create(wlOutput));
                    return;
                }
            }
        }
    }
}
