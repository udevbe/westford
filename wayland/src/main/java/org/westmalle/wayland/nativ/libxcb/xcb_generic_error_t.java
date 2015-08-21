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

public class xcb_generic_error_t extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("response_type",
                                                             "error_code",
                                                             "sequence",
                                                             "resource_id",
                                                             "minor_code",
                                                             "major_code",
                                                             "pad0",
                                                             "pad",
                                                             "full_sequence");

    public byte  response_type;
    public byte  error_code;
    public short sequence;
    public int   resource_id;
    public short minor_code;
    public byte  major_code;
    public byte  pad0;
    public int[] pad = new int[5];
    public int full_sequence;

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
