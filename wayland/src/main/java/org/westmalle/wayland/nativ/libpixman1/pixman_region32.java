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
package org.westmalle.wayland.nativ.libpixman1;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class pixman_region32 extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("extents",
                                                             "data");

    /**
     * C type : pixman_box32_t
     */
    public pixman_box32                     extents;
    /**
     * C type : pixman_region32_data_t*
     */
    public pixman_region32_data.ByReference data;

    public pixman_region32() {
        super();
    }

    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
