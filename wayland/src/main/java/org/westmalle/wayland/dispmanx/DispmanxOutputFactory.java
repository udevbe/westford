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


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.core.OutputGeometry;
import org.westmalle.wayland.core.OutputMode;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.nativ.libbcm_host.VC_DISPMANX_ALPHA_T;
import org.westmalle.wayland.nativ.libbcm_host.VC_RECT_T;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_NO_ROTATE;
import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_PROTECTION_NONE;

//TODO unit test
//TODO refactor once we get all of this working
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

        final int display = this.libbcm_host.vc_dispmanx_display_open(device);
        if (display == 0) {
            throw new RuntimeException("Failed to open dispmanx display for device " + device);
        }
        final DISPMANX_MODEINFO_T modeinfo = new DISPMANX_MODEINFO_T();
        final int success = this.libbcm_host.vc_dispmanx_display_get_info(display,
                                                                          Pointer.ref(modeinfo).address);
        if (success < 0) {
            throw new RuntimeException("Failed get info for display=" + device);
        }

        final int dispmanxElement = createDispmanxWindow(display,
                                                         modeinfo);
        final DispmanxOutput dispmanxOutput = this.privateDispmanxOutputFactory.create(dispmanxElement,
                                                                                       modeinfo);
        final Output output = createOutput(dispmanxOutput,
                                           modeinfo);
        return this.wlOutputFactory.create(output);
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
                                                            .transform(WlOutputTransform.NORMAL.value)
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .flags(0)
                                                .height(modeinfo.height())
                                                .width(modeinfo.width())
                                                .refresh(60)
                                                .build();
        return this.outputFactory.create(outputGeometry,
                                         outputMode,
                                         dispmanxOutput);
    }

    private int createDispmanxWindow(final int display,
                                     final DISPMANX_MODEINFO_T modeinfo) {

        final VC_RECT_T dst_rect = new VC_RECT_T();
        final VC_RECT_T src_rect = new VC_RECT_T();

        this.libbcm_host.vc_dispmanx_rect_set(Pointer.ref(dst_rect).address,
                                              0,
                                              0,
                                              modeinfo.width(),
                                              modeinfo.height());
        this.libbcm_host.vc_dispmanx_rect_set(Pointer.ref(src_rect).address,
                                              0,
                                              0,
                                              modeinfo.width() << 16,
                                              modeinfo.height() << 16);

        final int update = this.libbcm_host.vc_dispmanx_update_start(0);

        final VC_DISPMANX_ALPHA_T alpharules = new VC_DISPMANX_ALPHA_T();
        alpharules.flags(Libbcm_host.DISPMANX_FLAGS_ALPHA_FIXED_ALL_PIXELS);
        alpharules.opacity(255);
        alpharules.mask(0);

        final int dispmanxElement = this.libbcm_host.vc_dispmanx_element_add(update,
                                                                             display,
                                                                             0 /* layer */,
                                                                             Pointer.ref(dst_rect).address,
                                                                             0 /* src resource */,
                                                                             Pointer.ref(src_rect).address,
                                                                             DISPMANX_PROTECTION_NONE,
                                                                             Pointer.ref(alpharules).address,
                                                                             0L /* clamp */,
                                                                             DISPMANX_NO_ROTATE);
        this.libbcm_host.vc_dispmanx_update_submit_sync(update);

        return dispmanxElement;
    }
}