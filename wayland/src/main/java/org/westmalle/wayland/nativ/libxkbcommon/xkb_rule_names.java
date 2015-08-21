//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.nativ.libxkbcommon;


import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xkb_rule_names extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("rules",
                                                             "model",
                                                             "layout",
                                                             "variant",
                                                             "options");

    /**
     * The rules file to use. The rules file describes how to interpret
     * the values of the model, layout, variant and options fields.
     * <p/>
     * If NULL or the empty string "", a default value is used.
     * If the XKB_DEFAULT_RULES environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    Pointer rules;
    /**
     * The keyboard model by which to interpret keycodes and LEDs.
     * <p/>
     * If NULL or the empty string "", a default value is used.
     * If the XKB_DEFAULT_MODEL environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    Pointer model;
    /**
     * A comma separated list of layouts (languages) to include in the
     * keymap.
     * <p/>
     * If NULL or the empty string "", a default value is used.
     * If the XKB_DEFAULT_LAYOUT environment variable is set, it is used
     * as the default.  Otherwise the system default is used.
     */
    Pointer layout;
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
    Pointer variant;
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
    Pointer options;


    @Override
    protected List getFieldOrder() {
        return FIELD_ORDER;
    }
}
