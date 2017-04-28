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
package org.westford.compositor.drm

import com.google.auto.factory.AutoFactory
import org.westford.nativ.libdrm.DrmModeConnector
import org.westford.nativ.libdrm.DrmModeModeInfo
import org.westford.nativ.libdrm.DrmModeRes

import javax.annotation.Nonnegative

@AutoFactory(allowSubclasses = true,
             className = "DrmOutputFactory") class DrmOutput(val drmModeRes: DrmModeRes,
                                                             val drmModeConnector: DrmModeConnector,
                                                             @param:Nonnegative val crtcId: Int,
                                                             val mode: DrmModeModeInfo)
