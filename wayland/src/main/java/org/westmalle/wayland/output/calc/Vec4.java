package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

/**
 * @param <S> source space
 */
@AutoValue
public abstract class Vec4<S extends Space> {

    public static <S extends Space> Vec4<S> create(final float[] array) {
        return Vec4.<S>create(array, 0);
    }

    public static <S extends Space> Vec4<S> create(final float[] array,
                                                   final int offset) {
        if (array.length < 4) {
            throw new IllegalArgumentException("Array length must be >= 4");
        }
        return Vec4.<S>builder()
                .x(array[offset])
                .y(array[1 + offset])
                .z(array[2 + offset])
                .w(array[3 + offset])
                .build();
    }

    public static <S extends Space> Vec4<S> create(final float x,
                                                   final float y,
                                                   final float z,
                                                   final float w) {
        return Vec4.<S>builder().x(x).y(y).z(z).w(w).build();
    }

    public static <S extends Space> Builder<S> builder() {
        return new AutoValue_Vec4.Builder<S>().x(0).y(0).z(0).w(0);
    }

    public abstract float getX();

    public abstract float getY();

    public abstract float getZ();

    public abstract float getW();

    public abstract Builder<S> toBuilder();

    /**
     * @return a new array of length 4
     */
    public float[] toArray() {
        return new float[]{
                getX(),
                getY(),
                getZ(),
                getW()
        };
    }

    @AutoValue.Builder
    public interface Builder<S extends Space> {

        Builder<S> x(float x);

        Builder<S> y(float y);

        Builder<S> z(float z);

        Builder<S> w(float z);

        Vec4<S> build();
    }

    /**
     *
     * @param right
     * @return
     */
    public Vec4<S> add(final Vec4<S> right) {
        return Vec4.<S>create(getX() + right.getX(),
                              getY() + right.getY(),
                              getZ() + right.getZ(),
                              getW() + right.getW());
    }

    /**
     *
     * @param right
     * @return
     */
    public Vec4<S> subtract(final Vec4<S> right) {
        return Vec4.<S>create(getX() - right.getX(),
                              getY() - right.getY(),
                              getZ() - right.getZ(),
                              getW() - right.getW());
    }
}
