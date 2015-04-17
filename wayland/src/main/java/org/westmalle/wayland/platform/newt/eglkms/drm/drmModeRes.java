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
package org.westmalle.wayland.platform.newt.eglkms.drm;


import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class drmModeRes extends Structure {

    public int     count_fbs;
    public Pointer fbs;

    public int     count_crtcs;
    public Pointer crtcs;

    public int     count_connectors;
    public Pointer connectors;

    public int     count_encoders;
    public Pointer encoders;

    public int min_width, max_width;
    public int min_height, max_height;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("count_fbs",
                             "fbs",
                             "count_crtcs",
                             "crtcs",
                             "count_connectors",
                             "connectors",
                             "count_encoders",
                             "encoders",
                             "min_width",
                             "max_width",
                             "min_height",
                             "max_height");
    }
}