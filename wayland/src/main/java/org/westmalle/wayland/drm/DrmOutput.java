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
package org.westmalle.wayland.drm;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.nativ.libdrm.DrmModeConnector;
import org.westmalle.nativ.libdrm.DrmModeModeInfo;
import org.westmalle.nativ.libdrm.DrmModeRes;
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

//TODO drm connector, remove all gbm dependencies
@AutoFactory(allowSubclasses = true,
             className = "DrmOutputFactory")
public class DrmOutput implements RenderOutput {

    @Nonnull
    private final Renderer         renderer;
    @Nonnull
    private final WlOutput         wlOutput;
    @Nonnull
    private final DrmModeRes       drmModeRes;
    @Nonnull
    private final DrmModeConnector drmModeConnector;
    private final int              crtcId;
    @Nonnull
    private final DrmModeModeInfo  mode;

    DrmOutput(@Nonnull @Provided final Renderer renderer,
              @Nonnull final WlOutput wlOutput,
              @Nonnull final DrmModeRes drmModeRes,
              @Nonnull final DrmModeConnector drmModeConnector,
              @Nonnegative final int crtcId,
              @Nonnull final DrmModeModeInfo mode) {
        this.renderer = renderer;
        this.wlOutput = wlOutput;
        this.drmModeRes = drmModeRes;
        this.drmModeConnector = drmModeConnector;
        this.crtcId = crtcId;
        this.mode = mode;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
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

    @Override
    public void render() {
        this.renderer.visit(this);
    }
}
