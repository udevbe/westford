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

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DrmLibrary extends Library {
    String JNA_LIBRARY_NAME = "drm";

    DrmLibrary INSTANCE = (DrmLibrary) Native.loadLibrary(JNA_LIBRARY_NAME,
                                                          DrmLibrary.class);

    drmModeRes drmModeGetResources(int fd);

    drmModeConnector drmModeGetConnector(int fd,
                                         int connector_id);

    void drmModeFreeConnector(drmModeConnector connector);

    drmModeEncoder drmModeGetEncoder(int fd,
                                     int encoder_id);

    void drmModeFreeEncoder(drmModeEncoder encoder);
}
