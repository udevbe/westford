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

object Vt {
    val VT_OPENQRY: Short = 0x5600
    val VT_GETMODE: Short = 0x5601  /* get mode of active vt */
    val VT_SETMODE: Short = 0x5602  /* set mode of active vt */

    val VT_AUTO: Byte = 0x00    /* auto vt switching */
    val VT_PROCESS: Byte = 0x01    /* process controls switching */
    val VT_ACKACQ: Byte = 0x02    /* acknowledge switch */

    val VT_ACTIVATE: Short = 0x5606  /* make vt active */
    val VT_WAITACTIVE: Short = 0x5607  /* wait for vt active */

    val VT_GETSTATE: Short = 0x5603

    val VT_RELDISP: Short = 0x5605  /* release display */
}
