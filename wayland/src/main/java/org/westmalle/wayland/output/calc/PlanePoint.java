package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class PlanePoint {

    public static PlanePoint create(@Nonnull final Vec4 point,
                                          @Nonnull final Plane plane) {
        return new AutoValue_PlanePoint(point,
                                              plane);
    }

    public abstract Vec4 getPoint();

    public abstract Plane getPlane();

    public Optional<Vec4> translateTo(@Nonnull final Plane targetPlane) {
        //TODO unit test

        final Optional<Mat4> mat4Optional = getPlane().getTranslation(targetPlane);
        final Optional<Vec4> result;
        if (mat4Optional.isPresent()) {
            result = Optional.of(mat4Optional.get()
                                             .multiply(getPoint()));
        }
        else {
            result = Optional.empty();
        }
        return result;
    }
}
