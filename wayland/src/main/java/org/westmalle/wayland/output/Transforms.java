package org.westmalle.wayland.output;

import org.westmalle.wayland.output.calc.Mat4;

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
                                                       0f, 0f,  0f, 1f);
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
                                                        0f, 0f, 0f, 1f);
    //@formatter:on

    //@formatter:off
    @Nonnull
    public static Mat4 SCALE(@Nonnegative final float scale) {
        return Mat4.create( scale, 0f, 0f, 0f,
                            0f, scale, 0f, 0f,
                            0f, 0f, scale, 0f,
                            0f, 0f, 0f, 1f);
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
