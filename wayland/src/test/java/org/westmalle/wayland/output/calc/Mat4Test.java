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
package org.westmalle.wayland.output.calc;

import com.google.common.collect.Range;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class Mat4Test {

    @Test
    public void testAdd() throws Exception {
        //given
        final Mat4 left = Mat4.create(11f,
                                      22f,
                                      33f,
                                      44f,
                                      55f,
                                      66f,
                                      77f,
                                      88f,
                                      -.5f,
                                      -1f,
                                      -1.5f,
                                      -2f,
                                      -2.5f,
                                      -3f,
                                      -3.5f,
                                      -4f);
        final Mat4 right = Mat4.create(9f,
                                       8f,
                                       7f,
                                       6f,
                                       5f,
                                       4f,
                                       3f,
                                       2f,
                                       .5f,
                                       1f,
                                       1.5f,
                                       2f,
                                       2.5f,
                                       3f,
                                       3.5f,
                                       4f);

        //when
        final Mat4 result = left.add(right);

        //then
        assertThat(result.getM00()).isEqualTo(20f);
        assertThat(result.getM01()).isEqualTo(30f);
        assertThat(result.getM02()).isEqualTo(40f);
        assertThat(result.getM03()).isEqualTo(50f);

        assertThat(result.getM10()).isEqualTo(60f);
        assertThat(result.getM11()).isEqualTo(70f);
        assertThat(result.getM12()).isEqualTo(80f);
        assertThat(result.getM13()).isEqualTo(90f);

        assertThat(result.getM20()).isEqualTo(0f);
        assertThat(result.getM21()).isEqualTo(0f);
        assertThat(result.getM22()).isEqualTo(0f);
        assertThat(result.getM23()).isEqualTo(0f);

        assertThat(result.getM30()).isEqualTo(0f);
        assertThat(result.getM31()).isEqualTo(0f);
        assertThat(result.getM32()).isEqualTo(0f);
        assertThat(result.getM33()).isEqualTo(0f);
    }

    @Test
    public void testMultiplyMat() throws Exception {
        //given
        final Mat4 left = Mat4.create(11f,
                                      22f,
                                      33f,
                                      44f,
                                      55f,
                                      66f,
                                      77f,
                                      88f,
                                      -.5f,
                                      -1f,
                                      -1.5f,
                                      -2f,
                                      -2.5f,
                                      -3f,
                                      -3.5f,
                                      -4f);
        final Mat4 right = Mat4.create(9f,
                                       8f,
                                       7f,
                                       6f,
                                       5f,
                                       4f,
                                       3f,
                                       2f,
                                       .5f,
                                       1f,
                                       1.5f,
                                       2f,
                                       2.5f,
                                       3f,
                                       3.5f,
                                       4f);

        //when
        final Mat4 result = left.multiply(right);

        //then
        assertThat(result.getM00()).isEqualTo(520.5f);
        assertThat(result.getM01()).isEqualTo(701f);
        assertThat(result.getM02()).isEqualTo(881.5f);
        assertThat(result.getM03()).isEqualTo(1062f);

        assertThat(result.getM10()).isEqualTo(268.5f);
        assertThat(result.getM11()).isEqualTo(365f);
        assertThat(result.getM12()).isEqualTo(461.5f);
        assertThat(result.getM13()).isEqualTo(558f);

        assertThat(result.getM20()).isEqualTo(54.75f);
        assertThat(result.getM21()).isEqualTo(69.5f);
        assertThat(result.getM22()).isEqualTo(84.25f);
        assertThat(result.getM23()).isEqualTo(99f);

        assertThat(result.getM30()).isEqualTo(180.75f);
        assertThat(result.getM31()).isEqualTo(237.5f);
        assertThat(result.getM32()).isEqualTo(294.25f);
        assertThat(result.getM33()).isEqualTo(351f);
    }

    @Test
    public void testMultiplyVec() throws Exception {
        //given
        final Mat4 left = Mat4.create(11f,
                                      22f,
                                      33f,
                                      44f,
                                      55f,
                                      66f,
                                      77f,
                                      88f,
                                      -.5f,
                                      -1f,
                                      -1.5f,
                                      -2f,
                                      -2.5f,
                                      -3f,
                                      -3.5f,
                                      -4f);

        final Vec4 right = Vec4.create(11f,
                                       66f,
                                       -1.5f,
                                       -4f);

        //when
        final Vec4 result = left.multiply(right);

        //then
        assertThat(result.getX()).isEqualTo(3761.75f);
        assertThat(result.getY()).isEqualTo(4611.5f);
        assertThat(result.getZ()).isEqualTo(5461.25f);
        assertThat(result.getW()).isEqualTo(6311f);
    }

    @Test
    public void testInvert() throws Exception {
        //given
        final Mat4 left = Mat4.create(1f,
                                      0f,
                                      5f,
                                      0f,
                                      2f,
                                      1f,
                                      6f,
                                      0f,
                                      3f,
                                      4f,
                                      0f,
                                      0f,
                                      0f,
                                      0f,
                                      0f,
                                      1f);

        //when
        final Mat4 result = left.invert();

        //then
        assertThat(result.getM00()).isIn(Range.closed(-24.0001f,
                                                      -23.9999f));//24
        assertThat(result.getM01()).isIn(Range.closed(19.9999f,
                                                      20.0001f));//20
        assertThat(result.getM02()).isIn(Range.closed(-5.0001f,
                                                      -4.9999f));//-5
        assertThat(result.getM03()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0

        assertThat(result.getM10()).isIn(Range.closed(17.9999f,
                                                      18.0001f));//18
        assertThat(result.getM11()).isIn(Range.closed(-15.0001f,
                                                      -14.9999f));//-15
        assertThat(result.getM12()).isIn(Range.closed(3.9999f,
                                                      4.0001f));//4
        assertThat(result.getM13()).isIn(Range.closed(-.0001f,
                                                      0.0001f));//0

        assertThat(result.getM20()).isIn(Range.closed(4.9999f,
                                                      5.0001f));//5
        assertThat(result.getM21()).isIn(Range.closed(-4.0001f,
                                                      3.9999f));//-4
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