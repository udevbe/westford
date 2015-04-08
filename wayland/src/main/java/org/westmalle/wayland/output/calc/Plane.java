package org.westmalle.wayland.output.calc;

import org.westmalle.wayland.output.Point;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class Plane {

    private final Map<Plane, Mat4> translations = new WeakHashMap<>();

    @Nonnull
    public PlaneLocation locate(@Nonnull final Point point) {
        return locate(Vec4.create(point.getX(),
                                  point.getY(),
                                  0,
                                  1));
    }

    @Nonnull
    public PlaneLocation locate(@Nonnull final Vec4 vec4) {
        return PlaneLocation.create(vec4,
                                    this);
    }

    @Nonnull
    public Optional<Mat4> getTranslation(@Nonnull final Plane target) {
        return Optional.ofNullable(this.translations.get(target));
    }

    public void setTranslation(@Nonnull final Plane target,
                               @Nonnull final Mat4 translation) {
        this.translations.put(target,
                              translation);
        target.translations.put(this,
                                translation.invert());
    }
}