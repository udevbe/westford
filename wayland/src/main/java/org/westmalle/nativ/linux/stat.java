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

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "st_dev",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_ino",
                       type = CType.UNSIGNED_LONG),
                @Field(name = "st_mode",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_nlink",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_uid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_gid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_rdev",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_size",
                       type = CType.LONG),
                @Field(name = "st_blksize",
                       type = CType.LONG),
                @Field(name = "blkcnt_t",
                       type = CType.UNSIGNED_LONG),
                @Field(name = "st_atime",
                       type = CType.LONG),
                @Field(name = "st_mtime",
                       type = CType.LONG),
                @Field(name = "st_ctime",
                       type = CType.LONG),

        })
public final class stat extends stat_Jaccall_StructType {}
