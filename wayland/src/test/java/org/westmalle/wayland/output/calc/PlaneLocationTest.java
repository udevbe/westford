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