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

public class xcb_screen_t extends Structure{

    private static final List<?> FIELD_ORDER = Arrays.asList("root",
                                                             "default_colormap",
                                                             "white_pixel",
                                                             "black_pixel",
                                                             "current_input_masks",
                                                             "width_in_pixels",
                                                             "height_in_pixels",
                                                             "width_in_millimeters",
                                                             "height_in_millimeters",
                                                             "min_installed_maps",
                                                             "max_installed_maps",
                                                             "root_visual",
                                                             "backing_stores",
                                                             "save_unders",
                                                             "root_depth",
                                                             "allowed_depths_len");
    public int root;
    public int default_colormap;
    public int white_pixel;
    public int black_pixel;
    public int current_input_masks;
    public short width_in_pixels;
    public short height_in_pixels;
    public short width_in_millimeters;
    public short height_in_millimeters;
    public short min_installed_maps;
    public short max_installed_maps;
    public int root_visual;
    public byte backing_stores;
    public byte save_unders;
    public byte root_depth;
    public byte allowed_depths_len;

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }

    public static class ByReference extends xcb_screen_t implements Structure.ByReference{

    }
}
