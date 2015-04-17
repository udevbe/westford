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

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class drmModeEncoder extends Structure {

    public int encoder_id;
    public int encoder_type;
    public int crtc_id;
    public int possible_crtcs;
    public int possible_clones;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("encoder_id",
                             "encoder_type",
                             "crtc_id",
                             "possible_crtcs",
                             "possible_clones");
    }
}
