package org.westmalle.wayland.output.calc;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class PlaneLocationTest {

    @Test
    public void testTranslateTo() throws Exception {
        //given
        final Plane plane = new Plane();
        final Vec4 vec4 = Vec4.create(12,
                                      34,
                                      56,
                                      78);
        final PlaneLocation planeLocation = PlaneLocation.create(vec4,
                                                                 plane);

        final Plane other       = new Plane();
        final Mat4  translation = mock(Mat4.class);
        final Vec4  otherVec4   = mock(Vec4.class);
        when(translation.multiply(vec4)).thenReturn(otherVec4);

        plane.setTranslation(other,
                             translation);

        //when
        planeLocation.translateTo(other);

        //then
        verify(translation).multiply(vec4);

    }
}