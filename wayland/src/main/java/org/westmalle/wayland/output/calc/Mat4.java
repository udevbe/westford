package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

/**
 * @param <S> source space
 * @param <T> target space
 */
@AutoValue
public abstract class Mat4<S extends Space, T extends Space> {

    public static <S extends Space, T extends Space> Mat4<S, T> create(final float[] array,
                                                                       final int offset) {
        if (array.length < 16) {
            throw new IllegalArgumentException("Array length must be >= 16");
        }
        return Mat4.<S, T>builder()
                .m00(array[offset]).m01(array[1 + offset]).m02(array[2 + offset]).m03(array[3 + offset])
                .m10(array[4 + offset]).m11(array[5 + offset]).m12(array[6 + offset]).m13(array[7 + offset])
                .m20(array[8 + offset]).m21(array[9 + offset]).m22(array[10 + offset]).m23(array[11 + offset])
                .m30(array[12 + offset]).m31(array[13 + offset]).m32(array[14 + offset]).m33(array[15] + offset)
                .build();
    }


    /**
     * Construct a new matrix. Arguments are in row major order.
     *
     * @param m00
     * @param m10
     * @param m20
     * @param m30
     * @param m01
     * @param m11
     * @param m21
     * @param m31
     * @param m02
     * @param m12
     * @param m22
     * @param m32
     * @param m03
     * @param m13
     * @param m23
     * @param m33
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S extends Space, T extends Space> Mat4<S, T> create(final float m00, final float m10, final float m20, final float m30,
                                                                       final float m01, final float m11, final float m21, final float m31,
                                                                       final float m02, final float m12, final float m22, final float m32,
                                                                       final float m03, final float m13, final float m23, final float m33) {
        return Mat4.<S, T>builder()
                .m00(m00).m10(m10).m20(m20).m30(m30)
                .m01(m01).m11(m11).m21(m21).m31(m31)
                .m02(m02).m12(m12).m22(m22).m32(m32)
                .m03(m03).m13(m13).m23(m23).m33(m33)
                .build();
    }

    /**
     * @param array expected format is column major.
     */
    public static <S extends Space, T extends Space> Mat4<S, T> create(final float[] array) {
        return Mat4.<S, T>create(array, 0);
    }

    public static <S extends Space, T extends Space> Builder<S, T> builder() {
        return new AutoValue_Mat4.Builder<S, T>()
                .m00(0.f).m10(0.f).m20(0.f).m30(0.f)
                .m01(0.f).m11(0.f).m21(0.f).m31(0.f)
                .m02(0.f).m12(0.f).m22(0.f).m32(0.f)
                .m03(0.f).m13(0.f).m23(0.f).m33(0.f);
    }

    /**
     * Column 0, Row 0
     *
     * @return
     */
    public abstract float getM00();

    /**
     * Column 0, Row 1
     *
     * @return
     */
    public abstract float getM01();

    /**
     * Column 0, Row 2
     *
     * @return
     */
    public abstract float getM02();

    /**
     * Column 0, Row 3
     *
     * @return
     */
    public abstract float getM03();

    /**
     * Column 1, Row 0
     *
     * @return
     */
    public abstract float getM10();

    /**
     * Column 1, Row 1
     *
     * @return
     */
    public abstract float getM11();

    /**
     * Column 1, Row 2
     *
     * @return
     */
    public abstract float getM12();

    /**
     * Column 1, Row 3
     *
     * @return
     */
    public abstract float getM13();

    /**
     * Column 2, Row 0
     *
     * @return
     */
    public abstract float getM20();

    /**
     * Column 2, Row 1
     *
     * @return
     */
    public abstract float getM21();

    /**
     * Column 2, Row 2
     *
     * @return
     */
    public abstract float getM22();

    /**
     * Column 2, Row 3
     *
     * @return
     */
    public abstract float getM23();

    /**
     * Column 3, Row 0
     *
     * @return
     */
    public abstract float getM30();

    /**
     * Column 3, Row 1
     *
     * @return
     */
    public abstract float getM31();

    /**
     * Column 3, Row 2
     *
     * @return
     */
    public abstract float getM32();

    /**
     * Column 3, Row 3
     *
     * @return
     */
    public abstract float getM33();

    public abstract Builder<S, T> toBuilder();

