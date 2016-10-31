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
package org.westford.compositor.core.calc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;

@RunWith(MockitoJUnitRunner.class)
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