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
package org.westford.compositor.launch.x11

import org.westford.compositor.x11.config.X11OutputConfig
import org.westford.compositor.x11.config.X11PlatformConfig

class X11PlatformConfigSimple : X11PlatformConfig {

    override val display: String = System.getenv("DISPLAY")

    override val x11RenderOutputConfigs: List<X11OutputConfig> = listOf(X11OutputConfig(name = "window0",
                                                                                        width = 1024,
                                                                                        height = 768)
            //uncomment to enable a second output
            //                                                                        ,
            //                                                                        X11OutputConfig(name = "window1",
            //                                                                                        width = 1024,
            //                                                                                        height = 768)
                                                                       )
}
