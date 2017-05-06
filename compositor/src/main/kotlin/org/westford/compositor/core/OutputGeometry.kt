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

import javax.annotation.Nonnegative

/**
 * @param x x position within the global compositor space, starting from top left.
 * @param y y position within the global compositor space, starting from top left.
 * @param physicalWidth width in millimeters of the output
 * @param physicalHeight height in millimeters of the output
 * @param subpixel subpixel orientation of the output
 * @param make textual description of the manufacturer
 * @param model textual description of the model
 * @param transform transform that maps framebuffer to output
 */
data class OutputGeometry(val x: Int,
                          val y: Int,
                          @param:Nonnegative val physicalWidth: Int,
                          @param: Nonnegative val physicalHeight: Int,
                          val subpixel: Int,
                          val make: String,
                          val model: String,
                          val transform: Int)
