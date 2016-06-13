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
package org.westmalle.wayland.dispmanx;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@AutoFactory(className = "PrivateDispmanxOutputFactory",
             allowSubclasses = true)
public class DispmanxPlatform {

    @Nonnull
    private final WlOutput            wlOutput;
    private final int                 dispmanxElement;
    @Nonnull
    private final DISPMANX_MODEINFO_T modeinfo;

    DispmanxPlatform(@Nonnull final WlOutput wlOutput,
                     final int dispmanxElement,
                     @Nonnull final DISPMANX_MODEINFO_T modeinfo) {
        this.wlOutput = wlOutput;
        this.dispmanxElement = dispmanxElement;
        this.modeinfo = modeinfo;
    }

    @Nonnull
    public DISPMANX_MODEINFO_T getModeinfo() {
        return this.modeinfo;
    }

    public int getDispmanxElement() {
        return this.dispmanxElement;
    }

    @Nonnull
    public WlOutput getWlOutput() {
        return this.wlOutput;
    }
}
