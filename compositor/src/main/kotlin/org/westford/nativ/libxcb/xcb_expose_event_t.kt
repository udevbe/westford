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
package org.westford.nativ.libxcb

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(Field(name = "response_type",
              type = CType.CHAR),
        Field(name = "pad0",
              type = CType.CHAR),
        Field(name = "sequence",
              type = CType.SHORT),
        Field(name = "window",
              type = CType.INT),
        Field(name = "x",
              type = CType.SHORT),
        Field(name = "y",
              type = CType.SHORT),
        Field(name = "width",
              type = CType.SHORT),
        Field(name = "height",
              type = CType.SHORT),
        Field(name = "count",
              type = CType.SHORT)) class xcb_expose_event_t : Struct_xcb_expose_event_t()
