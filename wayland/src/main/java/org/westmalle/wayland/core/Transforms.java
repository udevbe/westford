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
package org.westmalle.wayland.core;

import org.westmalle.wayland.core.calc.Mat4;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class Transforms {

    @Nonnull
    public static final Mat4 NORMAL      = Mat4.IDENTITY;
    //@formatter:off
    @Nonnull
    public static final Mat4 _90         = Mat4.create(0f, -1f, 0f, 0f,
                                                       1f,  0f, 0f, 0f,
                                                       0f,  0f, 1f, 0f,
                                                       0f,  0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static final Mat4 _180        = Mat4.create(-1f,  0f, 0f, 0f,
                                                        0f, -1f, 0f, 0f,
                                                        0f,  0f, 1f, 0f,
                                                        0f,  0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static final Mat4 _270        = Mat4.create( 0f,  1f, 0f, 0f,
                                                       -1f, -0f, 0f, 0f,
                                                        0f,  0f, 1f, 0f,
                                                        0f,  0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static final Mat4 FLIPPED     = Mat4.create(-1f, 0f, 0f, 0f,
                                                        0f, 1f, 0f, 0f,
                                                        0f, 0f, 1f, 0f,
                                                        0f, 0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static final Mat4 FLIPPED_90  = Mat4.create(0f, 1f, 0f, 0f,
                                                       1f, 0f, 0f, 0f,
                                                       0f, 0f, 1f, 0f,
                                                       0f, 0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static final Mat4 FLIPPED_180 = Mat4.create( 1f,  0f, 0f, 0f,
                                                        0f, -1f, 0f, 0f,
                                                        0f,  0f, 1f, 0f,
                                                        0f,  0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static final Mat4 FLIPPED_270 = Mat4.create( 0f, -1f, 0f, 0f,
                                                       -1f,  0f, 0f, 0f,
                                                        0f,  0f, 1f, 0f,
                                                        0f,  0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static Mat4 SCALE(@Nonnegative final float scale) {
        return Mat4.create( scale, 0f,    0f,    0f,
                            0f,    scale, 0f,    0f,
                            0f,    0f,    scale, 0f,
                            0f,    0f,    0f,    1f);
    }
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static Mat4 TRANSLATE(final int x,
                                 final int y) {
        return Mat4.create( 1f, 0f, 0f, x,
                            0f, 1f, 0f, y,
                            0f, 0f, 1f, 0f,
                            0f, 0f, 0f, 1f);
    }
    //@formatter:on
}
