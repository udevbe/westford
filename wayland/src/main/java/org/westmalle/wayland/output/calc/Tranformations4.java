package org.westmalle.wayland.output.calc;

public class Tranformations4 {
    public static <S extends Space, T extends Space> Mat4<S, T> identity() {
        return Mat4.<S,T>create(
                1.f, 0.f, 0.f, 0.f,
                0.f, 1.f, 0.f, 0.f,
                0.f, 0.f, 1.f, 0.f,
                0.f, 0.f, 0.f, 1.f);
    }

    /**
     * Invert this matrix
     *
     * @return a new matrix who's source and destination are swapped.
     */
    public <S extends Space, T extends Space>Mat4<T, S> invert(final Mat4<S, T> matrix) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }
}
