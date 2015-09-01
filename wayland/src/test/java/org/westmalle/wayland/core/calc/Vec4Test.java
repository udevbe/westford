package org.westmalle.wayland.core.calc;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class Vec4Test {


    @Test
    public void testAdd() throws Exception {
        //given
        final Vec4 left = Vec4.create(12,
                                      34,
                                      56,
                                      78);
        final Vec4 right = Vec4.create(87,
                                       65,
                                       43,
                                       21);

        //when
        Vec4 result = left.add(right);

        //then
        assertThat(result).isEqualTo(Vec4.create(99,
                                                 99,
                                                 99,
                                                 99));
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final Vec4 left = Vec4.create(99,
                                      99,
                                      99,
                                      99);

        final Vec4 right = Vec4.create(87,
                                       65,
                                       43,
                                       21);

        //when
        final Vec4 result = left.subtract(right);

        //then
        assertThat(result).isEqualTo(Vec4.create(12,
                                                 34,
                                                 56,
                                                 78));
    }
}