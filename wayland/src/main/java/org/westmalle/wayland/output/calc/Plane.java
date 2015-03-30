package org.westmalle.wayland.output.calc;

import org.westmalle.wayland.output.Point;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

public class Plane {

    private final Map<Plane,Mat4> translations = new WeakHashMap<>();

    public PointTranslation translate(@Nonnull Point sourcePoint){
        return translate(Vec4.create(sourcePoint.getX(),
                                     sourcePoint.getY(),
                                     0,
                                     1));
    }

    public PointTranslation translate(@Nonnull Vec4 source){
        return new PointTranslation(source);
    }

    public Optional<Mat4> getTranslation(@Nonnull Plane target){
        return Optional.ofNullable(this.translations.get(target));
    }

    public void setTranslation(@Nonnull Plane target,
                               @Nonnull Mat4 translation){
        translations.put(target,
                         translation);
        target.translations.put(this,
                                translation.invert());
    }

    public class PointTranslation {

        private final Vec4 point;
        private final Plane plane;

        PointTranslation(final Vec4 point) {
            this.point = point;
            this.plane = Plane.this;
        }

        public Vec4 getPoint() {
            return this.point;
        }

        public Plane getSpace(){
            return this.plane;
        }

        public Optional<Vec4> to(@Nonnull final Plane targetPlane) {
            final Optional<Mat4> mat4Optional = getTranslation(targetPlane);
            final Optional<Vec4> result;
            if(mat4Optional.isPresent()){
                result = Optional.of(mat4Optional.get().multiply(this.point));
            }else{
                result = Optional.empty();
            }
            return result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final PointTranslation that = (PointTranslation) o;

            return getPoint().equals(that.getPoint()) && plane.equals(that.plane);
        }

        @Override
        public int hashCode() {
            int result = getPoint().hashCode();
            result = 31 * result + plane.hashCode();
            return result;
        }
    }
}
