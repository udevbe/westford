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
package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "clock",
                       type = CType.UNSIGNED_INT),
                @Field(name = "hdisplay",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "hsync_start",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "hsync_end",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "htotal",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "hskew",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vdisplay",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vsync_start",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vsync_end",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vtotal",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vskew",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vrefresh",
                       type = CType.UNSIGNED_INT),
                @Field(name = "flags",
                       type = CType.UNSIGNED_INT),
                @Field(name = "type",
                       type = CType.UNSIGNED_INT),
                @Field(name = "name",
                       type = CType.CHAR,
                       dataType = String.class,
                       cardinality = Libdrm.DRM_DISPLAY_MODE_LEN)
        })
public final class DrmModeModeInfo extends DrmModeModeInfo_Jaccall_StructType {

}
