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
package org.westford.nativ.linux;


public class TermBits {
    public static final int OPOST = 0x1;
    public static final int OLCUC = 0x2;
    public static final int ONLCR = 0x4;
    public static final int OCRNL = 0x8;

    public static final int TCSANOW   = 0;
    public static final int TCSADRAIN = 1;
    public static final int TCSAFLUSH = 2;

    public static final int TCIFLUSH  = 0;
    public static final int TCOFLUSH  = 1;
    public static final int TCIOFLUSH = 2;
}
