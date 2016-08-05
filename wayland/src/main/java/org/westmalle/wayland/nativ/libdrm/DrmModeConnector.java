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
                @Field(name = "connector_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "encoder_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "connector_type",
                       type = CType.UNSIGNED_INT),
                @Field(name = "connector_type_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "connection",
                       type = CType.INT),
                @Field(name = "mmWidth",
                       type = CType.UNSIGNED_INT),
                @Field(name = "mmHeight",
                       type = CType.UNSIGNED_INT),
                @Field(name = "drmModeSubPixel",
                       type = CType.INT),
                @Field(name = "count_modes",
                       type = CType.INT),
                @Field(name = "modes",
                       type = CType.POINTER,
                       dataType = DrmModeModeInfo.class),
                @Field(name = "count_props",
                       type = CType.INT),
                @Field(name = "props",
                       type = CType.POINTER,
                       dataType = Integer.class),
                @Field(name = "prop_values",
                       type = CType.POINTER,
                       dataType = Long.class),
                @Field(name = "count_encoders",
                       type = CType.INT),
                @Field(name = "encoders",
                       type = CType.POINTER,
                       dataType = Integer.class),

        })
public final class DrmModeConnector extends DrmModeConnector_Jaccall_StructType {}
