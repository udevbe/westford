/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.compositor.dispmanx;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.compositor.core.Output;
import org.westmalle.compositor.core.OutputFactory;
import org.westmalle.compositor.core.OutputGeometry;
import org.westmalle.compositor.core.OutputMode;
import org.westmalle.compositor.dispmanx.DispmanxOutputFactory;
import org.westmalle.compositor.dispmanx.PrivateDispmanxPlatformFactory;
import org.westmalle.compositor.protocol.WlOutput;
import org.westmalle.compositor.protocol.WlOutputFactory;
import org.westmalle.nativ.libbcm_host.DISPMANX_MODEINFO_T;
import org.westmalle.nativ.libbcm_host.Libbcm_host;
import org.westmalle.nativ.libbcm_host.VC_DISPMANX_ALPHA_T;
import org.westmalle.nativ.libbcm_host.VC_RECT_T;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.westmalle.nativ.libbcm_host.Libbcm_host.DISPMANX_NO_ROTATE;
import static org.westmalle.nativ.libbcm_host.Libbcm_host.DISPMANX_PROTECTION_NONE;

//TODO unit test
//TODO refactor once we get all of this working
public class DispmanxPlatformFactory {

    @Nonnull
    private final Libbcm_host                    libbcm_host;
    @Nonnull
    private final WlOutputFactory                wlOutputFactory;
    @Nonnull
    private final OutputFactory                  outputFactory;
    @Nonnull
    private final PrivateDispmanxPlatformFactory privateDispmanxPlatformFactory;
    @Nonnull
    private final DispmanxOutputFactory          dispmanxOutputFactory;

    @Inject
    DispmanxPlatformFactory(@Nonnull final Libbcm_host libbcm_host,
                            @Nonnull final WlOutputFactory wlOutputFactory,
                            @Nonnull final OutputFactory outputFactory,
                            @Nonnull final PrivateDispmanxPlatformFactory privateDispmanxPlatformFactory,
                            @Nonnull final DispmanxOutputFactory dispmanxRenderOutputFactory) {
        this.libbcm_host = libbcm_host;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.privateDispmanxPlatformFactory = privateDispmanxPlatformFactory;
        this.dispmanxOutputFactory = dispmanxRenderOutputFactory;
    }

    public DispmanxPlatform create(final int device) {

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

        //TODO from config
        final List<DispmanxOutput> dispmanxOutputs = new ArrayList<>(1);
        final int dispmanxElement = createDispmanxWindow(display,
                                                         modeinfo);
        final Output output = createOutput(device,
                                           modeinfo);
        final WlOutput wlOutput = this.wlOutputFactory.create(output);

        dispmanxOutputs.add(this.dispmanxOutputFactory.create(wlOutput,
                                                              dispmanxElement));

        return this.privateDispmanxPlatformFactory.create(modeinfo,
                                                          dispmanxOutputs);
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

    private Output createOutput(final int device,
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
        //TODO translate dispmanx output number to udev readable nam
        return this.outputFactory.create("" + device,
                                         outputGeometry,
                                         outputMode);
    }
}