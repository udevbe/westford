package org.westmalle.wayland.output;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Point {

    public static Builder builder() {
        return new AutoValue_Point.Builder().x(0).y(0);
    }

    public abstract int getX();

    public abstract int getY();

    public Point add(Point right){
        return Point.builder().x(getX() + right.getX()).y(getY() + right.getY()).build();
    }

    public Point subtract(Point right){
        return Point.builder().x(getX() - right.getX()).y(getY() - right.getY()).build();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder x(int x);

        Builder y(int y);

        Point build();
    }

    public abstract Builder toBuilder();
}
