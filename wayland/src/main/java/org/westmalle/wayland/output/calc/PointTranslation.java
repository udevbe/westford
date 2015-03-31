package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

import java.util.Optional;

import javax.annotation.Nonnull;

@AutoValue
public abstract class PointTranslation {

    public static PointTranslation create(@Nonnull final Vec4 point,
                                          @Nonnull final Plane plane){
        return new AutoValue_PointTranslation(point,
                                              plane);
    }

    public abstract Vec4 getPoint();

    public abstract Plane getPlane();

    public Optional<Vec4> to(@Nonnull final Plane targetPlane) {
        final Optional<Mat4> mat4Optional = getPlane().getTranslation(targetPlane);
        final Optional<Vec4> result;
        if (mat4Optional.isPresent()) {
            result = Optional.of(mat4Optional.get()
                                             .multiply(getPoint()));
        } else {
            result = Optional.empty();
        }
        return result;
    }
}
