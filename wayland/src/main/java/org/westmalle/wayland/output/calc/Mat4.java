package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class Mat4 {

    public static final Mat4 IDENTITY = Mat4.create(1.f,
                                                    0.f,
                                                    0.f,
                                                    0.f,
                                                    0.f,
                                                    1.f,
                                                    0.f,
                                                    0.f,
                                                    0.f,
                                                    0.f,
                                                    1.f,
                                                    0.f,
                                                    0.f,
                                                    0.f,
                                                    0.f,
                                                    1.f);

    /**
     * Construct a new matrix. Array elements should be in column major order.
     *
     * @return a new 4 by 4 matrix.
     */
    public static Mat4 create(@Nonnull final float[] array,
                              @Nonnegative final int offset) {
        checkArgument(array.length >= 16,
                      "Array length must be >= 16");
        checkArgument(offset >= 0,
                      "Offset must be >= 0");
        return Mat4.builder()
                   .m00(array[offset])
                   .m01(array[1 + offset])
                   .m02(array[2 + offset])
                   .m03(array[3 + offset])
                   .m10(array[4 + offset])
                   .m11(array[5 + offset])
                   .m12(array[6 + offset])
                   .m13(array[7 + offset])
                   .m20(array[8 + offset])
                   .m21(array[9 + offset])
                   .m22(array[10 + offset])
                   .m23(array[11 + offset])
                   .m30(array[12 + offset])
                   .m31(array[13 + offset])
                   .m32(array[14 + offset])
                   .m33(array[15] + offset)
                   .build();
    }

    /**
     * Construct a new matrix. Arguments are in column major order.
     *
     * @param m00 column 0, row 0
     * @param m01 column 0, row 1
     * @param m02 column 0, row 2
     * @param m03 column 0, row 3
     * @param m10 column 1, row 0
     * @param m11 column 1, row 1
     * @param m12 column 1, row 2
     * @param m13 column 1, row 3
     * @param m20 column 2, row 0
     * @param m21 column 2, row 1
     * @param m22 column 2, row 2
     * @param m23 column 2, row 3
     * @param m30 column 3, row 0
     * @param m31 column 3, row 1
     * @param m32 column 3, row 2
     * @param m33 column 3, row 3
     *
     * @return a new 4 by 4 matrix.
     */
    public static Mat4 create(final float m00,
                              final float m01,
                              final float m02,
                              final float m03,

                              final float m10,
                              final float m11,
                              final float m12,
                              final float m13,

                              final float m20,
                              final float m21,
                              final float m22,
                              final float m23,

                              final float m30,
                              final float m31,
                              final float m32,
                              final float m33) {
        return Mat4.builder()
                   .m00(m00)
                   .m10(m10)
                   .m20(m20)
                   .m30(m30)
                   .m01(m01)
                   .m11(m11)
                   .m21(m21)
                   .m31(m31)
                   .m02(m02)
                   .m12(m12)
                   .m22(m22)
                   .m32(m32)
                   .m03(m03)
                   .m13(m13)
                   .m23(m23)
                   .m33(m33)
                   .build();
    }

    /**
     * @param array expected format is column major. Index 0 -> column 0, row 0; index 1 -> column 0, row 1 and so forth.
     */
    public static Mat4 create(@Nonnull final float[] array) {
        return Mat4.create(array,
                           0);
    }

    public static Builder builder() {
        return new AutoValue_Mat4.Builder()
                .m00(0.f)
                .m10(0.f)
                .m20(0.f)
                .m30(0.f)
                .m01(0.f)
                .m11(0.f)
                .m21(0.f)
                .m31(0.f)
                .m02(0.f)
                .m12(0.f)
                .m22(0.f)
                .m32(0.f)
                .m03(0.f)
                .m13(0.f)
                .m23(0.f)
                .m33(0.f);
    }

    /**
     * @return Column 0, Row 0
     */
    public abstract float getM00();

    /**
     * @return Column 0, Row 1
     */
    public abstract float getM01();

    /**
     * @return Column 0, Row 2
     */
    public abstract float getM02();

    /**
     * @return Column 0, Row 3
     */
    public abstract float getM03();

    /**
     * @return Column 1, Row 0
     */
    public abstract float getM10();

    /**
     * @return Column 1, Row 1
     */
    public abstract float getM11();

    /**
     * @return Column 1, Row 2
     */
    public abstract float getM12();

    /**
     * @return Column 1, Row 3
     */
    public abstract float getM13();

    /**
     * @return Column 2, Row 0
     */
    public abstract float getM20();

    /**
     * @return Column 2, Row 1
     */
    public abstract float getM21();

    /**
     * @return Column 2, Row 2
     */
    public abstract float getM22();

    /**
     * @return Column 2, Row 3
     */
    public abstract float getM23();

    /**
     * @return Column 3, Row 0
     */
    public abstract float getM30();

    /**
     * @return Column 3, Row 1
     */
    public abstract float getM31();

    /**
     * @return Column 3, Row 2
     */
    public abstract float getM32();

    /**
     * @return Column 3, Row 3
     */
    public abstract float getM33();

    public abstract Builder toBuilder();

    public Mat4 add(final Mat4 other) {
        return Mat4.create(getM00() + other.getM00(),
                           getM01() + other.getM01(),
                           getM02() + other.getM02(),
                           getM03() + other.getM03(),
                           getM10() + other.getM10(),
                           getM11() + other.getM11(),
                           getM12() + other.getM12(),
                           getM13() + other.getM13(),
                           getM20() + other.getM20(),
                           getM21() + other.getM21(),
                           getM22() + other.getM22(),
                           getM23() + other.getM23(),
                           getM30() + other.getM30(),
                           getM31() + other.getM31(),
                           getM32() + other.getM32(),
                           getM33() + other.getM33());
    }

    @AutoValue.Builder
    public interface Builder {

        Builder m00(float element);

        Builder m01(float element);

        Builder m02(float element);

        Builder m03(float element);

        Builder m10(float element);

        Builder m11(float element);

        Builder m12(float element);

        Builder m13(float element);

        Builder m20(float element);

        Builder m21(float element);

        Builder m22(float element);

        Builder m23(float element);

        Builder m30(float element);

        Builder m31(float element);

        Builder m32(float element);

        Builder m33(float element);

        Mat4 build();
    }

    /**
     * Transform vector in point plane to destination plane.
     *
     * @param right right hand side of the multiplication
     *
     * @return new vector who's space is this matrix' destination space.
     */
    public Vec4 multiply(final Vec4 right) {
        //TODO unit test

        final float rightX = right.getX();
        final float rightY = right.getY();
        final float rightZ = right.getZ();
        final float rightW = right.getW();

        return Vec4.create(
                getM00() * rightX + getM10() * rightY + getM20() * rightZ + getM30() * rightW,
                getM01() * rightX + getM11() * rightY + getM21() * rightZ + getM31() * rightW,
                getM02() * rightX + getM12() * rightY + getM22() * rightZ + getM32() * rightW,
                getM03() * rightX + getM13() * rightY + getM23() * rightZ + getM33() * rightW
                          );
    }

    /**
     * Transform matrix in point plane to destination plane.
     *
     * @param right right hand side of the multiplication
     *
     * @return new matrix who's point is this matrix' destination space.
     */
    public Mat4 multiply(final Mat4 right) {
        //TODO unit test

        final float nm00 = this.getM00() * right.getM00() + this.getM10() * right.getM01() + this.getM20() * right.getM02() + this.getM30() * right.getM03();
        final float nm01 = this.getM01() * right.getM00() + this.getM11() * right.getM01() + this.getM21() * right.getM02() + this.getM31() * right.getM03();
        final float nm02 = this.getM02() * right.getM00() + this.getM12() * right.getM01() + this.getM22() * right.getM02() + this.getM32() * right.getM03();
        final float nm03 = this.getM03() * right.getM00() + this.getM13() * right.getM01() + this.getM23() * right.getM02() + this.getM33() * right.getM03();
        final float nm10 = this.getM00() * right.getM10() + this.getM10() * right.getM11() + this.getM20() * right.getM12() + this.getM30() * right.getM13();
        final float nm11 = this.getM01() * right.getM10() + this.getM11() * right.getM11() + this.getM21() * right.getM12() + this.getM31() * right.getM13();
        final float nm12 = this.getM02() * right.getM10() + this.getM12() * right.getM11() + this.getM22() * right.getM12() + this.getM32() * right.getM13();
        final float nm13 = this.getM03() * right.getM10() + this.getM13() * right.getM11() + this.getM23() * right.getM12() + this.getM33() * right.getM13();
        final float nm20 = this.getM00() * right.getM20() + this.getM10() * right.getM21() + this.getM20() * right.getM22() + this.getM30() * right.getM23();
        final float nm21 = this.getM01() * right.getM20() + this.getM11() * right.getM21() + this.getM21() * right.getM22() + this.getM31() * right.getM23();
        final float nm22 = this.getM02() * right.getM20() + this.getM12() * right.getM21() + this.getM22() * right.getM22() + this.getM32() * right.getM23();
        final float nm23 = this.getM03() * right.getM20() + this.getM13() * right.getM21() + this.getM23() * right.getM22() + this.getM33() * right.getM23();
        final float nm30 = this.getM00() * right.getM30() + this.getM10() * right.getM31() + this.getM20() * right.getM32() + this.getM30() * right.getM33();
        final float nm31 = this.getM01() * right.getM30() + this.getM11() * right.getM31() + this.getM21() * right.getM32() + this.getM31() * right.getM33();
        final float nm32 = this.getM02() * right.getM30() + this.getM12() * right.getM31() + this.getM22() * right.getM32() + this.getM32() * right.getM33();
        final float nm33 = this.getM03() * right.getM30() + this.getM13() * right.getM31() + this.getM23() * right.getM32() + this.getM33() * right.getM33();

        return Mat4.create(nm00,
                           nm01,
                           nm02,
                           nm03,
                           nm10,
                           nm11,
                           nm12,
                           nm13,
                           nm20,
                           nm21,
                           nm22,
                           nm23,
                           nm30,
                           nm31,
                           nm32,
                           nm33);
    }

    /**
     * Invert this matrix
     *
     * @return a new matrix who's point and destination are swapped.
     */
    public Mat4 invert() {
        //TODO unit test

        final float[][] matrix2d = new float[][]{{getM00(), getM10(), getM20(), getM30()},
                                                 {getM01(), getM11(), getM21(), getM31()},
                                                 {getM02(), getM12(), getM22(), getM32()},
                                                 {getM03(), getM13(), getM23(), getM33()}};
        //FIXME test uninvertable matrix, what will/should happen?
        final float[][] matrix2dInverted = invert(matrix2d);
        return Mat4.create(matrix2dInverted[0][0],
                           matrix2dInverted[0][1],
                           matrix2dInverted[0][2],
                           matrix2dInverted[0][3],
                           matrix2dInverted[1][0],
                           matrix2dInverted[1][1],
                           matrix2dInverted[1][2],
                           matrix2dInverted[1][3],
                           matrix2dInverted[2][0],
                           matrix2dInverted[2][1],
                           matrix2dInverted[2][2],
                           matrix2dInverted[2][3],
                           matrix2dInverted[3][0],
                           matrix2dInverted[3][1],
                           matrix2dInverted[3][2],
                           matrix2dInverted[3][3]);
    }

    private float[][] invert(final float[][] a) {
        final int n = a.length;
        final float[][] x = new float[n][n];
        final float[][] b = new float[n][n];
        final int[] index = new int[n];
        for (int i = 0; i < n; ++i) { b[i][i] = 1; }

        gaussian(a,
                 index);

        for (int i = 0; i < n - 1; ++i) {
            for (int j = i + 1; j < n; ++j) {
                for (int k = 0; k < n; ++k) {
                    b[index[j]][k]
                            -= a[index[j]][i] * b[index[i]][k];
                }
            }
        }

        for (int i = 0; i < n; ++i) {
            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];
            for (int j = n - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < n; ++k) {
                    x[j][i] -= a[index[j]][k] * x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }

    private void gaussian(final float[][] a,
                          final int[] index) {
        final int n = index.length;
        final float[] c = new float[n];

        for (int i = 0; i < n; ++i) { index[i] = i; }

        for (int i = 0; i < n; ++i) {
            float c1 = 0;
            for (int j = 0; j < n; ++j) {
                final float c0 = Math.abs(a[i][j]);
                if (c0 > c1) { c1 = c0; }
            }
            c[i] = c1;
        }

        int k = 0;
        for (int j = 0; j < n - 1; ++j) {
            float pi1 = 0;
            for (int i = j; i < n; ++i) {
                float pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) {
                    pi1 = pi0;
                    k = i;
                }
            }

            final int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < n; ++i) {
                final float pj = a[index[i]][j] / a[index[j]][j];

                a[index[i]][j] = pj;

                // Modify other elements accordingly
                for (int l = j + 1; l < n; ++l) { a[index[i]][l] -= pj * a[index[j]][l]; }
            }
        }
    }

    public FloatBuffer toBuffer() {
        //TODO unit test

        final FloatBuffer buffer = FloatBuffer.allocate(16);
        buffer.put(getM00())
              .put(getM01())
              .put(getM02())
              .put(getM03());
        buffer.put(getM10())
              .put(getM11())
              .put(getM12())
              .put(getM13());
        buffer.put(getM20())
              .put(getM21())
              .put(getM22())
              .put(getM23());
        buffer.put(getM30())
              .put(getM31())
              .put(getM32())
              .put(getM33());
        buffer.rewind();
        return buffer;
    }
}
