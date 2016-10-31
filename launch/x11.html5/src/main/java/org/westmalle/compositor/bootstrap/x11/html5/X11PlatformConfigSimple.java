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
package org.westmalle.compositor.bootstrap.x11.html5;


import org.westmalle.compositor.x11.config.X11OutputConfig;
import org.westmalle.compositor.x11.config.X11PlatformConfig;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class X11PlatformConfigSimple implements X11PlatformConfig {

    @Nonnull
    @Override
    public String getDisplay() {
        return ":0";
    }

    @Nonnull
    @Override
    public Iterable<X11OutputConfig> getX11RenderOutputConfigs() {
        return Arrays.asList(new X11OutputConfig() {
                                 @Nonnull
                                 @Override
                                 public String getName() {
                                     return "window0";
                                 }

                                 @Override
                                 public int getWidth() {
                                     return 1024;
                                 }

                                 @Override
                                 public int getHeight() {
                                     return 768;
                                 }
                             },
                             new X11OutputConfig() {
                                 @Nonnull
                                 @Override
                                 public String getName() {
                                     return "window1";
                                 }

                                 @Override
                                 public int getWidth() {
                                     return 1024;
                                 }

                                 @Override
                                 public int getHeight() {
                                     return 768;
                                 }
                             }
                            );
    }
}
