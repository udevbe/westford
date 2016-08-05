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
package org.westmalle.wayland.nativ.libxkbcommon;


import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "rules",
                       type = CType.POINTER,
                       dataType = String.class),
                @Field(name = "model",
                       type = CType.POINTER,
                       dataType = String.class),
                @Field(name = "layout",
                       type = CType.POINTER,
                       dataType = String.class),
                @Field(name = "variant",
                       type = CType.POINTER,
                       dataType = String.class),
                @Field(name = "options",
                       type = CType.POINTER,
                       dataType = String.class)
        })
public final class xkb_rule_names extends xkb_rule_names_Jaccall_StructType {

    /**
     * The rules file to use. The rules file describes how to interpret
     * the values of the model, layout, variant and options fields.
     * <p/>
     * If NULL or the empty string "", a default value is used.
     * If the XKB_DEFAULT_RULES environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    //Pointer rules;
    /**
     * The keyboard model by which to interpret keycodes and LEDs.
     * <p/>
     * If NULL or the empty string "", a default value is used.
     * If the XKB_DEFAULT_MODEL environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    //Pointer model;
    /**
     * A comma separated list of layouts (languages) to include in the
     * keymap.
     * <p/>
     * If NULL or the empty string "", a default value is used.
     * If the XKB_DEFAULT_LAYOUT environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    //Pointer layout;
    /**
     * A comma separated list of variants, one per layout, which may
     * modify or augment the respective layout in various ways.
     * <p/>
     * If NULL or the empty string "", and a default value is also used
     * for the layout, a default value is used.  Otherwise no variant is
     * used.
     * If the XKB_DEFAULT_VARIANT environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    //Pointer variant;
    /**
     * A comma separated list of options, through which the user specifies
     * non-layout related preferences, like which key combinations are used
     * for switching layouts, or which key is the Compose key.
     * <p/>
     * If NULL, a default value is used.  If the empty string "", no
     * options are used.
     * If the XKB_DEFAULT_OPTIONS environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    //Pointer options;
}
