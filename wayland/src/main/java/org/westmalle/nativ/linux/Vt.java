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
package org.westmalle.nativ.linux;


public class Vt {
    public static final short VT_OPENQRY = 0x5600;
    public static final short VT_GETMODE = 0x5601;  /* get mode of active vt */
    public static final short VT_SETMODE = 0x5602;  /* set mode of active vt */

    public static final byte  VT_AUTO    = 0x00;    /* auto vt switching */
    public static final byte  VT_PROCESS = 0x01;    /* process controls switching */
    public static final byte  VT_ACKACQ  = 0x02;    /* acknowledge switch */

    public static final short VT_ACTIVATE   = 0x5606;  /* make vt active */
    public static final short VT_WAITACTIVE = 0x5607;  /* wait for vt active */

    public static final short VT_GETSTATE = 0x5603;

    public static final short VT_RELDISP = 0x5605;  /* release display */
}
