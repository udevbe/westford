//Copyright 2016 Erik De Rijcke
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
