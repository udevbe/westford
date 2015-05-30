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
package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.sun.jna.Pointer;
import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.egl.HasEglOutput;

import javax.annotation.Nonnull;

@AutoFactory(className = "XOutputFactory")
public class XOutput implements HasEglOutput {

    @Nonnull
    private final XEglOutputFactory xEglOutputFactory;
    private final int               xWindow;
    @Nonnull
    private final Pointer           xDisplay;

    private XEglOutput eglOutput;

    XOutput(@Provided @Nonnull final XEglOutputFactory xEglOutputFactory,
            @Nonnull final Pointer xDisplay,
            final int xWindow) {
        this.xEglOutputFactory = xEglOutputFactory;
        this.xWindow = xWindow;
        this.xDisplay = xDisplay;
    }

    @Override
    public EglOutput getEglOutput() {
        if (this.eglOutput == null) {
            this.eglOutput = this.xEglOutputFactory.create(this.xDisplay,
                                                           this.xWindow);
        }
        return this.eglOutput;
    }
}