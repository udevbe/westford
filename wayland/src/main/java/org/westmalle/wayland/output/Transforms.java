package org.westmalle.wayland.output;

import com.hackoeur.jglm.Mat4;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class Transforms {
    public static final Mat4 NORMAL      = Mat4.MAT4_IDENTITY;
    public static final Mat4 _90         = new Mat4(0f,
                                                    -1f,
                                                    0f,
                                                    0f,

                                                    1f,
                                                    0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);
    public static final Mat4 _180        = new Mat4(-1f,
                                                    0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    -1f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);
    public static final Mat4 _270        = new Mat4(0f,
                                                    1f,
                                                    0f,
                                                    0f,

                                                    -1f,
                                                    -0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);
    public static final Mat4 FLIPPED     = new Mat4(-1f,
                                                    0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    1f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);
    public static final Mat4 FLIPPED_90  = new Mat4(0f,
                                                    1f,
                                                    0f,
                                                    0f,

                                                    1f,
                                                    0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);
    public static final Mat4 FLIPPED_180 = new Mat4(1f,
                                                    0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    -1f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);
    public static final Mat4 FLIPPED_270 = new Mat4(0f,
                                                    -1f,
                                                    0f,
                                                    0f,

                                                    -1f,
                                                    0f,
                                                    0f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    1f,
                                                    0f,

                                                    0f,
                                                    0f,
                                                    0f,
                                                    1f);

    @Nonnull
    public static Mat4 SCALE(@Nonnegative final int scale) {
        return new Mat4(
                1f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                scale
        );
    }

    public static Mat4 IDENTITY = Mat4.MAT4_IDENTITY;
}
