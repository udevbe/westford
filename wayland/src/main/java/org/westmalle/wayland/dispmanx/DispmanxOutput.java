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
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.egl.HasEglOutput;
import org.westmalle.wayland.nativ.libbcm_host.DISPMANX_MODEINFO_T;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoFactory(className = "PrivateDispmanxOutputFactory",
             allowSubclasses = true)
public class DispmanxOutput implements HasEglOutput {

    @Nonnull
    private final DispmanxEglOutputFactory dispmanxEglOutputFactory;
    private final int                      dispmanxElement;
    @Nonnull
    private final DISPMANX_MODEINFO_T      modeinfo;

    @Nullable
    private DispmanxEglOutput dispmanxEglOutput;

    DispmanxOutput(@Nonnull @Provided final DispmanxEglOutputFactory dispmanxEglOutputFactory,
                   final int dispmanxElement,
                   @Nonnull final DISPMANX_MODEINFO_T modeinfo) {
        this.dispmanxEglOutputFactory = dispmanxEglOutputFactory;
        this.dispmanxElement = dispmanxElement;
        this.modeinfo = modeinfo;
    }

    @Override
    public RenderOutput getEglOutput() {
        if (this.dispmanxEglOutput == null) {
            this.dispmanxEglOutput = this.dispmanxEglOutputFactory.create(this.dispmanxElement,
                                                                          this.modeinfo.width,
                                                                          this.modeinfo.height);
        }
        return this.dispmanxEglOutput;
    }
}
