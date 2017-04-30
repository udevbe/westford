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
package org.westford.nativ.linux

object Kd {
    val KDGKBMODE: Short = 0x4B44
    val KDSKBMODE: Short = 0x4B45
    val K_RAW: Byte = 0x00
    val K_XLATE: Byte = 0x01
    val K_MEDIUMRAW: Byte = 0x02
    val K_UNICODE: Byte = 0x03
    val K_OFF: Byte = 0x04

    val KDSETMODE: Short = 0x4B3A  /* set text/graphics mode */
    val KD_TEXT: Byte = 0x00
    val KD_GRAPHICS: Byte = 0x01
    val KD_TEXT0: Byte = 0x02   /* obsolete */
    val KD_TEXT1: Byte = 0x03  /* obsolete */
    val KDGETMODE: Short = 0x4B3B /* get current mode */
}
