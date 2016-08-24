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
package org.westmalle.nativ.libxcb;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "root",
                       type = CType.INT),
                @Field(name = "default_colormap",
                       type = CType.INT),
                @Field(name = "white_pixel",
                       type = CType.INT),
                @Field(name = "black_pixel",
                       type = CType.INT),
                @Field(name = "current_input_masks",
                       type = CType.INT),
                @Field(name = "width_in_pixels",
                       type = CType.SHORT),
                @Field(name = "height_in_pixels",
                       type = CType.SHORT),
                @Field(name = "width_in_millimeters",
                       type = CType.SHORT),
                @Field(name = "height_in_millimeters",
                       type = CType.SHORT),
                @Field(name = "min_installed_maps",
                       type = CType.SHORT),
                @Field(name = "max_installed_maps",
                       type = CType.SHORT),
                @Field(name = "root_visual",
                       type = CType.INT),
                @Field(name = "backing_stores",
                       type = CType.CHAR),
                @Field(name = "save_unders",
                       type = CType.CHAR),
                @Field(name = "root_depth",
                       type = CType.CHAR),
                @Field(name = "allowed_depths_len",
                       type = CType.CHAR),
        })
public final class xcb_screen_t extends xcb_screen_t_Jaccall_StructType {}

