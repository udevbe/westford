package org.westmalle.wayland.dispmanx;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.westmalle.wayland.core.OutputFactory;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

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


        return null;
    }

    private EGL_DISPMANX_WINDOW_T createDispmanxWindow(){
        return null;
    }
}
