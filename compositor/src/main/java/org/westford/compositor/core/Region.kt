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

interface Region {

    fun asList(): List<Rectangle>

    fun add(rectangle: Rectangle)

    fun subtract(rectangle: Rectangle)

    operator fun contains(point: Point): Boolean

    fun contains(clipping: Rectangle,
                 point: Point): Boolean

    operator fun contains(rectangle: Rectangle): Boolean

    fun intersect(rectangle: Rectangle): Region

    fun copy(): Region

    fun isEmpty(): Boolean
}

