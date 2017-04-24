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
package org.westford.compositor.x11;

import com.google.auto.factory.AutoFactory;
import org.westford.nativ.libxcb.xcb_screen_t;

@AutoFactory(allowSubclasses = true,
             className = "X11OutputFactory")
public class X11Output {

    private final int xWindow;

    private final int          x;
    private final int          y;
    private final int          width;
    private final int          height;
    private final String       name;
    private final xcb_screen_t screen;

    X11Output(final int xWindow,
              final int x,
              final int y,
              final int width,
              final int height,
              final String name,
              final xcb_screen_t screen) {
        this.xWindow = xWindow;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        this.screen = screen;
    }

    public int getXWindow() {
        return this.xWindow;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getName() {
        return this.name;
    }

    public xcb_screen_t getScreen() {
        return this.screen;
    }
}
