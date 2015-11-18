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
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.egl.HasEglOutput;

import javax.annotation.Nonnull;

@AutoFactory(className = "PrivateDispmanxOutputFactory",
             allowSubclasses = true)
public class DispmanxOutput implements HasEglOutput {

    @Nonnull
    private final DispmanxEglOutput dispmanxEglOutput;

    DispmanxOutput(@Nonnull final DispmanxEglOutput dispmanxEglOutput) {
        this.dispmanxEglOutput = dispmanxEglOutput;
    }

    @Override
    public RenderOutput getEglOutput() {
        return this.dispmanxEglOutput;
    }
}
