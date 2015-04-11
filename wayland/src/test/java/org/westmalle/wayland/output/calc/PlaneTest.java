package org.westmalle.wayland.output.calc;

import org.junit.Test;
import org.westmalle.wayland.output.Point;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;


public class PlaneTest {

    @Test
    public void testPoint() throws Exception {
        //given
        final Point point = Point.create(123,
                                         456);
        final Plane plane = new Plane();

        //when
        final PlaneLocation planeLocation = plane.locate(point);

        //then
        assertThat(planeLocation.getLocation()
                                .getX()).isEqualTo(123f);
        assertThat(planeLocation.getLocation()
                                .getY()).isEqualTo(456f);
    }

    @Test
    public void testVector() throws Exception {
        //given
        final Vec4 vec4 = Vec4.create(123,
                                      456,
                                      789,
                                      1011);
        final Plane plane = new Plane();

        //when
        final PlaneLocation planeLocation = plane.locate(vec4);

        //then
        assertThat(planeLocation.getLocation()
                                .getX()).isEqualTo(123f);
        assertThat(planeLocation.getLocation()
                                .getY()).isEqualTo(456f);
    }

    @Test
    public void testGetTranslation() throws Exception {
        //given
        final Plane plane = new Plane();
        final Plane other = new Plane();

        final Mat4 mat4 = Mat4.IDENTITY;

        //when
        plane.setTranslation(other,
                             mat4);

        //then
        final Optional<Mat4> otherMat4 = plane.getTranslation(other);
        assertThat(otherMat4.isPresent());
        assertThat(otherMat4.get()).isEqualTo(mat4);
    }
}