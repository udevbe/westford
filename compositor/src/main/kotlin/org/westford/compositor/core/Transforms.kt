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
package org.westford.compositor.core

import org.westford.compositor.core.calc.Mat4

import javax.annotation.Nonnegative

object Transforms {

    val NORMAL = Mat4.IDENTITY
    //@formatter:off
     val _90 = Mat4.create(0f, -1f, 0f, 0f,
1f, 0f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     val _180 = Mat4.create(-1f, 0f, 0f, 0f,
0f, -1f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     val _270 = Mat4.create(0f, 1f, 0f, 0f,
-1f, -0f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     val FLIPPED = Mat4.create(-1f, 0f, 0f, 0f,
0f, 1f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     val FLIPPED_90 = Mat4.create(0f, 1f, 0f, 0f,
1f, 0f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     val FLIPPED_180 = Mat4.create(1f, 0f, 0f, 0f,
0f, -1f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     val FLIPPED_270 = Mat4.create(0f, -1f, 0f, 0f,
-1f, 0f, 0f, 0f,
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
 //@formatter:on

    //@formatter:off
     fun SCALE(@Nonnegative scale:Float):Mat4 {
return Mat4.create(scale, 0f, 0f, 0f,
0f, scale, 0f, 0f,
0f, 0f, scale, 0f,
0f, 0f, 0f, 1f)
}
 //@formatter:on

    //@formatter:off
     fun TRANSLATE(x:Int,
y:Int):Mat4 {
return Mat4.create(1f, 0f, 0f, x.toFloat(),
0f, 1f, 0f, y.toFloat(),
0f, 0f, 1f, 0f,
0f, 0f, 0f, 1f)
}
 //@formatter:on
}
