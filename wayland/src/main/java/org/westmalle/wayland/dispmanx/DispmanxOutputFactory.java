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
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.nativ.libbcm_host.VC_RECT_T;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_PROTECTION_NONE;

//TODO unit test
public class DispmanxOutputFactory {

    @Nonnull
    private final Libbcm_host                  libbcm_host;
    @Nonnull
    private final WlOutputFactory              wlOutputFactory;
    @Nonnull
    private final OutputFactory                outputFactory;
    @Nonnull
    private final PrivateDispmanxOutputFactory privateDispmanxOutputFactory;
    @Nonnull
    private final WlCompositor                 wlCompositor;

    @Inject
    DispmanxOutputFactory(@Nonnull final Libbcm_host libbcm_host,
                          @Nonnull final WlOutputFactory wlOutputFactory,
                          @Nonnull final OutputFactory outputFactory,
                          @Nonnull final PrivateDispmanxOutputFactory privateDispmanxOutputFactory,
                          @Nonnull final WlCompositor wlCompositor) {
        this.libbcm_host = libbcm_host;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.privateDispmanxOutputFactory = privateDispmanxOutputFactory;
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
        final int displayHandle = this.libbcm_host.vc_dispmanx_display_open(device);
        if (displayHandle == 0) {
            throw new RuntimeException("Failed to open dispmanx display for device " + device);
        }

        final Pointer widthP  = new Memory(Integer.SIZE);
        final Pointer heightP = new Memory(Integer.SIZE);

        final int success = this.libbcm_host.graphics_get_display_size((short) device,
                                                                       widthP,
                                                                       heightP);
        if (success <= 0) {
            throw new RuntimeException("Failed get size for display " + device);
        }

        final int width  = widthP.getInt(0);
        final int height = heightP.getInt(0);

        final EGL_DISPMANX_WINDOW_T dispmanxWindow = createDispmanxWindow(device,
                                                                          displayHandle,
                                                                          width,
                                                                          height);

        final DispmanxOutput dispmanxOutput = createDispmanxOutput(dispmanxWindow);

        final Output output = createOutput(dispmanxOutput,
                                           width,
                                           height);

        return this.wlOutputFactory.create(output);
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

    public DispmanxOutput createDispmanxOutput(final EGL_DISPMANX_WINDOW_T dispmanxWindow) {
        return this.privateDispmanxOutputFactory.create(dispmanxWindow);
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
