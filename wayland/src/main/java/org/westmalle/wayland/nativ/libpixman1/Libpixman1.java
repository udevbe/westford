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

import com.sun.jna.ptr.IntByReference;

import javax.inject.Singleton;

@Singleton
public class Libpixman1 {

    public native pixman_box32 pixman_region32_rectangles(pixman_region32 region,
                                                          IntByReference n_rects);

    public native int pixman_region32_union_rect(pixman_region32 dest,
                                                 pixman_region32 source,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height);

    public native void pixman_region32_init_rect(pixman_region32 region,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height);

    public native int pixman_region32_subtract(pixman_region32 reg_d,
                                               pixman_region32 reg_m,
                                               pixman_region32 reg_s);

    public native int pixman_region32_contains_point(pixman_region32 region,
                                                     int x,
                                                     int y,
                                                     pixman_box32 box);

    public native int pixman_region32_intersect_rect(pixman_region32 dest,
                                                     pixman_region32 source,
                                                     int x,
                                                     int y,
                                                     int width,
                                                     int height);

}
