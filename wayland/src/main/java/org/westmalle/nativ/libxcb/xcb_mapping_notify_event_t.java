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
                @Field(name = "response_type",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "pad0",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "sequence",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "request",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "first_keycode",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "count",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "pad1",
                       type = CType.UNSIGNED_CHAR)
        })
public final class xcb_mapping_notify_event_t extends xcb_mapping_notify_event_t_Jaccall_StructType{}
