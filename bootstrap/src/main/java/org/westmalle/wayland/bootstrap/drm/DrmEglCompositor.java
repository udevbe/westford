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
package org.westmalle.wayland.bootstrap.drm;

import dagger.Component;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.drm.egl.GbmEglPlatformModule;
import org.westmalle.wayland.gles2.Gles2RendererModule;
import org.westmalle.wayland.input.LibinputSeatFactory;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      GbmEglPlatformModule.class})
public interface DrmEglCompositor {

    LifeCycle lifeCycle();

    LibinputSeatFactory seatFactory();
}
