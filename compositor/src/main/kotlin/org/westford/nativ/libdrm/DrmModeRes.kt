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
package org.westford.nativ.libdrm

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(Field(name = "count_fbs",
              type = CType.INT),
        Field(name = "fbs",
              type = CType.POINTER,
              dataType = Int::class),
        Field(name = "count_crtcs",
              type = CType.INT),
        Field(name = "crtcs",
              type = CType.POINTER,
              dataType = Int::class),
        Field(name = "count_connectors",
              type = CType.INT),
        Field(name = "connectors",
              type = CType.POINTER,
              dataType = Int::class),
        Field(name = "count_encoders",
              type = CType.INT),
        Field(name = "encoders",
              type = CType.POINTER,
              dataType = Int::class),
        Field(name = "min_width",
              type = CType.UNSIGNED_INT),
        Field(name = "max_width",
              type = CType.UNSIGNED_INT),
        Field(name = "min_height",
              type = CType.UNSIGNED_INT),
        Field(name = "max_height",
              type = CType.UNSIGNED_INT)) class DrmModeRes : DrmModeRes_Jaccall_StructType()
