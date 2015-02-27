package org.westmalle.wayland.output;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnegative;

@AutoValue
public abstract class Rectangle {
    public static Builder builder() {
        return new AutoValue_Rectangle.Builder().x(0).y(0).width(0).height(0);
    }

    public abstract int getX();

    public abstract int getY();

    public abstract int getWidth();

    public abstract int getHeight();

    public Point getPosition(){
        return Point.builder()
                       .x(getX())
                       .y(getY())
                       .build();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder x(int x);

        Builder y(int y);

        Builder width(@Nonnegative int width);

        Builder height(@Nonnegative int height);

        Rectangle build();
    }

    public abstract Builder toBuilder();
}
