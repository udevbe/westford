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

public class drmModeConnector extends Structure {

    public int connector_id;
    public int encoder_id;
    /**
     * < Encoder currently connected to
     */
    public int connector_type;
    public int connector_type_id;
    public int connection;
    public int mmWidth, mmHeight;
    /**
     * < HxW in millimeters
     */
    public int subpixel;

    public int                         count_modes;
    public drmModeModeInfo.ByReference modes;

    public int     count_props;
    public Pointer props;
    /**
     * < List of property ids
     */
    public Pointer prop_values;
    /**
     * < List of property values
     */

    public int     count_encoders;
    public Pointer encoders;

    /**
     * < List of encoder ids
     */

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("connector_id",
                             "encoder_id",
                             "connector_type",
                             "connector_type_id",
                             "connection",
                             "mmWidth",
                             "mmHeight",
                             "subpixel",
                             "count_modes",
                             "modes",
                             "count_props",
                             "props",
                             "prop_values",
                             "count_encoders",
                             "encoders");
    }
}
