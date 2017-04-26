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
package org.westford.nativ.libpng

import org.freedesktop.jaccall.Functor
import org.freedesktop.jaccall.Lng
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Unsigned

@Functor
@FunctionalInterface
interface png_rw_ptr {

    fun `$`(@Ptr png_structp: Long,
            @Ptr(Byte::class) png_bytep: Long,
            @Unsigned @Lng png_size_t: Long)
}