    /**
     * Array representation of this matrix. Array is in column major order.
     * index 0 -> column 0, row 0; index 1 -> column 0, row 1 and so forth.
     *
     * @return a new array of length 16, in column major order
     */
    public float[] toArray() {
        return new float[]{
                getM00(), getM01(), getM02(), getM03(),
                getM10(), getM11(), getM12(), getM13(),
                getM20(), getM21(), getM22(), getM23(),
                getM30(), getM31(), getM32(), getM33(),
        };
    }

    @AutoValue.Builder
    public interface Builder<S extends Space, T extends Space> {

        Builder<S, T> m00(float element);

        Builder<S, T> m01(float element);

        Builder<S, T> m02(float element);

        Builder<S, T> m03(float element);

        Builder<S, T> m10(float element);

        Builder<S, T> m11(float element);

        Builder<S, T> m12(float element);

        Builder<S, T> m13(float element);

        Builder<S, T> m20(float element);

        Builder<S, T> m21(float element);

        Builder<S, T> m22(float element);

        Builder<S, T> m23(float element);

        Builder<S, T> m30(float element);

        Builder<S, T> m31(float element);

        Builder<S, T> m32(float element);

        Builder<S, T> m33(float element);

        Mat4<S, T> build();
    }

    /**
     * Transform vector in source plane to destination plane.
     *
     * @param right right hand side of the multiplication
     * @return new vector who's space is this matrix' destination space.
     */
    public Vec4<T> multiply(final Vec4<S> right) {
        //TODO unit test

        final float rightX = right.getX();
        final float rightY = right.getY();
        final float rightZ = right.getZ();
        final float rightW = right.getW();

        return Vec4.<T>create(
                getM00() * rightX + getM10() * rightY + getM20() * rightZ + getM30() * rightW,
                getM01() * rightX + getM11() * rightY + getM21() * rightZ + getM31() * rightW,
                getM02() * rightX + getM12() * rightY + getM22() * rightZ + getM32() * rightW,
                getM03() * rightX + getM13() * rightY + getM23() * rightZ + getM33() * rightW
        );
    }

    /**
     * Transform matrix in source plane to destination plane.
     *
     * @param right right hand side of the multiplication
     * @param <U>   argument destination space
     * @return new matrix who's source is this matrix' destination space.
     */
    public <U extends Space> Mat4<T, U> multiply(final Mat4<S, U> right) {
        //TODO unit tests

        return Mat4.create(
                getM00() * right.getM00() + getM10() * right.getM01() + getM20() * right.getM02() + getM30() * right.getM03(),
                getM00() * right.getM10() + getM10() * right.getM11() + getM20() * right.getM12() + getM30() * right.getM13(),
                getM00() * right.getM20() + getM10() * right.getM21() + getM20() * right.getM22() + getM30() * right.getM23(),
                getM00() * right.getM30() + getM10() * right.getM31() + getM20() * right.getM32() + getM30() * right.getM33(),

                getM01() * right.getM00() + getM11() * right.getM01() + getM21() * right.getM02() + getM31() * right.getM03(),
                getM01() * right.getM10() + getM11() * right.getM11() + getM21() * right.getM12() + getM31() * right.getM13(),
                getM01() * right.getM20() + getM11() * right.getM21() + getM21() * right.getM22() + getM31() * right.getM23(),
                getM01() * right.getM30() + getM11() * right.getM31() + getM21() * right.getM32() + getM31() * right.getM33(),

                getM02() * right.getM00() + getM12() * right.getM01() + getM22() * right.getM02() + getM32() * right.getM03(),
                getM02() * right.getM10() + getM12() * right.getM11() + getM22() * right.getM12() + getM32() * right.getM13(),
                getM02() * right.getM20() + getM12() * right.getM21() + getM22() * right.getM22() + getM32() * right.getM23(),
                getM02() * right.getM30() + getM12() * right.getM31() + getM22() * right.getM32() + getM32() * right.getM33(),

                getM03() * right.getM00() + getM13() * right.getM01() + getM23() * right.getM02() + getM33() * right.getM03(),
                getM03() * right.getM10() + getM13() * right.getM11() + getM23() * right.getM12() + getM33() * right.getM13(),
                getM03() * right.getM20() + getM13() * right.getM21() + getM23() * right.getM22() + getM33() * right.getM23(),
                getM03() * right.getM30() + getM13() * right.getM31() + getM23() * right.getM32() + getM33() * right.getM33());
    }

    /**
     * Copy this matrix to a matrix with a different source and destination space.
     *
     * @param <U>
     * @param <V>
     * @return
     */
    public <U extends Space, V extends Space> Mat4<U, V> copy() {
        return Mat4.<U, V>create(toArray());
    }
}
