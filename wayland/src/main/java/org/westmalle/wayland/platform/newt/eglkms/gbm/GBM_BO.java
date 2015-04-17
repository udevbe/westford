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
package org.westmalle.wayland.platform.newt.eglkms.gbm;

public interface GBM_BO {

    /**
     * Buffer is going to be presented to the screen using an API such as KMS
     */
    int GBM_BO_USE_SCANOUT      = (1 << 0);
    /**
     * Buffer is going to be used as cursor
     */
    int GBM_BO_USE_CURSOR       = (1 << 1);
    /**
     * Deprecated
     */
    int GBM_BO_USE_CURSOR_64X64 = GBM_BO_USE_CURSOR;
    /**
     * Buffer is to be used for rendering - for example it is going to be used as the storage for a color buffer
     */
    int GBM_BO_USE_RENDERING    = (1 << 2);
    /**
     * Buffer can be used for gbm_bo_write.  This is guaranteed to work with GBM_BO_USE_CURSOR. but may not work for
     * other combinations.
     */
    int GBM_BO_USE_WRITE        = (1 << 3);
}
