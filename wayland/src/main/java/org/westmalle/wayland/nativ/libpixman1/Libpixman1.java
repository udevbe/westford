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

import com.github.zubnix.jaccall.Lib;
import com.github.zubnix.jaccall.Ptr;

import javax.inject.Singleton;

@Singleton
@Lib("pixman-1")
public class Libpixman1 {

    @Ptr
    public native long pixman_region32_rectangles(@Ptr long region,
                                                  @Ptr long n_rects);

    public native int pixman_region32_union_rect(@Ptr long dest,
                                                 @Ptr long source,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height);


    public native void pixman_region32_init_rect(@Ptr long region,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height);

    public native int pixman_region32_subtract(@Ptr long reg_d,
                                               @Ptr long reg_m,
                                               @Ptr long reg_s);

    public native int pixman_region32_contains_point(@Ptr long region,
                                                     int x,
                                                     int y,
                                                     @Ptr long box);

    public native int pixman_region32_intersect_rect(@Ptr long dest,
                                                     @Ptr long source,
                                                     int x,
                                                     int y,
                                                     int width,
                                                     int height);

    public native void pixman_region32_init(@Ptr long region);
}
