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
package org.westford.compositor.drm;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westford.compositor.core.RenderOutput;
import org.westford.compositor.core.Renderer;
import org.westford.compositor.protocol.WlOutput;
import org.westford.nativ.libdrm.DrmModeConnector;
import org.westford.nativ.libdrm.DrmModeModeInfo;
import org.westford.nativ.libdrm.DrmModeRes;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "DrmOutputFactory")
public class DrmOutput {

    @Nonnull
    private final DrmModeRes       drmModeRes;
    @Nonnull
    private final DrmModeConnector drmModeConnector;
    private final int              crtcId;
    @Nonnull
    private final DrmModeModeInfo  mode;

    DrmOutput(@Nonnull final DrmModeRes drmModeRes,
              @Nonnull final DrmModeConnector drmModeConnector,
              @Nonnegative final int crtcId,
              @Nonnull final DrmModeModeInfo mode) {
        this.drmModeRes = drmModeRes;
        this.drmModeConnector = drmModeConnector;
        this.crtcId = crtcId;
        this.mode = mode;
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
