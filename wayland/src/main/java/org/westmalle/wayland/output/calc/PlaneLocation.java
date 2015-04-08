package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class PlaneLocation {

    public static PlaneLocation create(@Nonnull final Vec4 point,
                                       @Nonnull final Plane plane) {
        return new AutoValue_PlaneLocation(point,
                                           plane);
    }

    public abstract Vec4 getLocation();

    public abstract Plane getPlane();

    public Optional<PlaneLocation> translateTo(@Nonnull final Plane targetPlane) {
        //TODO unit test

        final Optional<Mat4> mat4Optional = getPlane().getTranslation(targetPlane);
        final Optional<PlaneLocation> result;
        if (mat4Optional.isPresent()) {
            result = Optional.of(targetPlane.locate(mat4Optional.get()
                                                                .multiply(getLocation())));
        }
        else {
            result = Optional.empty();
        }
        return result;
    }
}
