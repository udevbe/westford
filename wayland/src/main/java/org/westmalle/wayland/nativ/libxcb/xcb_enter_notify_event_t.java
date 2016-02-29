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

@Struct({
                @Field(name = "response_type",
                       type = CType.CHAR),
                @Field(name = "detail",
                       type = CType.CHAR),
                @Field(name = "sequence",
                       type = CType.SHORT),
                @Field(name = "time",
                       type = CType.INT),
                @Field(name = "root",
                       type = CType.INT),
                @Field(name = "event",
                       type = CType.INT),
                @Field(name = "child",
                       type = CType.INT),
                @Field(name = "root_x",
                       type = CType.SHORT),
                @Field(name = "root_y",
                       type = CType.SHORT),
                @Field(name = "event_x",
                       type = CType.SHORT),
                @Field(name = "event_y",
                       type = CType.SHORT),
                @Field(name = "state",
                       type = CType.SHORT),
                @Field(name = "mode",
                       type = CType.CHAR),
                @Field(name = "same_scree_focus",
                       type = CType.CHAR),
        })
public final class xcb_enter_notify_event_t extends xcb_enter_notify_event_t_Jaccall_StructType {}
