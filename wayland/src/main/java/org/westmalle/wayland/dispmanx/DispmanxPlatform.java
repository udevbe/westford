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
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;

import javax.annotation.Nonnull;

@AutoFactory(className = "PrivateDispmanxPlatformFactory",
             allowSubclasses = true)
public class DispmanxPlatform implements Platform {

    @Nonnull
    private final DISPMANX_MODEINFO_T modeinfo;

    @Nonnull
    private final DispmanxConnector[] dispmanxConnectors;

    DispmanxPlatform(@Nonnull final DISPMANX_MODEINFO_T modeinfo,
                     @Nonnull final DispmanxConnector[] dispmanxConnectors) {
        this.modeinfo = modeinfo;
        this.dispmanxConnectors = dispmanxConnectors;
    }

    @Nonnull
    public DISPMANX_MODEINFO_T getModeinfo() {
        return this.modeinfo;
    }

    @Nonnull
    @Override
    public DispmanxConnector[] getConnectors() {
        return this.dispmanxConnectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }
}
