//Copyright 2016 Erik De Rijcke
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
package org.westmalle.wayland.bootstrap.x11;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.X11PlatformModule;
import org.westmalle.wayland.x11.egl.X11EglPlatformModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      X11EglPlatformModule.class,
                      X11EglPlatformAdaptorModule.class})
public interface X11EglCompositor {

    LifeCycle lifeCycle();

    /*
     * X11 egl platform provides a single seat.
     */
    WlSeat wlSeat();
}
