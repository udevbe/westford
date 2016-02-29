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

import com.github.zubnix.jaccall.CType;
import com.github.zubnix.jaccall.Field;
import com.github.zubnix.jaccall.Struct;

@Struct(union = true,
        value = {
                @Field(name = "data8",
                       type = CType.CHAR,
                       cardinality = 20),
                @Field(name = "data16",
                       type = CType.SHORT,
                       cardinality = 10),
                @Field(name = "data32",
                       type = CType.INT,
                       cardinality = 5),
        })
public final class xcb_client_message_data_t extends xcb_client_message_data_t_Jaccall_StructType {}
