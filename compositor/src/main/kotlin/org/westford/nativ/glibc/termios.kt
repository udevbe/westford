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
package org.westford.nativ.glibc

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(Field(name = "c_iflag",
              type = CType.UNSIGNED_INT),
        Field(name = "c_oflag",
              type = CType.UNSIGNED_INT),
        Field(name = "c_cflag",
              type = CType.UNSIGNED_INT),
        Field(name = "c_lflag",
              type = CType.UNSIGNED_INT),
        Field(name = "c_line",
              type = CType.UNSIGNED_CHAR),
        Field(name = "c_cc",
              type = CType.UNSIGNED_CHAR,
              cardinality = Libc.NCCS),
        Field(name = "c_ispeed",
              type = CType.UNSIGNED_INT),
        Field(name = "c_ospeed",
              type = CType.UNSIGNED_INT)) class termios : Struct_termios()
