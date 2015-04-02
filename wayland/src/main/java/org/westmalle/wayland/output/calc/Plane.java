package org.westmalle.wayland.output.calc;

import org.westmalle.wayland.output.Point;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

public class Plane {

    private final Map<Plane, Mat4> translations = new WeakHashMap<>();

    public PlanePoint point(@Nonnull final Point sourcePoint) {
        //TODO unit test

        return point(Vec4.create(sourcePoint.getX(),
                                 sourcePoint.getY(),
                                 0,
                                 1));
    }

    public PlanePoint point(@Nonnull final Vec4 source) {
        //TODO unit test

        return PlanePoint.create(source,
                                 this);
    }

    public Optional<Mat4> getTranslation(@Nonnull final Plane target) {
        //TODO unit test

        return Optional.ofNullable(this.translations.get(target));
    }

    public void setTranslation(@Nonnull final Plane target,
                               @Nonnull final Mat4 translation) {
        //TODO unit test

        this.translations.put(target,
                              translation);
        target.translations.put(this,
                                translation.invert());
    }
}