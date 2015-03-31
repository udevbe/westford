package org.westmalle.wayland.output.calc;

import org.westmalle.wayland.output.Point;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

public class Plane {

    private final Map<Plane,Mat4> translations = new WeakHashMap<>();

    public PointTranslation translate(@Nonnull final Point sourcePoint){
        return translate(Vec4.create(sourcePoint.getX(),
                                     sourcePoint.getY(),
                                     0,
                                     1));
    }

    public PointTranslation translate(@Nonnull final Vec4 source){
        return PointTranslation.create(source,
                                       this);
    }

    public Optional<Mat4> getTranslation(@Nonnull final Plane target){
        return Optional.ofNullable(this.translations.get(target));
    }

    public void setTranslation(@Nonnull final Plane target,
                               @Nonnull final Mat4 translation){
        translations.put(target,
                         translation);
        target.translations.put(this,
                                translation.invert());
    }
}