//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.compositor.core.calc;

import com.google.common.collect.Range;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class Mat4Test {

    @Test
    public void testAdd() throws Exception {
        //given
        //@formatter:off
        final Mat4 left = Mat4.create(  11f, 22f,   33f, 44f,
                                        55f, 66f,   77f, 88f,
                                       -.5f, -1f, -1.5f, -2f,
                                      -2.5f, -3f, -3.5f, -4f);
        //@formatter:on
        //@formatter:off
        final Mat4 right = Mat4.create(  9f, 8f,   7f, 6f,
                                         5f, 4f,   3f, 2f,
                                        .5f, 1f, 1.5f, 2f,
                                       2.5f, 3f, 3.5f, 4f);
        //@formatter:on
        //when
        final Mat4 result = left.add(right);

        //then
        assertThat(result.getM00()).isEqualTo(20f);
        assertThat(result.getM10()).isEqualTo(30f);
        assertThat(result.getM20()).isEqualTo(40f);
        assertThat(result.getM30()).isEqualTo(50f);

        assertThat(result.getM01()).isEqualTo(60f);
        assertThat(result.getM11()).isEqualTo(70f);
        assertThat(result.getM21()).isEqualTo(80f);
        assertThat(result.getM31()).isEqualTo(90f);

        assertThat(result.getM02()).isEqualTo(0f);
        assertThat(result.getM12()).isEqualTo(0f);
        assertThat(result.getM22()).isEqualTo(0f);
        assertThat(result.getM32()).isEqualTo(0f);

        assertThat(result.getM03()).isEqualTo(0f);
        assertThat(result.getM13()).isEqualTo(0f);
        assertThat(result.getM23()).isEqualTo(0f);
        assertThat(result.getM33()).isEqualTo(0f);
    }

    @Test
    public void testMultiplyMat() throws Exception {
        //given
        //@formatter:off
        final Mat4 left = Mat4.create(  11f, 22f,   33f, 44f,
                                        55f, 66f,   77f, 88f,
                                       -.5f, -1f, -1.5f, -2f,
                                      -2.5f, -3f, -3.5f, -4f);
        //@formatter:on
        //@formatter:off
        final Mat4 right = Mat4.create(  9f, 8f,   7f, 6f,
                                         5f, 4f,   3f, 2f,
                                        .5f, 1f, 1.5f, 2f,
                                       2.5f, 3f, 3.5f, 4f);
        //@formatter:on
        //when
        final Mat4 result = left.multiply(right);

        //then
        assertThat(result.getM00()).isEqualTo(335.5f);
        assertThat(result.getM10()).isEqualTo(341f);
        assertThat(result.getM20()).isEqualTo(346.5f);
        assertThat(result.getM30()).isEqualTo(352f);

        assertThat(result.getM01()).isEqualTo(1083.5f);
        assertThat(result.getM11()).isEqualTo(1045f);
        assertThat(result.getM21()).isEqualTo(1006.5f);
        assertThat(result.getM31()).isEqualTo(968f);

        assertThat(result.getM02()).isEqualTo(-15.25f);
        assertThat(result.getM12()).isEqualTo(-15.5f);
        assertThat(result.getM22()).isEqualTo(-15.75f);
        assertThat(result.getM32()).isEqualTo(-16f);

        assertThat(result.getM03()).isEqualTo(-49.25f);
        assertThat(result.getM13()).isEqualTo(-47.5f);
        assertThat(result.getM23()).isEqualTo(-45.75f);
        assertThat(result.getM33()).isEqualTo(-44f);
    }

    @Test
    public void testMultiplyVec() throws Exception {
        //given
        //@formatter:off
        final Mat4 left = Mat4.create(  11f, 22f,   33f, 44f,
                                        55f, 66f,   77f, 88f,
                                       -.5f, -1f, -1.5f, -2f,
                                      -2.5f, -3f, -3.5f, -4f);
        //@formatter:on
        //@formatter:off
        final Vec4 right = Vec4.create(  11f,
                                         66f,
                                       -1.5f,
                                         -4f);
        //@formatter:on

        //when
        final Vec4 result = left.multiply(right);

        //then
        assertThat(result.getX()).isEqualTo(1347.5f);
        assertThat(result.getY()).isEqualTo(4493.5f);
        assertThat(result.getZ()).isEqualTo(-61.25f);
        assertThat(result.getW()).isEqualTo(-204.25f);
    }

    @Test
    public void testInvert() throws Exception {
        //given
        //@formatter:off
        final Mat4 left = Mat4.create(1f, 0f, 5f, 0f,
                                      2f, 1f, 6f, 0f,
                                      3f, 4f, 0f, 0f,
                                      0f, 0f, 0f, 1f);
        //@formatter:on

        //when
        final Mat4 result = left.invert();

        //then
        assertThat(result.getM00()).isIn(Range.closed(-24.0001f,
                                                      -23.9999f));//24
        assertThat(result.getM01()).isIn(Range.closed(17.9999f,
                                                      18.0001f));//20
        assertThat(result.getM02()).isIn(Range.closed(4.9999f,
                                                      5.0001f));//5
        assertThat(result.getM03()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0

        assertThat(result.getM10()).isIn(Range.closed(19.9999f,
                                                      20.0001f));//20
        assertThat(result.getM11()).isIn(Range.closed(-15.0001f,
                                                      -14.9999f));//-15
        assertThat(result.getM12()).isIn(Range.closed(-4.0001f,
                                                      -3.9999f));//-4
        assertThat(result.getM13()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0

        assertThat(result.getM20()).isIn(Range.closed(-5.0001f,
                                                      -4.9999f));//-5
        assertThat(result.getM21()).isIn(Range.closed(3.9999f,
                                                      4.0001f));//4
        assertThat(result.getM22()).isIn(Range.closed(.9999f,
                                                      1.0001f));//1
        assertThat(result.getM23()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0

        assertThat(result.getM30()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0
        assertThat(result.getM31()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0
        assertThat(result.getM32()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0
        assertThat(result.getM33()).isIn(Range.closed(.9999f,
                                                      1.0001f));//1
    }
}