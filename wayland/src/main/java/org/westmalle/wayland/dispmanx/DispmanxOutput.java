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
import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.egl.HasEglOutput;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

@AutoFactory(className = "PrivateDispmanxOutputFactory",
             allowSubclasses = true)
public class DispmanxOutput implements HasEglOutput {

    private final DispmanxEglOutputFactory dispmanxEglOutputFactory;
    private final EGL_DISPMANX_WINDOW_T    dispmanxWindow;

    private DispmanxEglOutput eglOutput;

    DispmanxOutput(@Provided final DispmanxEglOutputFactory dispmanxEglOutputFactory,
                   final EGL_DISPMANX_WINDOW_T dispmanxWindow) {
        this.dispmanxEglOutputFactory = dispmanxEglOutputFactory;
        this.dispmanxWindow = dispmanxWindow;
    }

    @Override
    public EglOutput getEglOutput() {
        if (this.eglOutput == null) {
            this.eglOutput = this.dispmanxEglOutputFactory.create(this.dispmanxWindow);
        }
        return this.eglOutput;
    }
}
