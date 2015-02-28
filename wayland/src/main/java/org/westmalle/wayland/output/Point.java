package org.westmalle.wayland.output;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Point {

    public static final Point ZERO = builder().build();

    public static Point create(final int x,
                               final int y) {
        return builder().x(x)
                        .y(y)
                        .build();
    }

    public static Builder builder() {
        return new AutoValue_Point.Builder().x(0)
                                            .y(0);
    }

    public abstract int getX();

    public abstract int getY();

    public Point add(final Point right) {
        return Point.create(getX() + right.getX(),
                            getY() + right.getY());
    }

    public Point subtract(final Point right) {
        return Point.create(getX() - right.getX(),
                            getY() - right.getY());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder x(int x);

        Builder y(int y);

        Point build();
    }

    public abstract Builder toBuilder();
}
