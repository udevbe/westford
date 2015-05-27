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
package org.westmalle.wayland.nativ;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;


public class pixman_box32 extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("x1",
                                                             "y1",
                                                             "x2",
                                                             "y2");

    public int x1;
    public int y1;
    public int x2;
    public int y2;

    public pixman_box32() {
        super();
    }

    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
