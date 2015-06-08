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

import com.sun.jna.Pointer;
import com.sun.jna.Union;

public class xcb_client_message_data_t extends Union {
    public byte[]  data8  = new byte[20];
    public short[] data16 = new short[10];
    public int[]   data32 = new int[5];

    public xcb_client_message_data_t(final Pointer p) {
        super(p);
    }

    public xcb_client_message_data_t() {
    }

    public static class ByValue extends xcb_client_message_data_t implements Union.ByValue {

        public ByValue(final Pointer p) {
            super(p);
        }

        public ByValue() {
        }
    }
}
