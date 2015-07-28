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
package org.westmalle.wayland.nativ.libxcb;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xcb_screen_iterator_t extends Structure {
    private static final List<?> FIELD_ORDER = Arrays.asList("data",
                                                             "rem",
                                                             "index");

    public xcb_screen_t.ByReference data;
    public int                      rem;
    public int                      index;

    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }

    public static class ByValue extends xcb_screen_iterator_t implements Structure.ByValue {

    }
}


