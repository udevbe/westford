package org.westmalle.wayland.drm;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.nativ.libdrm.DrmModeConnector;
import org.westmalle.wayland.nativ.libdrm.DrmModeModeInfo;
import org.westmalle.wayland.nativ.libdrm.DrmModeRes;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Optional;

//TODO drm connector, remove all gbm dependencies
@AutoFactory(allowSubclasses = true,
             className = "DrmConnectorFactory")
public class DrmConnector implements Connector {

    @Nonnull
    private final Optional<WlOutput> wlOutput;
    @Nonnull
    private final DrmModeRes         drmModeRes;
    @Nonnull
    private final DrmModeConnector   drmModeConnector;
    private final int                crtcId;
    @Nonnull
    private final DrmModeModeInfo    mode;

    DrmConnector(@Nonnull
                 final Optional<WlOutput> wlOutput,
                 @Nonnull
                 final DrmModeRes drmModeRes,
                 @Nonnull
                 final DrmModeConnector drmModeConnector,
                 @Nonnegative
                 final int crtcId,
                 @Nonnull
                 final DrmModeModeInfo mode) {
        this.wlOutput = wlOutput;
        this.drmModeRes = drmModeRes;
        this.drmModeConnector = drmModeConnector;
        this.crtcId = crtcId;
        this.mode = mode;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.wlOutput;
    }

    @Nonnull
    public DrmModeRes getDrmModeRes() {
        return this.drmModeRes;
    }

    @Nonnull
    public DrmModeConnector getDrmModeConnector() {
        return this.drmModeConnector;
    }

    public int getCrtcId() {
        return this.crtcId;
    }

    @Nonnull
    public DrmModeModeInfo getMode() {
        return this.mode;
    }


}
