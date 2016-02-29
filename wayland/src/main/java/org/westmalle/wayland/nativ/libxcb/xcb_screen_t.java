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
                @Field(name = "root",
                       type = CType.INT),
                @Field(name = "default_colormap",
                       type = CType.INT),
                @Field(name = "white_pixel",
                       type = CType.INT),
                @Field(name = "black_pixel",
                       type = CType.INT),
                @Field(name = "current_input_masks",
                       type = CType.INT),
                @Field(name = "width_in_pixels",
                       type = CType.SHORT),
                @Field(name = "hight_in_pixels",
                       type = CType.SHORT),
                @Field(name = "width_in_millimeters",
                       type = CType.SHORT),
                @Field(name = "height_in_millimeters",
                       type = CType.SHORT),
                @Field(name = "min_installed_maps",
                       type = CType.SHORT),
                @Field(name = "max_installed_maps",
                       type = CType.SHORT),
                @Field(name = "root_visual",
                       type = CType.INT),
                @Field(name = "backing_stores",
                       type = CType.CHAR),
                @Field(name = "save_unders",
                       type = CType.CHAR),
                @Field(name = "root_depth",
                       type = CType.CHAR),
                @Field(name = "allowed_depths_len",
                       type = CType.CHAR),
        })
public final class xcb_screen_t extends xcb_screen_t_Jaccall_StructType {}

